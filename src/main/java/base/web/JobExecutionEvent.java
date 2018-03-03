package base.web;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "jobExecutionEvent")
class JobExecutionEvent {

	@DatabaseField(id = true, columnName = "id", width = 36)
	@Expose(deserialize = false)
	private String id;

	@DatabaseField(columnName = "jobId", foreign = true, foreignAutoRefresh = true)
	private Job job;

	@DatabaseField(columnName = "message")
	@Expose(deserialize = false)
	private String message;

	@DatabaseField(columnName = "recordedAt", index = true)
	@Expose(deserialize = false)
	private Timestamp recordedAt;

	protected JobExecutionEvent() {
		this.id = UUID.randomUUID().toString();
		this.recordedAt = Timestamp.from(Instant.now());
	}

	protected JobExecutionEvent(Job job, String message) {
		this();
		this.job = job;
		this.message = message;
	}

}
