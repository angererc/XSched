package xsched.analysis.core;
import java.util.HashSet;


public class UnorderedTasksSets<Instance, TV, SS> {

	public final HashSet<AnalysisTask<Instance, TV, SS>> tasksNotOrderedBefore;
	public final HashSet<AnalysisTask<Instance, TV, SS>> tasksNotOrderedAfter;
	
	public UnorderedTasksSets(HashSet<AnalysisTask<Instance, TV, SS>> tasksNotOrderedBefore,
			HashSet<AnalysisTask<Instance, TV, SS>> tasksNotOrderedAfter) {
		this.tasksNotOrderedBefore = tasksNotOrderedBefore;
		this.tasksNotOrderedAfter = tasksNotOrderedAfter;
	}

}
