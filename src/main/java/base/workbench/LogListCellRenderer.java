package base.workbench;

import java.awt.Color;
import java.awt.Component;
import java.util.logging.Level;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import common.PrettyLogRecord;

public class LogListCellRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = 600779110245379697L;

	@Override
	public Component getListCellRendererComponent(JList<? extends Object> list,
			Object value, int index, boolean isSelected, boolean cellHasFocus) {
		Component c = super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
		if (PrettyLogRecord.class.isAssignableFrom(value.getClass())){
			PrettyLogRecord logRecord = (PrettyLogRecord) value;
			Level level = logRecord.getLogRecord().getLevel();
			if (level == Level.INFO){
				//c.setBackground(Color.GREEN);
			}
			else if (level == Level.WARNING)
				c.setBackground(Color.YELLOW);
			else if (level == Level.SEVERE)
				c.setBackground(Color.RED);
		}
		return c;
	}

}
