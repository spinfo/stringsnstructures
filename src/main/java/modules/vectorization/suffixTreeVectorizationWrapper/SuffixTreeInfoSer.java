package modules.vectorization.suffixTreeVectorizationWrapper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

import modules.tree_building.suffixTreeClustering.data.Node;
import modules.tree_building.suffixTreeClustering.data.Type;
import modules.tree_building.suffixTreeClustering.st_interface.SuffixTreeInfo;

public class SuffixTreeInfoSer extends SuffixTreeInfo implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8236750034195294907L;
	
	private List<Node> thisNodes;

	public SuffixTreeInfoSer () {
		// Use the super constructor to initialize the standard instance variables;
		// see "modules.suffixTreeClustering.st_interface.SuffixTreeInfo.java".
		super();
		
		this.setNumberOfNodes(0);
		this.setNumberOfTypes(0);
	}
	
	// This method creates the necessary Node[] attribute so that proper serialization is possible.
	public void convertNodes(List<Node> nList) {
		for (Node i : nList) {
			this.addNode(i);
		}
	}
	private void writeObject(ObjectOutputStream out) throws IOException{
		out.writeInt(super.getNumberOfTypes());
		out.writeInt(super.getNumberOfNodes());
		out.writeObject(super.getTypes());
		this.thisNodes = super.getNodes();
		out.writeObject(this.thisNodes);
		
	}
	
	//TODO: This can not be an acceptable method. I have to ensure that the cast will be checked in the future.
	@SuppressWarnings("unchecked")
	private void readObject (ObjectInputStream in) throws IOException {
		super.setNumberOfTypes(in.readInt());
		super.setNumberOfNodes(in.readInt());
		try {
			super.setTypes((Set<Type>) in.readObject());
		} catch (ClassNotFoundException e) {
			System.err.println("Class Type not found.");
			e.printStackTrace();
		}
		try {
			this.thisNodes = (List<Node>) in.readObject();
		} catch (ClassNotFoundException e) {
			System.err.println("Class Type not found.");
			e.printStackTrace();
		}
		this.convertNodes(this.thisNodes);
		
		
	}

}
