package base.web;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.dao.Dao;

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

	private static Dao<Job, Long> dao() throws SQLException {
		return DatabaseFacade.getInstance().jobDao();
	}

}
