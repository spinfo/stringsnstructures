package common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import javax.swing.DefaultListModel;
import javax.swing.JList;

public class ListLoggingHandler extends Handler {
	
	private DefaultListModel<PrettyLogRecord> listModel = null;
	private List<JList<PrettyLogRecord>> autoScrollLists = new ArrayList<JList<PrettyLogRecord>>();

	public ListLoggingHandler() {
	}
	
	public ListLoggingHandler( DefaultListModel<PrettyLogRecord> listModel ) {
		this.listModel = listModel;
	}

	@Override
	public void publish(LogRecord record) {
		if (this.listModel != null){
			this.listModel.addElement(new PrettyLogRecord(record));
			Iterator<JList<PrettyLogRecord>> lists = autoScrollLists.iterator();
			while (lists.hasNext()){
				JList<PrettyLogRecord> list = lists.next();
				list.ensureIndexIsVisible(this.listModel.size()-1);
			}
			
		}
			
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() throws SecurityException {
	}

	/**
	 * @return the autoScrollLists
	 */
	public List<JList<PrettyLogRecord>> getAutoScrollLists() {
		return autoScrollLists;
	}

	/**
	 * @param autoScrollLists the autoScrollLists to set
	 */
	public void setAutoScrollLists(List<JList<PrettyLogRecord>> autoScrollLists) {
		this.autoScrollLists = autoScrollLists;
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
