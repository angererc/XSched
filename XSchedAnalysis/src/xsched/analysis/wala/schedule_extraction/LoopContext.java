package xsched.analysis.wala.schedule_extraction;

import java.util.HashSet;
import java.util.Set;

public class LoopContext {
	
	private final Set<BackEdgeFlowData> backEdges = new HashSet<BackEdgeFlowData>();
	
	LoopContext contextByAddingLoop(BackEdgeFlowData backEdge) {
		if(backEdges.contains(backEdge))
			return this;
		
		LoopContext lc = new LoopContext();
		lc.backEdges.addAll(backEdges);
		lc.backEdges.add(backEdge);
		
		return lc;
	}
	
	@Override
	public boolean equals(Object otherObj) {
		return (otherObj == this) || (otherObj instanceof LoopContext && ((LoopContext)otherObj).backEdges.equals(backEdges));
	}

	@Override
	public int hashCode() {
		return backEdges.hashCode();
	}
	
	@Override
	public String toString() {
		return "{" + backEdges.toString() + "}";
	}
}

