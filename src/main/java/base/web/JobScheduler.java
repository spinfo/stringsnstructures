package base.web;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import base.web.JobExecutionCallbackReceiver;
import base.workbench.ModuleWorkbenchController;
import modules.ModuleNetwork;

class JobScheduler implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(JobScheduler.class);

	private static JobScheduler instance;

	// the started jobs database ids along with the module network that is executing
	// them
	private Map<Long, ModuleNetwork> startedJobs;

	// these regulate how often a wakeup will occur, e.g.: an intervallDuration of
	// 1000 ms and and intervallsTillWakeup set to 10 means that 10 seconds will
	// pass between wakeups (if no wakeup is induced from the outside).
	private final int intervallsTillWakeup = 10;
	private long intervallDuration = 1000L;
	private int intervallsCount = 0;

	// private constructor for singleton instance
	private JobScheduler() {
		// this is needed concurrently as some values might be looked up from a route or
		// a job execution callback
		this.startedJobs = new ConcurrentHashMap<>();
	}

	public static JobScheduler instance() {
		if (instance == null) {
			instance = new JobScheduler();
		}
		return instance;
	}

	protected synchronized void wakeup() {
		this.intervallsCount = 0;
	}

	protected boolean isRunningJob(long jobId) {
		ModuleNetwork network = startedJobs.get(jobId);
		return (network != null) && network.isRunning();
	}

	@Override
	public void run() {
		while (true) {
			if (intervallsCount > 0) {
				try {
					intervallsCount -= 1;
					Thread.sleep(intervallDuration);
				} catch (InterruptedException e) {
					LOGGER.error(
							"Unexpected interrupt. Shutting down all jobs. Interrupt exception was: " + e.getMessage());
					for (Long id : startedJobs.keySet()) {
						this.shutdownModuleNetwork(id);
					}
				}
			} else {
				intervallsCount = intervallsTillWakeup;

				// first look for old jobs to stop/handle
				processExecutingJobs();

				// then start new ones and save a health info capturing the started jobs no
				processPendingJobs();
				checkHealth();
			}
		}
	}

	private void processPendingJobs() {
		try {
			List<Job> pending = JobDao.fetchPending();
			LOGGER.debug("Got " + pending.size() + " pending jobs, in execution: " + startedJobs.size());
			for (Job job : pending) {
				LOGGER.debug("Starting pending job: " + job.getId());
				try {
					startJob(job);
				} catch (Exception e) {
					e.printStackTrace();
					LOGGER.error("Exception while starting job: " + e.getMessage());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("Exception while processing pending jobs: " + e.getMessage());
		}
	}

	private void processExecutingJobs() {
		// if this throws, it might do so on every iteration, so shutdown everything
		List<Job> runningJobs = Collections.emptyList();
		try {
			runningJobs = JobDao.fetch(startedJobs.keySet());
		} catch (SQLException e) {
			LOGGER.error("Fatal: Unable to get running jobs: " + e.getMessage());
			panicAndStopEverything();
		}
		for (Job job : runningJobs) {
			try {
				ModuleNetwork network = startedJobs.get(job.getId());

				// a job is successful if it was not stopped externally and the network is no
				// longer running
				if (!job.hasEnded() && !network.isRunning()) {
					job.setSucceeded();
					shutdownModuleNetwork(job.getId());
				}
				// a job may have been cancelled
				else if (job.hasFailed()) {
					shutdownModuleNetwork(job.getId());
				}
				// a job may not have been set as succeeded anywhere else
				else if (job.hasSucceeded()) {
					throw new IllegalStateException("Found successfully ended Job, which was not harvested before.");
				}
			} catch (Exception e) {
				shutdownModuleNetwork(job.getId());
				LOGGER.error("Exception while processing started jobs: " + e.getMessage());
			}
		}
	}

	private void checkHealth() {
		try {
			HealthInformation info = HealthInformation.collect();
			HealthInformationDao.create(info);
		} catch (Exception e) {
			LOGGER.error("Error when saving health information: " + e.getMessage());
		}
	}

	// if there are no running jobs, we may somewhat safely ask for garbage
	// collection to
	// allow for better health checks
	private void maybeGarbageCollect() {
		if (this.startedJobs.size() == 0) {
			LOGGER.debug("Asking for garbage collection as there are no running jobs.");
			System.gc();
		}
	}

	private void panicAndStopEverything() {
		for (Long jobId : startedJobs.keySet()) {
			try {
				shutdownModuleNetwork(jobId);
				Job job = JobDao.fetch(jobId);
				if (!job.hasEnded()) {
					job.setFailed("Cancelled in panic mode.");
				}
			} catch (Exception e) {
				LOGGER.error("Error when stopping job in panic mode: " + e.getMessage());
			}
		}
	}

	// TODO: It's hard to reason about whether the reference to the job network will
	// be removed if an error occurs at some point __during__ startup. This could be
	// better.
	private void startJob(Job job) throws WebError.InvalidWorkflowDefiniton, SQLException {

		// instantiate a new workbench controller (this should never fail
		ModuleWorkbenchController controller;
		try {
			controller = new ModuleWorkbenchController();
		} catch (Exception e) {
			LOGGER.error("Unexpected fatal error: Cannot instantiate workbench controller.");
			throw new RuntimeException(e);
		}
		try {
			controller.loadModuleNetworkFromString(job.getWorkflowDefinition(), true);
		} catch (Exception e) {
			throw new WebError.InvalidWorkflowDefiniton(e);
		}
		// create a new listener for the job, that will contact us on failure
		JobExecutionCallbackReceiver receiver = new JobExecutionCallbackReceiver(job);

		// start everything
		ModuleNetwork network = controller.getModuleNetwork();
		try {
			// this should never fail, but make sure
			if (!network.addCallbackReceiver(receiver)) {
				throw new RuntimeException("Could not add callback receiver.");
			}
			network.runModules();
		} catch (Exception e) {
			e.printStackTrace();
			String msg = "Unexpected error on job start: " + e.getMessage();
			LOGGER.error(msg);
			network.stopModules();
			job.setFailed(msg);
		}
		// save a reference to the network if we got this far
		this.startedJobs.put(job.getId(), network);
		job.setStarted();
	}

	// shutdown all references to the module network and
	private void shutdownModuleNetwork(long jobId) {
		// remove our internal reference to the network
		ModuleNetwork network = startedJobs.remove(jobId);
		if (network != null) {
			LOGGER.debug("Shutting down module network for job: " + jobId);
			if (network.isRunning()) {
				network.stopModules();
			}
			network.removeAllModules();
		} else {
			LOGGER.error("No moduleNetwork present to shut down, jobId: " + jobId);
		}
		// after a job is removed we may garbage collect something
		maybeGarbageCollect();
	}
}
