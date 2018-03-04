package base.web;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "job")
class Job {

	@DatabaseField(id = true)
	@Expose
	private long id = -1;

	@DatabaseField(columnName = "maxMemory")
	@Expose
	private long maxMemory = 0;

	@DatabaseField(columnName = "maxTime")
	@Expose
	private long maxTime = 0;

	@DatabaseField(columnName = "failed")
	@Expose
	private boolean failed;

	@DatabaseField(columnName = "createdAt", index = true)
	@Expose(deserialize = false)
	private Timestamp createdAt;

	@DatabaseField(columnName = "startedAt", index = true)
	@Expose(deserialize = false)
	private Timestamp startedAt;

	@DatabaseField(columnName = "endedAt", index = true)
	@Expose(deserialize = false)
	private Timestamp endedAt;

	@DatabaseField(columnName = "workflowDefinition")
	@Expose
	private String workflowDefinition;

	@ForeignCollectionField(eager = true, orderColumnName = "recordedAt", orderAscending = false)
	@Expose(deserialize = false)
	private Collection<JobExecutionEvent> events;

	protected Job() {
		this.failed = false;
		this.createdAt = currentTime();
		this.events = new ArrayList<>();
	}

	void save() throws SQLException {
		synchronized (DatabaseFacade.GLOBAL_LOCK) {
			dao().createOrUpdate(this);
		}
	}

	void setStarted() throws SQLException {
		synchronized (DatabaseFacade.GLOBAL_LOCK) {
			setStartTimeNow();
			this.addEvent("Started processing.");
			this.save();
		}
	}

	void setSucceeded() throws SQLException {
		synchronized (DatabaseFacade.GLOBAL_LOCK) {
			setEndTimeNow();
			this.failed = false;
			this.addEvent("Finished successfully.");
			this.save();
		}
	}

	void setFailed(String message) throws SQLException {
		synchronized (DatabaseFacade.GLOBAL_LOCK) {
			setEndTimeNow();
			this.failed = true;
			this.addEvent(message);
			this.save();
		}
	}

	void addEvent(String message) throws SQLException {
		synchronized (DatabaseFacade.GLOBAL_LOCK) {
			JobExecutionEvent event = new JobExecutionEvent(this, message);
			eventDao().create(event);
		}
	}

	protected String getWorkflowDefinition() {
		return workflowDefinition;
	}

	protected void setWorkflowDefinition(String workflowDefinition) {
		this.workflowDefinition = workflowDefinition;
	}

	protected long getId() {
		return id;
	}

	protected long getMaxMemory() {
		return maxMemory;
	}

	protected long getMaxTime() {
		return maxTime;
	}

	protected Timestamp getCreatedAt() {
		return createdAt;
	}

	protected Timestamp getStartedAt() {
		return startedAt;
	}

	protected Timestamp getEndedAt() {
		return endedAt;
	}

	/**
	 * @return true if an end date was recorded for this job.
	 */
	protected boolean hasEnded() {
		return getEndedAt() != null;
	}

	/**
	 * @return true if the job has ended and was successful, false otherwise.
	 */
	protected boolean hasSucceeded() {
		return hasEnded() && !failed;
	}

	/**
	 * @return true if the job has ended and has failed, false otherwise.
	 */
	protected boolean hasFailed() {
		return hasEnded() && failed;
	}

	/**
	 * Events are exposed read-only. Modify a job's events by using the model's
	 * methods.
	 * 
	 * @return An unmodifiable version of the events linked to this job.
	 */
	protected List<JobExecutionEvent> getEvents() {
		return Collections.unmodifiableList(new ArrayList<>(events));
	}

	private void setStartTimeNow() {
		// Never allow overwriting a recorded start time
		if (this.startedAt == null) {
			this.startedAt = currentTime();
		} else {
			throw new RuntimeException("Cannot set start time of an already started job.");
		}
	}

	private void setEndTimeNow() {
		// Never allow overwriting a recorded end time
		if (this.endedAt == null) {
			this.endedAt = currentTime();
		} else {
			throw new RuntimeException("Cannot set end time of an already ended job.");
		}
	}

	private static Dao<Job, Long> dao() throws SQLException {
		return DatabaseFacade.getInstance().jobDao();
	}

	private Dao<JobExecutionEvent, String> eventDao() throws SQLException {
		return DatabaseFacade.getInstance().getJobExecutionEventDao();
	}

	private Timestamp currentTime() {
		return Timestamp.from(Instant.now());
	}

}
