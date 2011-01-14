package xsched.analysis.core;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ScheduleSite<T, SS> extends TaskVariable<SS> {
	
	private HashSet<AnalysisTask<T, SS>> possibleTargetTasks = new HashSet<AnalysisTask<T, SS>>();
	private ArrayList<TaskVariable<?>> actualParameters = new ArrayList<TaskVariable<?>>();
	private Set<AnalysisTask<T, SS>> childrenCache;
	
	public enum Multiplicity {
		single, multipleUnordered, multipleOrdered;
	}
	public final Multiplicity multiplicity;
	
	ScheduleSite(SS id, Multiplicity multiplicity) {
		super(id);
		this.multiplicity = multiplicity;
	}
	
	public void addPossibleTaskTarget(AnalysisTask<T, SS> task) {
		possibleTargetTasks.add(task);
	}
	
	public Set<AnalysisTask<T, SS>> possibleTargetTasks() {
		assert possibleTargetTasks.size() > 0;
		return possibleTargetTasks;
	}
	
	public void addActualParameter(TaskVariable<?> tVar) {
		actualParameters.add(tVar);
	}
	
	public TaskVariable<?> actualParameter(int i) {
		return actualParameters.get(i);
	}
	
	public ArrayList<TaskVariable<?>> actualParameters() {
		return actualParameters;
	}
	
	public int numActualParameters() {
		return actualParameters.size();
	}
	
	public Set<AnalysisTask<T, SS>> children() {
		if(childrenCache != null)
			return childrenCache;
		
		HashSet<AnalysisTask<T, SS>> allChildren = new HashSet<AnalysisTask<T, SS>>();
		
		for(AnalysisTask<T, SS> task : possibleTargetTasks) {
			allChildren.addAll(task.children());
		}
		
		childrenCache = allChildren;
		return allChildren;
		
	}
	
	@Override
	public boolean isOrderedWith(TaskVariable<?> after) {
		if(this.equals(after)) {
			return multiplicity == Multiplicity.single || multiplicity == Multiplicity.multipleOrdered;
		} else {
			return super.isOrderedWith(after);
		}
	}
	
	@Override
	public String toString() {
		return id + " = schedule <" + possibleTargetTasks + ">(" + actualParameters + ")";
	}
}
