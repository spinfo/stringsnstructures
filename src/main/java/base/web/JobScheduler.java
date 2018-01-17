package base.web;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import base.web.JobExecutionCallbackReceiver.JobFailureListener;
import base.web.Server.InvalidWorkflowDefiniton;
import base.workbench.ModuleWorkbenchController;
import modules.ModuleNetwork;

public class JobScheduler implements JobFailureListener, Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(JobScheduler.class);

	private static JobScheduler instance;

	private Map<Long, ModuleNetwork> startedJobs;

	// these regulate how often a wakeup will occur
	private final int intervallsTillWakeupDefault = 10;
	private int intervallsTillWakeup = 0;
	private long intervallDuration = 1000L;

	// private constructor for singleton instance
	private JobScheduler() {
		// todo: make sure we need this concurrently
		this.startedJobs = new ConcurrentHashMap<>();
	}

	// TODO: It's hard to reason about whether the reference to the job network will
	// be removed if an error occurs at some point __during__ startup. This could be
	// better
	private void startJob(Job job) throws InvalidWorkflowDefiniton, SQLException {

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
			throw new InvalidWorkflowDefiniton(e);
		}
		// create a new listener for the job, that will contact us on failure
		JobExecutionCallbackReceiver receiver = new JobExecutionCallbackReceiver(job, this);

		// start everything
		ModuleNetwork network = controller.getModuleNetwork();
		try {
			if (!network.addCallbackReceiver(receiver)) {
				throw new RuntimeException("Could not add callback receiver.");
			}
			network.runModules();
		} catch (Exception e) {
			e.printStackTrace();
			String msg = "Unexpected error on job start: " + e.getMessage();
			LOGGER.error(msg);
			job.setFailed(msg);
			onJobFailure(job);
		}

		// save a reference to the network if we got this far
		job.setStarted();
		this.startedJobs.put(job.getId(), network);
	}

	public static JobScheduler instance() {
		if (instance == null) {
			instance = new JobScheduler();
		}
		return instance;
	}

	@Override
	public void run() {
		while (true) {
			if (intervallsTillWakeup > 0) {
				try {
					intervallsTillWakeup -= 1;
					Thread.sleep(intervallDuration);
				} catch (InterruptedException e) {
					LOGGER.error(
							"Unexpected interrupt. Shutting down all jobs. Interrupt exception was: " + e.getMessage());
					for (Long id : startedJobs.keySet()) {
						this.shutdownModuleNetwork(id);
					}
				}
			} else {
				intervallsTillWakeup = intervallsTillWakeupDefault;
				processPendingJobs();
				processExecutingJobs();
			}
		}
	}

	private void processPendingJobs() {
		try {
			List<Job> pending = Job.fetchPending();
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
		List<Long> toEnd = new ArrayList<>();
		try {
			for (Long jobId : startedJobs.keySet()) {
				ModuleNetwork network = startedJobs.get(jobId);

				if (!network.isRunning()) {
					// every job the module network of which is not running will be stopped
					toEnd.add(jobId);
					Job job = Job.fetch(jobId);

					if (job.getEndedAt() == null) {
						// the job has finished successfully by not having failed before
						job.setSucceeded();
					} else {
						// this should never happen, but make sure
						if (!job.isFailed()) {
							throw new IllegalStateException(
									"Found successfully ended Job, which was not harvested before.");
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("Exception while processing started jobs: " + e.getMessage());
		} finally {
			// Make sure that any error producing jobs, do not do so in the next iteration
			// of processing
			for (Long jobId : toEnd) {
				shutdownModuleNetwork(jobId);
			}
		}
		// TODO: Remove (Kept in for testing for memory leaks.)
		System.gc();
	}

	public synchronized void wakeup() {
		this.intervallsTillWakeup = 0;
	}

	@Override
	public void onJobFailure(Job job) {
		if (job == null) {
			LOGGER.error("Got invalid call to handle failure for null job.");
			return;
		}
		shutdownModuleNetwork(job.getId());
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
	}
}
