package helpers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.LogRecord;

/**
 * LogRecord wrapper with a nice toString method.
 * @author Marcel Boeing
 *
 */
public class PrettyLogRecord {
	private static final DateFormat DATEFORMAT = new SimpleDateFormat();
	private LogRecord logRecord;

	public PrettyLogRecord(LogRecord logRecord) {
		this.logRecord = logRecord;
	}

	/**
	 * @return the logRecord
	 */
	public LogRecord getLogRecord() {
		return logRecord;
	}

	/**
	 * @param logRecord the logRecord to set
	 */
	public void setLogRecord(LogRecord logRecord) {
		this.logRecord = logRecord;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer returnString = new StringBuffer();
		returnString.append(DATEFORMAT.format(new Date(this.logRecord.getMillis()))+" "+this.logRecord.getLevel().getLocalizedName()+" "+this.logRecord.getMessage());
		if (this.logRecord.getThrown() != null)
			returnString.append(" ("+this.logRecord.getThrown().getMessage()+")");
		return returnString.toString();
	}
	
	

}
