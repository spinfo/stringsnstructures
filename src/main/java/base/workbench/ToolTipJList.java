package base.workbench;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Iterator;

import javax.swing.JList;

import modules.Module;

public class ToolTipJList<E> extends JList<Module> {
	private static final long serialVersionUID = -7868066009909531086L;

	public ToolTipJList(Module[] array) {
		super(array);
	}

	public String getToolTipText(MouseEvent event) {
	    Point p = event.getPoint();
	    int location = locationToIndex(p);
	    Module module = (Module) getModel().getElementAt(location);
	    StringBuffer tip = new StringBuffer();
	    
	    tip.append("<html><h3>");
	    tip.append(module.toString()+"<h3>");
	    tip.append("<p>"+insertNewlines(module.getDescription(), 50)+"<p>");
	    tip.append("<ul><li>Inputs:</li><ul>");
	    
	    Iterator<String> inputNames = module.getInputPorts().keySet().iterator();
	    while (inputNames.hasNext()){
	    	String inputName = inputNames.next();
	    	tip.append("<li>"+inputName+" ("+module.getInputPorts().get(inputName).getClass().getSimpleName()+")</li>");
	    }
	    tip.append("</ul><li>Outputs:</li><ul>");
	    Iterator<String> outputNames = module.getOutputPorts().keySet().iterator();
	    while (outputNames.hasNext()){
	    	String outputName = outputNames.next();
	    	tip.append("<li>"+outputName+" ("+module.getOutputPorts().get(outputName).getClass().getSimpleName()+")</li>");
	    }
	    tip.append("</ul></ul></html>");
	    
	    return tip.toString();
	}
	
	private String insertNewlines(String eingabe, int umbruchNach){
		StringBuffer returnValue = new StringBuffer();
		
		for (int anfang = 0; anfang<eingabe.length(); anfang+=umbruchNach){
			int ende = anfang+umbruchNach;
			if (ende>=eingabe.length())
				ende = eingabe.length();
			returnValue.append(eingabe.substring(anfang, ende)+"<br>");
		}
		return returnValue.toString();
	}
	
}
