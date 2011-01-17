package xsched.analysis.wala;

import java.util.HashSet;
import java.util.Set;

import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.traverse.SCCIterator;

public class LoopFinder<T> {
	
	public static interface NodeFilter<T> {
		//if you return false, a scc that contains this node will not count as an SCC! so it brakes up the SCC-nes
		public boolean ignoreNode(T node);
	}
	
	private HashSet<T> loopingElements = new HashSet<T>();
	
	//filter can be null; when filter returns true for any node in an scc, a SCC will not count as an scc
	public LoopFinder(Graph<T> graph, NodeFilter<T> filter) {
		SCCIterator<T> it = new SCCIterator<T>(graph);
		scc_loop: 
			while(it.hasNext()) {
				Set<T> ts = it.next();
				if(ts.size() > 0) {
					if(filter != null) {
						for(T node : ts) {
							if(filter.ignoreNode(node))
								continue scc_loop;
						}
					}
					loopingElements.addAll(ts);
				}
			}
	}
	
	public boolean isInLoop(T node) {
		return loopingElements.contains(node);
	}
}
