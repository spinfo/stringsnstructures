package base.web;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "healthInformation")
class HealthInformation {

	private static long MEMORY_PRESUMABLY_USED_BY_SERVER = 1500000;

	@DatabaseField(columnName = "id", generatedId = true)
	private long id;

	// the processors available to the jvm
	@DatabaseField
	@Expose
	private int availableProcessors;

	// the maximum amount of memory allocatable to the jvm by the os (in bytes)
	@DatabaseField
	@Expose
	private long maxMemory;

	// the memory allocated to the jvm by the os (in bytes)
	@DatabaseField
	@Expose
	private long totalMemory;

	// the free memory within the memory allocated to the jvm by the os (in bytes)
	@DatabaseField
	@Expose
	private long freeMemory;

	// the memory actually used (totalMemory - freeMemory, in bytes)
	@DatabaseField
	@Expose
	private long usedMemory;

	// the memory that can by maxiamlly allocated (maxMemory - usedMemory, in bytes)
	@DatabaseField
	@Expose
	private long presumablyFreeMemory;

	// the amount of running jobs as reported by the db
	@DatabaseField
	@Expose
	private long runningJobs;

	@DatabaseField(columnName = "collectedAt", index = true)
	@Expose
	private Timestamp collectedAt;

	public HealthInformation() {
		// empty constructor mainly for ormlite
		this.collectedAt = Timestamp.from(Instant.now());
	}

	protected static HealthInformation collect() throws SQLException {
		HealthInformation result = new HealthInformation();
		Runtime runtime = Runtime.getRuntime();

		// these are probably always the same, but collect them each time anyway,
		// because who knows
		result.availableProcessors = runtime.availableProcessors();

		// get some memory values directly returned by the runtime
		result.maxMemory = runtime.maxMemory();
		result.totalMemory = runtime.totalMemory();
		result.freeMemory = runtime.freeMemory();

		// calculate some values on our own
		result.usedMemory = result.totalMemory - result.freeMemory;
		result.presumablyFreeMemory = result.maxMemory - result.usedMemory;

		// get some information out of the db
		result.runningJobs = JobDao.countRunningJobs();

		return result;
	}

}
