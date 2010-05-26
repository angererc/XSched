package xsched.analysis.heap;

import java.util.ArrayList;
import java.util.List;

import xsched.analysis.schedule.CreationEdge;
import xsched.analysis.schedule.Schedule;
import xsched.analysis.schedule.ScheduleNode;

public class ParallelActivationsTree<Context> extends ActivationsTreeInnerNode<Context> {

	public ParallelActivationsTree(Context context) {
		super(context);		
	}

	@Override
	public List<CreationEdge<Context>> addCreationEdgesToSchedule(Schedule<Context> schedule, ScheduleNode<Context> creator) {
		int size = this.children.size();
		
		//TODO add a fast path here, for size == 1
		//a list of lists
		List<List<CreationEdge<Context>>> creationEdgesLists = new ArrayList<List<CreationEdge<Context>>>(size);
		//the flattened version
		List<CreationEdge<Context>> creationEdges = new ArrayList<CreationEdge<Context>>(this.children.size());
		
		//first, collect all lists of edges from our children
		for(ActivationsTree<Context> child : this.children) {
			List<CreationEdge<Context>> childEdges = child.addCreationEdgesToSchedule(schedule, creator);
			creationEdgesLists.add(childEdges);
			creationEdges.addAll(childEdges);
		}
		
		//now, add constraints between all of them
		assert(creationEdgesLists.size() == size);
		for(int i = 0; i < size-1; i++) {
			for(int j = i+1; j < size; j++) {
				for(CreationEdge<Context> first : creationEdgesLists.get(i)) {
					for(CreationEdge<Context> second : creationEdgesLists.get(j)) {
						first.isParallelTo(second);
						second.isParallelTo(first);
					}
				}
			}
		}
		
		return creationEdges;
	}

	
}
