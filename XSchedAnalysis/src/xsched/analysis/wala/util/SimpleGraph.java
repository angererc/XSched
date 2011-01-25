package xsched.analysis.wala.util;

import java.util.Iterator;

import com.ibm.wala.util.graph.AbstractNumberedGraph;
import com.ibm.wala.util.graph.EdgeManager;
import com.ibm.wala.util.graph.NodeManager;
import com.ibm.wala.util.graph.impl.SparseNumberedEdgeManager;
import com.ibm.wala.util.intset.IntSet;

public class SimpleGraph<T> extends AbstractNumberedGraph<T> {
	private final SimpleNodeManager<T> nodeManager;
	private final SparseNumberedEdgeManager<T> edgeManager;
	
	public SimpleGraph() {
		super();
		nodeManager = new SimpleNodeManager<T>();
		edgeManager = new SparseNumberedEdgeManager<T>(nodeManager);
	}
	
	public void addAllNodesAndEdges(SimpleGraph<T> other) {	
		this.nodeManager.addAllNodes(other.nodeManager);
		
		Iterator<T> nodes = other.iterator();
		while(nodes.hasNext()) {
			T node = nodes.next();
			Iterator<T> succs = other.edgeManager.getSuccNodes(node);
			while(succs.hasNext()) {
				T succ = succs.next();
				edgeManager.addEdge(node, succ);
			}
		}	
	}
	
	@Override
	protected EdgeManager<T> getEdgeManager() {
		return edgeManager;
	}

	@Override
	protected NodeManager<T> getNodeManager() {
		return nodeManager;
	}
	
	public boolean stateEquals(SimpleGraph<T> other) {
		if (! this.nodeManager.stateEquals(other.nodeManager))
			return false;
		
		Iterator<T> nodes = this.iterator();
		while(nodes.hasNext()) {
			T node = nodes.next();
			IntSet otherSuccs = other.edgeManager.getSuccNodeNumbers(node);
			IntSet mySuccs = edgeManager.getSuccNodeNumbers(node);
			
			if(mySuccs == null && otherSuccs != null) 
				return false;
			else if(otherSuccs == null && mySuccs != null)
				return false;
			else if(mySuccs != null && otherSuccs != null && ! mySuccs.sameValue(otherSuccs))
				return false;
			
		}
		
		return true;
	}

	public String edgesToString() {
		return edgeManager.toString();
	}
	
	public String nodesToString() {
		return nodeManager.toString();
	}
}
