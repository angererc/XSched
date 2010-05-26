package xsched.analysis.heap;

import java.util.List;

import xsched.analysis.schedule.CreationEdge;
import xsched.analysis.schedule.Schedule;
import xsched.analysis.schedule.ScheduleNode;

public abstract class ActivationsTree<Context> {
	public final Context context;
	
	protected ActivationsTree(Context context) {
		this.context = context;
	}
	
	public abstract List<CreationEdge<Context>> addCreationEdgesToSchedule(Schedule<Context> schedule, ScheduleNode<Context> creator);
}
