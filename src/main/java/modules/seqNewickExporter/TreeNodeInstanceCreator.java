package modules.seqNewickExporter;

import java.lang.reflect.Type;

import com.google.gson.InstanceCreator;
import common.ParentRelationTreeNodeImpl;
import common.TreeNode;

public class TreeNodeInstanceCreator implements InstanceCreator <TreeNode> {
	
	@Override
	public TreeNode createInstance(Type type) {
		return new ParentRelationTreeNodeImpl(null);
	}
	
}