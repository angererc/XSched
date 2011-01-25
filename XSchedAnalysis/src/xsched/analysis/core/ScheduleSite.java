package xsched.analysis.core;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ScheduleSite<Instance, TV, SS> extends TaskVariable<TV> {
	
	private HashSet<AnalysisTask<Instance, TV, SS>> possibleTargetTasks = new HashSet<AnalysisTask<Instance, TV, SS>>();
	private HashMap<Integer, TaskVariable<?>> actualParameters = new HashMap<Integer, TaskVariable<?>>();
	private Set<AnalysisTask<Instance, TV, SS>> childrenCache;
	
	public enum Multiplicity {
		single, multipleUnordered, multipleOrdered;
	}
	public final Multiplicity multiplicity;
	
	ScheduleSite(TV id, Multiplicity multiplicity) {
		super(id);
		this.multiplicity = multiplicity;
	}
	
	public void addPossibleTaskTarget(AnalysisTask<Instance, TV, SS> task) {
		possibleTargetTasks.add(task);
	}
	
	public Set<AnalysisTask<Instance, TV, SS>> possibleTargetTasks() {
		assert possibleTargetTasks.size() > 0;
		return possibleTargetTasks;
	}
	
	public void addActualParameter(int position, TaskVariable<?> tVar) {
		assert ! actualParameters.containsKey(position);
		actualParameters.put(position, tVar);
	}
	
	public TaskVariable<?> actualParameter(int i) {
		return actualParameters.get(i);
	}
	
	public HashMap<Integer, TaskVariable<?>> actualParameters() {
		return actualParameters;
	}
	
	public int numActualParameters() {
		return actualParameters.size();
	}
	
	public Set<AnalysisTask<Instance, TV, SS>> children() {
		if(childrenCache != null)
			return childrenCache;
		
		HashSet<AnalysisTask<Instance, TV, SS>> allChildren = new HashSet<AnalysisTask<Instance, TV, SS>>();
		
		for(AnalysisTask<Instance, TV, SS> task : possibleTargetTasks) {
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
