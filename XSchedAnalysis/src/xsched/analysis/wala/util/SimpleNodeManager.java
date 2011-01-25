package xsched.analysis.wala.util;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

import com.ibm.wala.util.graph.NumberedNodeManager;
import com.ibm.wala.util.intset.IntIterator;
import com.ibm.wala.util.intset.IntSet;

public class SimpleNodeManager<T> implements NumberedNodeManager<T> {

	final private ArrayList<T> nodes;

	public SimpleNodeManager() {
		super();
		nodes = new ArrayList<T>();
	}

	public void addAllNodes(SimpleNodeManager<T> other) {
		for(T n : other.nodes) {
			addNode(n);
		}	
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
		if(! nodes.contains(n))
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
		if(nodes.size() != other.nodes.size())
			return false;
		
		for(T n : nodes) {
			if(! other.nodes.contains(n))
				return false;
		}
		
		return true;
	}

	@Override
	public String toString() {
		return nodes.toString();
	}

	@Override
	public int getMaxNumber() {
		return nodes.size()-1;
	}

	@Override
	public T getNode(int number) {
		return nodes.get(number);
	}

	@Override
	public int getNumber(T N) {
		return nodes.indexOf(N);
	}

	@Override
	public Iterator<T> iterateNodes(final IntSet s) {
		return new Iterator<T>() {
			private final IntIterator it = s.intIterator();
			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public T next() {
				int next = it.next();
				return nodes.get(next);		
			}

			@Override
			public void remove() {
				throw new ConcurrentModificationException();
			}
			
		};
	}

}

