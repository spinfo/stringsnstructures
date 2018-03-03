package base.web;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

class DatabaseFacade {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseFacade.class);

	// a global lock which all accesses can be synchronised on
	// TODO: This should be configurable in the jdbc-connection
	public static final Object GLOBAL_LOCK = new Object();

	private ConnectionSource connectionSource;

	private static DatabaseFacade instance = null;

	private Dao<Job, Long> jobDao;
	private Dao<JobExecutionEvent, String> jobExecutionEventDao;

	private DatabaseFacade(final String jdbcUrl) {
		try {
			connectionSource = new JdbcPooledConnectionSource(jdbcUrl);
			setupTables(connectionSource);
		} catch (SQLException e) {
			LOGGER.error("Could not setup db: " + e.getMessage());
		}
	}

	static DatabaseFacade getInstance() {
		if (DatabaseFacade.instance == null) {
			instance = new DatabaseFacade(String.format("jdbc:sqlite:%s", ServerConfig.get().getDatabaseFile()));
		}
		return instance;
	}

	protected Dao<Job, Long> jobDao() throws SQLException {
		if (jobDao == null) {
			jobDao = DaoManager.createDao(connectionSource, Job.class);
		}
		return jobDao;
	}

	protected Dao<JobExecutionEvent, String> getJobExecutionEventDao() throws SQLException {
		if (jobExecutionEventDao == null) {
			jobExecutionEventDao = DaoManager.createDao(connectionSource, JobExecutionEvent.class);
		}
		return jobExecutionEventDao;
	}

	private void setupTables(final ConnectionSource connectionSource) throws SQLException {
		TableUtils.createTableIfNotExists(connectionSource, Job.class);
		TableUtils.createTableIfNotExists(connectionSource, JobExecutionEvent.class);
	}

}
