package base.web;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;

class HealthInformationDao {

	// old health information objects are regularly deleted after this threshold
	// (three days)
	private static final int SECONDS_UNTIL_DELETION = 3 * 24 * 60 * 60;

	protected static long create(HealthInformation healthInfo) throws SQLException {
		synchronized (DatabaseFacade.GLOBAL_LOCK) {
			// before creating a new health information delete old ones
			deleteOldInfos();
			return dao().create(healthInfo);
		}
	}

	protected static List<HealthInformation> fetchLatest(long limit, long offset) throws SQLException {
		synchronized (DatabaseFacade.GLOBAL_LOCK) {
			return dao().queryBuilder().orderBy("collectedAt", false).limit(limit).offset(offset).query();
		}
	}
	
	private static int deleteOldInfos() throws SQLException {
		Timestamp thresholdTime = Timestamp.from(Instant.now().minusSeconds(SECONDS_UNTIL_DELETION));

		DeleteBuilder<HealthInformation, Long> builder = dao().deleteBuilder();
		builder.where().le("collectedAt", thresholdTime);

		synchronized (DatabaseFacade.GLOBAL_LOCK) {
			return builder.delete();
		}
	}

	private static Dao<HealthInformation, Long> dao() throws SQLException {
		return DatabaseFacade.getInstance().getHealthInformationDao();
	}

}
