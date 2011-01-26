package xsched.analysis.core;
import java.util.HashSet;


public class UnorderedTasksSets<Instance, TV, SM extends TaskScheduleManager<TV>> {

	public final HashSet<AnalysisTask<Instance, TV, SM>> tasksNotOrderedBefore;
	public final HashSet<AnalysisTask<Instance, TV, SM>> tasksNotOrderedAfter;
	
	public UnorderedTasksSets(HashSet<AnalysisTask<Instance, TV, SM>> tasksNotOrderedBefore,
			HashSet<AnalysisTask<Instance, TV, SM>> tasksNotOrderedAfter) {
		this.tasksNotOrderedBefore = tasksNotOrderedBefore;
		this.tasksNotOrderedAfter = tasksNotOrderedAfter;
	}

}
