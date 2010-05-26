/**
 * 
 */
package xsched.analysis.heap;

import java.util.ArrayList;
import java.util.List;

import xsched.analysis.schedule.ActivationNode;
import xsched.analysis.schedule.CreationEdge;
import xsched.analysis.schedule.P2Set;
import xsched.analysis.schedule.Schedule;
import xsched.analysis.schedule.ScheduleNode;
import xsched.analysis.schedule.Task;

public class NewActivationRecord<Context> extends ActivationsTree<Context> {
	
	public P2Set<Context> receivers;
	public Task task;
	public ArrayList<P2Set<Context>> params = new ArrayList<P2Set<Context>>();
	
	public NewActivationRecord(Context context) {
		super(context);
	}
	public String toString() {
		return "schedule declaration: " + context + " := " + receivers + "." + task + "(" + params + ")";
	}	
		
	@Override
	public List<CreationEdge<Context>> addCreationEdgesToSchedule(Schedule<Context> schedule, ScheduleNode<Context> creator) {
		ActivationNode<Context> newActivation = schedule.getOrCreateActivationNode(context, task);
		
		CreationEdge<Context> creationEdge = schedule.addCreationEdge(creator, newActivation, receivers, params);
		
		List<CreationEdge<Context>> result = new ArrayList<CreationEdge<Context>>();
		result.add(creationEdge);
		return result;
	}
	

}