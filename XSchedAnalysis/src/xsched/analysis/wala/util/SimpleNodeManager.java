package xsched.analysis.wala.util;

import java.util.HashSet;
import java.util.Iterator;

import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.graph.NodeManager;

public class SimpleNodeManager<T> implements NodeManager<T> {

	  final private HashSet<T> nodes;

	  public SimpleNodeManager() {
		  super();
		  nodes = HashSetFactory.make();
	  }
	  
	  public SimpleNodeManager(SimpleNodeManager<T> other) {
		  super();
		  nodes = HashSetFactory.make(other.nodes);
	  }
	  
	  public void addAllNodes(SimpleNodeManager<T> other) {
		  this.nodes.addAll(other.nodes);
	  }
	  
	  public Iterator<T> iterator() {
	    return nodes.iterator();
	  }

	  /*
	   * @see com.ibm.wala.util.graph.NodeManager#getNumberOfNodes()
	   */
	  public int getNumberOfNodes() {
	    return nodes.size();
	  }

	  /*
	   * @see com.ibm.wala.util.graph.NodeManager#addNode(com.ibm.wala.util.graph.Node)
	   */
	  public void addNode(T n) {
	    nodes.add(n);
	  }

	  /*
	   * @see com.ibm.wala.util.graph.NodeManager#remove(com.ibm.wala.util.graph.Node)
	   */
	  public void removeNode(T n) {
	    nodes.remove(n);
	  }

	  /*
	   * @see com.ibm.wala.util.graph.NodeManager#containsNode(com.ibm.wala.util.graph.Node)
	   */
	  public boolean containsNode(T N) {
	    return nodes.contains(N);
	  }
	  
	  public boolean stateEquals(SimpleNodeManager<T> other) {
		  return this.nodes.equals(other.nodes);
	  }
	  
	  @Override
	  public String toString() {
		  return nodes.toString();
	  }

	}

