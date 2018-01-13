package base.web;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;

class Job {
	
	static final Timestamp ZERO_TIMESTAMP = new Timestamp(0L);

	@DatabaseField(id = true)
	private long id;
	
	@DatabaseField(columnName = "maxMemory")
	private long maxMemory;
	
	@DatabaseField(columnName = "maxTime")
	private long maxTime;
	
	@DatabaseField(columnName = "maxResultSize")
	private long maxResultSize;
	
	@DatabaseField(columnName = "createdAt", index = true)
	private Timestamp createdAt;
	
	@DatabaseField(columnName = "startedAt", index = true)
	private Timestamp startedAt = ZERO_TIMESTAMP;
	
	@DatabaseField(columnName = "endedAt", index = true)
	private Timestamp endedAt = ZERO_TIMESTAMP;
	
	@DatabaseField(columnName = "workflowDefinition")
	private String workflowDefinition;
	
	protected Job() {
		this.createdAt = currentTime();
	}
	
	private Dao<Job, Long> dao() throws SQLException {
		return DatabaseFacade.getInstance().jobDao();
	}
	
	private Timestamp currentTime() {
		return Timestamp.from(Instant.now());
	}
	
	void save() throws SQLException {
		dao().createOrUpdate(this);
	}
	
	void setStarted() throws SQLException {
		this.startedAt = currentTime();
		this.save();
	}
	
	void setEnded() throws SQLException {
		this.endedAt = currentTime();
		this.save();
	}

	public String getWorkflowDefinition() {
		return workflowDefinition;
	}

	public void setWorkflowDefinition(String workflowDefinition) {
		this.workflowDefinition = workflowDefinition;
	}

	public static Timestamp getZeroTimestamp() {
		return ZERO_TIMESTAMP;
	}

	public long getId() {
		return id;
	}

	public long getMaxMemory() {
		return maxMemory;
	}

	public long getMaxTime() {
		return maxTime;
	}

	public long getMaxResultSize() {
		return maxResultSize;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public Timestamp getStartedAt() {
		return startedAt;
	}

	public Timestamp getEndedAt() {
		return endedAt;
	}
	
	
	
}
