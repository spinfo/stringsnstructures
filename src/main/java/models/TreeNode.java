package models;

import java.util.Map;

public interface TreeNode {
	
	public String getNodeValue();
	public int getNodeCounter();
	public Map<String,TreeNode> getChildNodes();

}
