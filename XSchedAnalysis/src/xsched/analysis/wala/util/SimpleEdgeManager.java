package xsched.analysis.wala.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.ibm.wala.util.collections.EmptyIterator;
import com.ibm.wala.util.graph.EdgeManager;

public class SimpleEdgeManager<T> implements EdgeManager<T> {
	private final Map<T, Set<T>> forwardEdges = new HashMap<T, Set<T>>();
	private final Map<T, Set<T>> backwardEdges = new HashMap<T, Set<T>>();
	
	public SimpleEdgeManager() {
		super();		
	}
	
	public SimpleEdgeManager(SimpleEdgeManager<T> other) {
		super();
		for(Entry<T, Set<T>> entry : other.forwardEdges.entrySet()) {
			forwardEdges.put(entry.getKey(), new HashSet<T>(entry.getValue()));
		}
		for(Entry<T, Set<T>> entry : other.backwardEdges.entrySet()) {
			backwardEdges.put(entry.getKey(), new HashSet<T>(entry.getValue()));
		}
	}
	
	public void addAllEdges(SimpleEdgeManager<T> other) {
		for(Entry<T, Set<T>> entry : other.forwardEdges.entrySet()) {
			Set<T> nodes = forwardEdges.get(entry.getKey());
			if(nodes == null) {
				nodes = new HashSet<T>();
				forwardEdges.put(entry.getKey(), nodes);
			}
			nodes.addAll(entry.getValue());
		}
		for(Entry<T, Set<T>> entry : other.backwardEdges.entrySet()) {
			Set<T> nodes = backwardEdges.get(entry.getKey());
			if(nodes == null) {
				nodes = new HashSet<T>();
				backwardEdges.put(entry.getKey(), nodes);
			}
			nodes.addAll(entry.getValue());
		}
	}
	
	@Override
	public void addEdge(T src, T dst) {
		Set<T> succs = forwardEdges.get(src);
		if(succs == null) {
			succs = new HashSet<T>();
			forwardEdges.put(src, succs);
		}
		succs.add(dst);
		
		Set<T> preds = backwardEdges.get(dst);
		if(preds == null) {
			preds = new HashSet<T>();
			backwardEdges.put(dst, preds);
		}
		preds.add(src);
	}

	@Override
	public int getPredNodeCount(T n) {
		if (backwardEdges.containsKey(n))
	          return ((Set<T>) backwardEdges.get(n)).size();
	        else
	          return 0;
	}

	@Override
	public Iterator<T> getPredNodes(T n) {
		if (backwardEdges.containsKey(n))
	          return backwardEdges.get(n).iterator();
	        else
	          return EmptyIterator.instance();
	}

	@Override
	public int getSuccNodeCount(T N) {
		if (forwardEdges.containsKey(N))
	          return ((Set<T>) forwardEdges.get(N)).size();
	        else
	          return 0;
	}

	@Override
	public Iterator<T> getSuccNodes(T n) {
		if (forwardEdges.containsKey(n))
	          return forwardEdges.get(n).iterator();
	        else
	          return EmptyIterator.instance();
	}

	@Override
	public boolean hasEdge(T src, T dst) {
		return forwardEdges.containsKey(src) && ((Set<T>) forwardEdges.get(src)).contains(dst);
	}

	@Override
	public void removeAllIncidentEdges(T node)
			throws UnsupportedOperationException {
		removeIncomingEdges(node);
		removeOutgoingEdges(node);
	}

	@Override
	public void removeEdge(T src, T dst)
			throws UnsupportedOperationException {
		Set<T> succs = forwardEdges.get(src);
		if(succs != null) {
			succs.remove(dst);
		}
		Set<T> preds = backwardEdges.get(dst);
		if(preds != null) {
			preds.remove(src);
		}
	}

	@Override
	public void removeIncomingEdges(T node)
			throws UnsupportedOperationException {
		Set<T> preds = backwardEdges.get(node);
		if(preds != null) {
			for(T pred : preds) {
				Set<T> succs = forwardEdges.get(pred);
				if(succs != null) {
					succs.remove(node);
				}
			}
			
			backwardEdges.remove(node);
		}
	}

	@Override
	public void removeOutgoingEdges(T node)
			throws UnsupportedOperationException {
		Set<T> succs = forwardEdges.get(node);
		if(succs != null) {
			for(T succ : succs) {
				Set<T> preds = backwardEdges.get(succ);
				if(preds != null) {
					preds.remove(node);
				}
			}
			
			forwardEdges.remove(node);
		}			
	}
	
	public boolean stateEquals(SimpleEdgeManager<T> other) {
		return this.backwardEdges.equals(other.backwardEdges) && this.forwardEdges.equals(other.forwardEdges);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for(Entry<T, Set<T>> edges : forwardEdges.entrySet()) {
			for(T rhs : edges.getValue()) {
				builder.append(" " + edges.getKey() + "->" + rhs + ";");
			}
		}
		return builder.toString();
	}
}