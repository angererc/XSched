package xsched.analysis.schedule;

import java.util.List;

import xsched.analysis.heap.ActivationsTree;
import xsched.analysis.heap.NewHBRelationshipRecord;

public interface Heap<Context> {

	public void prepare(Schedule<Context> schedule, ScheduleNode<Context> now, P2Set<Context> receivers, List<P2Set<Context>> params);
	public void analyze();
	
	public ActivationsTree<Context> newActivations();
	public List<NewHBRelationshipRecord<Context>> newHBRelationshipRecords();
	
	public void freeze();
	public void dump(String filename);
	
	public Heap<Context> merge(Heap<Context> other);
	public Heap<Context> zip(Heap<Context> other);
	
}
