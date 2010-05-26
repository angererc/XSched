package xsched.analysis.schedule;

import java.util.ArrayList;
import java.util.List;
import xsched.analysis.heap.NewHBRelationshipRecord;

public class ActivationNode<Context> extends ScheduleNode<Context>
 {
	
	public final Context context;	
	public final Task task;
			
	ActivationNode(Context context, Task task) {		
		this.context = context;
		this.task = task;
	}
		
	@Override
	public void analyze(Schedule<Context> schedule) {
		
		//first, compute the incoming heap and the receivers/params
		Heap<Context> heap = this.combineIncomingHeaps(schedule);
		ReceiversParamsPair recAndParams = this.combineIncomingReceiversAndParams(schedule);
		
		//then, analyze the heap
		heap.prepare(schedule, this, recAndParams.receivers, recAndParams.params);
		heap.analyze();
		heap.freeze();
		heap.dump("after.dot");
		
		//remember the result
		schedule.setResultHeap(this, heap);
		
		//handle new activations and creation edges
		heap.newActivations().addCreationEdgesToSchedule(schedule, this);
				
		for(NewHBRelationshipRecord<Context> hbRecord : heap.newHBRelationshipRecords()) {
			createHBRelationships(schedule, hbRecord);			
		}
		
		//re-analyze our outgoing nodes
		schedule.nodeChanged(this);
	}
	
	private void createHBRelationships(Schedule<Context> schedule, NewHBRelationshipRecord<Context> record) {
		List<Context> lhsds = record.lhs.contents();
		List<Context> rhsds = record.rhs.contents();
		
		for(Context lhs : lhsds) {
			for(Context rhs : rhsds) {
				ActivationNode<Context> lhsScheduleNode = schedule.activationNodeForContext(lhs);
				ActivationNode<Context> rhsScheduleNode = schedule.activationNodeForContext(rhs);
				schedule.addHappensBefore(lhsScheduleNode, rhsScheduleNode);
			}
		}
	}
	
	//all creation edges are exclusive because an activation can only be created once
	//therefore, we simply merge everything
	private ReceiversParamsPair combineIncomingReceiversAndParams(Schedule<Context> schedule) {
		//initialize new empty params array
		int numParams = task.numParams();
		
		P2Set<Context> receivers = schedule.factory.newP2Set();
		List<P2Set<Context>> params = new ArrayList<P2Set<Context>>(numParams);
		for(int i=0; i < numParams; i++) {
			params.add(schedule.factory.newP2Set());
		}
		
		//merge all the param sets from the incoming creation edges		
		for(CreationEdge<Context> creationEdge : schedule.incomingCreationEdges(this)) {
			receivers.merge(creationEdge.receivers);
			List<P2Set<Context>> someParams = creationEdge.params;
			assert(someParams.size() == numParams);
			for(int i=0; i < numParams; i++) {
				params.get(i).merge(someParams.get(i));
			}
		}
		
		return new ReceiversParamsPair(receivers, params);
	}
	
	private Heap<Context> combineIncomingHeaps(Schedule<Context> schedule) {
		Heap<Context> heap = schedule.factory.newHeap();
		//merge all the param sets from the incoming creation edges		
		for(CreationEdge<Context> creationEdge : schedule.incomingCreationEdges(this)) {
			heap.merge(schedule.resultHeapForNode(creationEdge.source));			
		}
		
		List<ScheduleNode<Context>> happensBeforeNode = schedule.incomingHBEdges(this);
		
		for(ScheduleNode<Context> inNode : happensBeforeNode) {
			//TODO: don't just merge, but zip if possible
			heap.merge(schedule.resultHeapForNode(inNode));
		}
		
		return heap;
	}
	
	//helper to return the receiver and the params 
	private class ReceiversParamsPair {
		public final P2Set<Context>receivers;
		public final List<P2Set<Context>> params;
		public ReceiversParamsPair(P2Set<Context> receivers, List<P2Set<Context>> params) {
			this.receivers = receivers;
			this.params = params;
		}
	}
}
