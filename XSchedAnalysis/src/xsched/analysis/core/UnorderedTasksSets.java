package xsched.analysis.core;
import java.util.HashSet;


public class UnorderedTasksSets<T, SS> {

	public final HashSet<Task<T, SS>> tasksNotOrderedBefore;
	public final HashSet<Task<T, SS>> tasksNotOrderedAfter;
	
	public UnorderedTasksSets(HashSet<Task<T, SS>> tasksNotOrderedBefore,
			HashSet<Task<T, SS>> tasksNotOrderedAfter) {
		this.tasksNotOrderedBefore = tasksNotOrderedBefore;
		this.tasksNotOrderedAfter = tasksNotOrderedAfter;
	}

}
