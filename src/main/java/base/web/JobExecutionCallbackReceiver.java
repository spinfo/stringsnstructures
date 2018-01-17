package base.web;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import common.parallelization.CallbackReceiverImpl;

class JobExecutionCallbackReceiver extends CallbackReceiverImpl {

	private static final Logger LOGGER = LoggerFactory.getLogger(JobExecutionCallbackReceiver.class);

	private final Job job;

	// TODO: Check if this interface is really necessary as dead jobs are harvested
	// periodically
	// an interface to communicate failures to outside parties
	protected interface JobFailureListener {
		void onJobFailure(Job job);
	};

	private final JobFailureListener failureListener;

	protected JobExecutionCallbackReceiver(Job job, JobFailureListener listener) {
		this.job = job;
		this.failureListener = listener;
	}

	@Override
	public void receiveCallback(Thread process, Object processingResult, boolean repeat) {
		if (checkCallbackExpectations(process, processingResult)) {
			Boolean success = (Boolean) processingResult;
			try {
				if (success) {
					String msg = String.format("Success: '%s'", process.getName());
					job.addEvent(msg);
					job.save();
				} else {
					String msg = String.format("Failure: '%s'", process.getName());
					job.setFailed(msg);
					failureListener.onJobFailure(job);
				}
			} catch (SQLException e) {
				failWithoutDbAccess("Unable to save event for job: " + job.getId());
			}
		}

		super.receiveCallback(process, processingResult, repeat);
	}

	@Override
	public void receiveException(Thread process, Throwable exception) {
		// mark the job as failed by noting the exceptions message
		String msg = String.format("Failure: '%s', message: %s", process.getName(), exception.getMessage());
		try {
			job.setFailed(msg);
			failureListener.onJobFailure(job);
		} catch (SQLException e) {
			failWithoutDbAccess("Unable to save failure for job: " + msg);
		}
		super.receiveException(process, exception);
	}

	// if we cannot contact the database, log a message and inform other listeners
	// to cancel
	private void failWithoutDbAccess(String message) {
		LOGGER.error(message);
		if (failureListener != null) {
			failureListener.onJobFailure(job);
		}
	}

	// check that we are receiving what we expected from the calling class
	private boolean checkCallbackExpectations(Thread process, Object processingResult) {
		if (process == null) {
			LOGGER.error("Invalid call to handle result of null thread.");
			return false;
		}
		if (process.getName() == null) {
			LOGGER.error("Invalid unnamed thread.");
			return false;
		}
		if (processingResult == null) {
			LOGGER.error("Invalid processing result: null");
			return false;
		}
		if (!Boolean.class.equals(processingResult.getClass())) {
			LOGGER.warn("Invalid processing result. Expected Boolean, got: " + processingResult.getClass());
			return false;
		}
		return true;
	}

}
