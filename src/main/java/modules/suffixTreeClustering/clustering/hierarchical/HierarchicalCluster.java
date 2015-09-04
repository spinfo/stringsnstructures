package modules.suffixTreeClustering.clustering.hierarchical;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import modules.suffixTreeClustering.data.Type;

public class HierarchicalCluster {

	private int age;
	private static int nextAge;

	private List<Type> allTypes = new LinkedList<Type>();

	private HierarchicalCluster left;
	private HierarchicalCluster right;
	private Set<String> topics;

	/**
	 * Constructor for a Cluster.
	 * @param t - Type (Document) to be assigned to that cluster.
	 */
	public HierarchicalCluster(Type t) {
		allTypes.add(t);
		this.setAge(nextAge++);
		this.topics = new HashSet<String>();
	}

	/**
	 * Constructor. Builds Cluster containing 2 other clusters as children.
	 * @param left - 'left' child
	 * @param right - 'right' child
	 */
	public HierarchicalCluster(HierarchicalCluster left, HierarchicalCluster right) {
		this.left = left;
		this.right = right;
		allTypes.addAll(left.allTypes);
		allTypes.addAll(right.allTypes);
		this.setAge(nextAge++);
		this.topics = new HashSet<String>();
		this.topics.addAll(left.topics);
		this.topics.addAll(right.topics);
	}

	public HierarchicalCluster getLeftChild() {
		return left;
	}

	public HierarchicalCluster getRightChild() {
		return right;
	}

	public List<Type> getAllTypes() {
		return allTypes;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < allTypes.size(); i++) {
			sb.append(allTypes.get(i).getID() + "|");
		}
		return sb.toString();
	}

	public void addTopic(String topic) {
		this.topics.add(topic);
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public Set<String> getTopics() {
		return topics;
	}
}
