package xsched.analysis.core;
import java.util.HashSet;


public class UnorderedTasksSets<T, SS> {

	public final HashSet<AnalysisTask<T, SS>> tasksNotOrderedBefore;
	public final HashSet<AnalysisTask<T, SS>> tasksNotOrderedAfter;
	
	public UnorderedTasksSets(HashSet<AnalysisTask<T, SS>> tasksNotOrderedBefore,
			HashSet<AnalysisTask<T, SS>> tasksNotOrderedAfter) {
		this.tasksNotOrderedBefore = tasksNotOrderedBefore;
		this.tasksNotOrderedAfter = tasksNotOrderedAfter;
	}

}
