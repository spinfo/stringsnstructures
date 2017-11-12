package base.workbench;

class UserMessages {

	static final String WORKBENCH_GUI_STARTED = "Workbench GUI started.";
	
	static final String CLEAR_SEARCH_FIELD = "clears the search field, resetting the module list";
	static final String CLEAR_MODULE_TREE = "Clears the current module tree and creates a new one based on the selected module type.";
	static final String ADD_MODULE = "Adds a module as a child to the one currently selected in the tree.";
	static final String REMOVE_MODULE = "Removes the selected module (and all its children) from the tree.";
	static final String RUN_MODULES = "Starts the processing of the module tree.";
	static final String STOP_MODULES = "Stops the processing of the module tree.";
	static final String EDIT_MODULE_PROPERTIES = "Lets you edit or review the properties of the module that is currently chosen in the tree.";
	static final String SAVE_TO_FILE = "Lets you choose a file to save the module tree to.";
	static final String LOAD_FROM_FILE = "Lets you choose a file to load the module tree from.";
	
	static final String COULD_NOT_CLOSE_MODULE_FRAME = "Sorry, but I could not close this module frame.";
	static final String UNKNOWN_MODULE_TO_DELETE = "I'm afraid I don't know which module to delete -- there is none selected.";
	static final String UNKNOWN_MODULE_TO_EDIT = "Please select a module frame first.";
	static final String UNABLE_TO_DISPLAY_EDITOR = "Could not display editor dialogue.";
	static final String UNABLE_TO_RUN_MODULES = "Sorry, but I wasn't able to run the modules.";
	static final String UNABLE_TO_STOP_MODULES = "Sorry, but I wasn't able to stop the modules.";
	static final String UNABLE_TO_SAVE_MODULES = "Sorry, but I wasn't able to save the module tree.";
	static final String UNABLE_TO_LOAD_MODULES = "Sorry, but I wasn't able to load the module tree.";
	static final String UNKNOWN_MODULES_ROOT = "I could not find a good start for arranging the frames -- did you construct a loop? If so, please don't.";
	static final String UNABLE_TO_ADD_MODULE = "The module could not be added to the network.";
	static final String UNABLE_TO_REMOVE_MODULE = "Sorry, but the selected module could not be removed from the network.";
	static final String ERROR_WHEN_REMOVING_PORT_CONNECTION = "Sorry, but there was an error removing the port connection.";
	
	static String unableToOpenEditor(String moduleName) {
		return String.format("Could not open editor for module %s", moduleName);
	}
	
	static String unableToConnectPorts(String furtherMessage) {
		return String.format("Sorry, but I cannot connect those ports: %s", furtherMessage);
	}
	
	static String unknownCommand(String command) {
		return String.format("Sorry, but this command is unknown to me: %s", command);
	}
	
	static String unknownObjectClicked(Object object) {
		String objectString = (object == null ? "null" : object.getClass().getCanonicalName());
		return String.format("I just registered a mouse click for an object I do not know how to handle: %s", objectString);
	}
	
	static String moduleHasBeenRemoved(String moduleName) {
		return String.format("The module '%s' has been removed from the network.", moduleName);
	}
}
