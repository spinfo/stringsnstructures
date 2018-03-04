package base.web;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.DeleteBuilder;

class JobDao {

	protected static boolean exists(long id) throws SQLException {
		synchronized (DatabaseFacade.GLOBAL_LOCK) {
			return dao().idExists(id);
		}
	}

	protected static List<Job> fetchPending() throws SQLException {
		synchronized (DatabaseFacade.GLOBAL_LOCK) {
			return dao().queryBuilder().orderBy("createdAt", true).where().isNull("startedAt").query();
		}
	}

	protected static List<Job> fetchRunning() throws SQLException {
		synchronized (DatabaseFacade.GLOBAL_LOCK) {
			return dao().queryBuilder().where().isNotNull("startedAt").and().isNull("endedAt").query();
		}
	}

	protected static Job fetch(long id) throws SQLException {
		synchronized (DatabaseFacade.GLOBAL_LOCK) {
			return dao().queryForId(id);
		}
	}

	protected static List<Job> fetch(Iterable<Long> ids) throws SQLException {
		synchronized (DatabaseFacade.GLOBAL_LOCK) {
			return dao().queryBuilder().where().in("id", ids).query();
		}
	}

	protected static long countRunningJobs() throws SQLException {
		synchronized (DatabaseFacade.GLOBAL_LOCK) {
			return dao().queryBuilder().where().isNotNull("startedAt").and().isNull("endedAt").countOf();
		}
	}

	protected static int deleteJob(Job job) throws SQLException {
		synchronized (DatabaseFacade.GLOBAL_LOCK) {
			DeleteBuilder<JobExecutionEvent, String> eventsDelete = eventDao().deleteBuilder();
			eventsDelete.where().eq("jobId", job.getId());

			return TransactionManager.callInTransaction(dao().getConnectionSource(), new Callable<Integer>() {
				@Override
				public Integer call() throws Exception {
					eventsDelete.delete();
					return dao().delete(job);
				}
			});
		}
	}

	protected static long sumRunningJobsMemoryDemands() throws SQLException {
		synchronized (DatabaseFacade.GLOBAL_LOCK) {
			String[] result = dao().queryBuilder().selectRaw("SUM(maxMemory)").where().isNotNull("startedAt").and()
					.isNull("endedAt").queryRawFirst();
			if (result == null || result.length != 1 || result[0] == null) {
				return 0L;
			}
			try {
				return Long.parseLong(result[0]);
			} catch (NumberFormatException e) {
				throw new SQLException("Unexpected result for sum query: '" + result[0] + "'.");
			}
		}
	}

	private static Dao<Job, Long> dao() throws SQLException {
		return DatabaseFacade.getInstance().jobDao();
	}

	private static Dao<JobExecutionEvent, String> eventDao() throws SQLException {
		return DatabaseFacade.getInstance().getJobExecutionEventDao();
	}

}
