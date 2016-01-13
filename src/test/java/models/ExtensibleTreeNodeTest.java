package models;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ExtensibleTreeNodeTest {

	@Test
	public void serialisationTest() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		ExtensibleTreeNode node = new ExtensibleTreeNode();
		node.setNodeValue("1");
		node.setNodeCounter(5);
		node.getAttributes().put("doubleCounter", new Double(3));
		node.getAttributes().put("text", new String("this is the parent node"));

		ExtensibleTreeNode node2 = new ExtensibleTreeNode();
		node2.setNodeValue("1");
		node2.setNodeCounter(2);
		node2.getAttributes().put("doubleCounter", new Double(4));
		node2.getAttributes().put("text", new String("this is the child node"));
		
		node.getChildNodes().put("child", node2);
		
		String json = gson.toJson(node);
		
		ExtensibleTreeNode node3 = gson.fromJson(json, ExtensibleTreeNode.class);
		
		Assert.assertTrue(((Double)(node3.getChildNodes().get("child").getAttributes().get("doubleCounter"))).doubleValue() == 4d);
		Assert.assertTrue(node3.getChildNodes().get("child").getAttributes().get("doubleCounter").getClass().equals(Double.class));
	}
	
	

}
