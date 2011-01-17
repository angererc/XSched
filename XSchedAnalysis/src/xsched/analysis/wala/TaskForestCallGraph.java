package xsched.analysis.wala;

import java.util.Collection;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.impl.PartialCallGraph;
import com.ibm.wala.util.collections.Filter;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.GraphSlicer;
import com.ibm.wala.util.graph.traverse.DFS;

//prunes the call graph such that root nodes are task method nodes and the call edges only point to non task methods
public class TaskForestCallGraph extends PartialCallGraph {

	public static TaskForestCallGraph make(final CallGraph cg, final Collection<CGNode> taskMethodNodes) {
		final Collection<CGNode> nodes = DFS.getReachableNodes(cg, taskMethodNodes, new Filter<CGNode>(){
			@Override
			public boolean accepts(CGNode o) {				
				return ! ScheduleInference.isTaskMethod(o.getMethod()); 				
			}			
		});
		Graph<CGNode> partialGraph = GraphSlicer.prune(cg, new Filter<CGNode>() {
			public boolean accepts(CGNode o) {
				return nodes.contains(o);
			}
		});

		return new TaskForestCallGraph(cg, taskMethodNodes, partialGraph);
	}

	protected TaskForestCallGraph(CallGraph cg,
			Collection<CGNode> partialRoots, Graph<CGNode> partialGraph) {
		super(cg, partialRoots, partialGraph);
		// TODO Auto-generated constructor stub
	}

}
