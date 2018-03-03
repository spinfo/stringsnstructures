package base.web;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

import com.google.gson.annotations.Expose;

class StatusReport {

	@Expose(deserialize = false)
	private String name;

	// the processors available to the jvm
	@Expose(deserialize = false)
	private int availableProcessors;

	// the maximum amount of memory allocatable to the jvm by the os (in bytes)
	@Expose(deserialize = false)
	private long maxMemory;

	// the memory allocated to the jvm by the os (in bytes)
	@Expose(deserialize = false)
	private long totalMemory;

	// the free memory within the memory allocated to the jvm by the os (in bytes)
	@Expose(deserialize = false)
	private long freeMemory;

	// the memory actually used (totalMemory - freeMemory, in bytes)
	@Expose(deserialize = false)
	private long usedMemory;

	// the memory that can be newly allocated by us atm (maxMemory - usedMemory, in bytes)
	@Expose(deserialize = false)
	private long usableMemory;

	// the amount of running jobs as reported by the db
	@Expose(deserialize = false)
	private long runningJobs;

	@Expose(deserialize = false)
	private Timestamp collectedAt;

	public StatusReport() {
		// empty constructor mainly for ormlite
		this.collectedAt = Timestamp.from(Instant.now());
	}

	protected static StatusReport collect() throws SQLException {
		StatusReport result = new StatusReport();
		Runtime runtime = Runtime.getRuntime();

		// these are probably always the same, but collect them each time anyway,
		// because who knows
		result.availableProcessors = runtime.availableProcessors();
		result.name = ServerConfig.get().getName();

		// get some memory values directly returned by the runtime
		result.maxMemory = runtime.maxMemory();
		result.totalMemory = runtime.totalMemory();
		result.freeMemory = runtime.freeMemory();

		// calculate some values on our own
		result.usedMemory = result.totalMemory - result.freeMemory;
		result.usableMemory = result.maxMemory - result.usedMemory;

		// get some information out of the db
		result.runningJobs = JobDao.countRunningJobs();

		return result;
	}

}
