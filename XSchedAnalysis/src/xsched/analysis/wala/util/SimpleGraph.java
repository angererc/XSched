package xsched.analysis.wala.util;

import com.ibm.wala.util.graph.AbstractNumberedGraph;
import com.ibm.wala.util.graph.EdgeManager;
import com.ibm.wala.util.graph.NodeManager;

public class SimpleGraph<T> extends AbstractNumberedGraph<T> {
	private final SimpleNodeManager<T> nodeManager;
	private final SimpleEdgeManager<T> edgeManager;
	
	public SimpleGraph() {
		super();
		nodeManager = new SimpleNodeManager<T>();
		edgeManager = new SimpleEdgeManager<T>();
	}
	
	public SimpleGraph(SimpleGraph<T> other) {
		super();
		nodeManager = new SimpleNodeManager<T>(other.nodeManager);
		edgeManager = new SimpleEdgeManager<T>(other.edgeManager);
	}
	
	public void addAllNodesAndEdges(SimpleGraph<T> other) {
		this.edgeManager.addAllEdges(other.edgeManager);
		this.nodeManager.addAllNodes(other.nodeManager);
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
		return this.nodeManager.stateEquals(other.nodeManager) && this.edgeManager.stateEquals(other.edgeManager);
	}

	public String edgesToString() {
		return edgeManager.toString();
	}
	
	public String nodesToString() {
		return nodeManager.toString();
	}
}
