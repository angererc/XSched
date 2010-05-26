package xsched.analysis.schedule;

import java.util.ArrayList;
import java.util.List;

/*
 * a creation edge always leads from the creator activation to the created activation.
 * two creation edges from the same source can be parallel or exclusive 
 */
public class CreationEdge<Context> {

	public final ScheduleNode<Context> source;
	public final ScheduleNode<Context> target;
		
	public final P2Set<Context> receivers;
	public final List<P2Set<Context>> params;
	private List<CreationEdge<Context>> exclusiveEdges;
	private List<CreationEdge<Context>> parallelEdges;
	
	CreationEdge(ScheduleNode<Context> source, ScheduleNode<Context> target, P2Set<Context> receivers, List<P2Set<Context>> params) {
		this.source = source;
		this.target = target;
		this.receivers = receivers;
		this.params = params;
	}
	
	public void isParallelTo(CreationEdge<Context> other) {
		assert(other.source == this.source);
		
		if(parallelEdges == null)
			parallelEdges = new ArrayList<CreationEdge<Context>>();
		
		parallelEdges.add(other);	
	}
	
	public void isExclusiveTo(CreationEdge<Context> other) {
		assert(other.source == this.source);
		if(exclusiveEdges == null)
			exclusiveEdges = new ArrayList<CreationEdge<Context>>();
		
		exclusiveEdges.add(other);
	}

}
