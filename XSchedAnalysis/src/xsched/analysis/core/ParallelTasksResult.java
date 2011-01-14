package xsched.analysis.core;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

//this class contains the result of analyzing a task.
//for each task it contains other tasks that have been found to be potentially parallel
//if t1 is parallel to t2 then t2 is parallel to t1
public class ParallelTasksResult<T, SS> {
	private HashMap<AnalysisTask<T, SS>, HashSet<AnalysisTask<T, SS>>> parallelTasksMap =  new HashMap<AnalysisTask<T, SS>, HashSet<AnalysisTask<T, SS>>>();
	
	private void addIntoMap(AnalysisTask<T, SS> key, AnalysisTask<T, SS> value) {
		HashSet<AnalysisTask<T, SS>> set = parallelTasksMap.get(key);
		if(set == null) {
			set = new HashSet<AnalysisTask<T, SS>>();
			parallelTasksMap.put(key, set);
		}
		set.add(value);
	}
	
	public void mergeWith(ParallelTasksResult<T, SS> other) {
		parallelTasksMap.putAll(other.parallelTasksMap);
	}
	
	public void setParallel(AnalysisTask<T, SS> one, AnalysisTask<T, SS> other) {
		addIntoMap(one, other);
		addIntoMap(other, one);
	}
		
	public void setParallel(Collection<AnalysisTask<T, SS>> one, Collection<AnalysisTask<T, SS>> other) {
		for(AnalysisTask<T, SS> t1 : one) {
			for(AnalysisTask<T, SS> t2 : other) {
				setParallel(t1, t2);
			}
		}
	}
	
	public boolean isParallel(AnalysisTask<T, SS> one, AnalysisTask<T, SS> other) {
		HashSet<AnalysisTask<T, SS>> set = parallelTasksMap.get(one);
		if(set == null)
			return false;
		
		return set.contains(other);
	}
	
	public boolean isOrdered(AnalysisTask<T, SS> one, AnalysisTask<T, SS> other) {
		return ! isParallel(one, other);
	}
	
	public Set<AnalysisTask<T, SS>> parallelTasksFor(AnalysisTask<T, SS> task) {
		HashSet<AnalysisTask<T, SS>> set = parallelTasksMap.get(task);
		if(set == null)
			return Collections.emptySet();
		
		return set;
	}
}
