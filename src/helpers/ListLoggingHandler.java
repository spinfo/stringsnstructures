package helpers;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import javax.swing.DefaultListModel;

public class ListLoggingHandler extends Handler {
	
	private DefaultListModel<PrettyLogRecord> listModel = null;

	public ListLoggingHandler() {
	}
	
	public ListLoggingHandler( DefaultListModel<PrettyLogRecord> listModel ) {
		this.listModel = listModel;
	}

	@Override
	public void publish(LogRecord record) {
		if (this.listModel != null)
			this.listModel.addElement(new PrettyLogRecord(record));
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() throws SecurityException {
	}

	/**
	 * @return the listModel
	 */
	public DefaultListModel<PrettyLogRecord> getListModel() {
		return listModel;
	}

	/**
	 * @param listModel the listModel to set
	 */
	public void setListModel(DefaultListModel<PrettyLogRecord> listModel) {
		this.listModel = listModel;
	}

}
