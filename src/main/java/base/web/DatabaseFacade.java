package base.web;

import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DatabaseFacade {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseFacade.class);
	
	private ConnectionSource connectionSource;

	private static DatabaseFacade instance = null;
	
	private Dao<Job, Long> jobDao;
	
	private DatabaseFacade(final String jdbcUrl) {
		try {
			connectionSource = new JdbcConnectionSource(jdbcUrl);
			setupTables(connectionSource);
		} catch (SQLException e) {
			LOGGER.error("Could not setup db: " + e.getMessage());
		}
	}
	
	static DatabaseFacade getInstance() {
		if (DatabaseFacade.instance == null) {
			// TODO: Find a good place for the db
			int i = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
			instance = new DatabaseFacade("jdbc:sqlite:/tmp/jobs-" + i + ".db");
		}
		return instance;
	}
	
	protected Dao<Job, Long> jobDao() throws SQLException {
		if (jobDao == null) {
			jobDao = DaoManager.createDao(connectionSource, Job.class);
		}
		return jobDao;
	}
	
	
	private void setupTables(final ConnectionSource connectionSource) throws SQLException {
		TableUtils.createTableIfNotExists(connectionSource, Job.class);
		TableUtils.createTableIfNotExists(connectionSource, JobExecutionEvent.class);
	}
	
}
