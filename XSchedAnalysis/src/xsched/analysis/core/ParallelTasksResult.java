package xsched.analysis.core;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

//this class contains the result of analyzing a task.
//for each task it contains other tasks that have been found to be potentially parallel
//if t1 is parallel to t2 then t2 is parallel to t1
public class ParallelTasksResult<Instance, TV, SS> {
	private HashMap<AnalysisTask<Instance, TV, SS>, HashSet<AnalysisTask<Instance, TV, SS>>> parallelTasksMap =  new HashMap<AnalysisTask<Instance, TV, SS>, HashSet<AnalysisTask<Instance, TV, SS>>>();
	
	private void addIntoMap(AnalysisTask<Instance, TV, SS> key, AnalysisTask<Instance, TV, SS> value) {
		HashSet<AnalysisTask<Instance, TV, SS>> set = parallelTasksMap.get(key);
		if(set == null) {
			set = new HashSet<AnalysisTask<Instance, TV, SS>>();
			parallelTasksMap.put(key, set);
		}
		set.add(value);
	}
	
	public void mergeWith(ParallelTasksResult<Instance, TV, SS> other) {
		parallelTasksMap.putAll(other.parallelTasksMap);
	}
	
	public void setParallel(AnalysisTask<Instance, TV, SS> one, AnalysisTask<Instance, TV, SS> other) {
		addIntoMap(one, other);
		addIntoMap(other, one);
	}
		
	public void setParallel(Collection<AnalysisTask<Instance, TV, SS>> one, Collection<AnalysisTask<Instance, TV, SS>> other) {
		for(AnalysisTask<Instance, TV, SS> t1 : one) {
			for(AnalysisTask<Instance, TV, SS> t2 : other) {
				setParallel(t1, t2);
			}
		}
	}
	
	public boolean isParallel(AnalysisTask<Instance, TV, SS> one, AnalysisTask<Instance, TV, SS> other) {
		HashSet<AnalysisTask<Instance, TV, SS>> set = parallelTasksMap.get(one);
		if(set == null)
			return false;
		
		return set.contains(other);
	}
	
	public boolean isOrdered(AnalysisTask<Instance, TV, SS> one, AnalysisTask<Instance, TV, SS> other) {
		return ! isParallel(one, other);
	}
	
	public Set<AnalysisTask<Instance, TV, SS>> parallelTasksFor(AnalysisTask<Instance, TV, SS> task) {
		HashSet<AnalysisTask<Instance, TV, SS>> set = parallelTasksMap.get(task);
		if(set == null)
			return Collections.emptySet();
		
		return set;
	}
}
