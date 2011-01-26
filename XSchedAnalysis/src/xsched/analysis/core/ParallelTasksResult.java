package xsched.analysis.core;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

//this class contains the result of analyzing a task.
//for each task it contains other tasks that have been found to be potentially parallel
//if t1 is parallel to t2 then t2 is parallel to t1
public class ParallelTasksResult<Instance, TV, SM extends TaskScheduleManager<TV>> {
	private HashMap<AnalysisTask<Instance, TV, SM>, HashSet<AnalysisTask<Instance, TV, SM>>> parallelTasksMap =  new HashMap<AnalysisTask<Instance, TV, SM>, HashSet<AnalysisTask<Instance, TV, SM>>>();
	
	private void addIntoMap(AnalysisTask<Instance, TV, SM> key, AnalysisTask<Instance, TV, SM> value) {
		HashSet<AnalysisTask<Instance, TV, SM>> set = parallelTasksMap.get(key);
		if(set == null) {
			set = new HashSet<AnalysisTask<Instance, TV, SM>>();
			parallelTasksMap.put(key, set);
		}
		set.add(value);
	}
	
	public void mergeWith(ParallelTasksResult<Instance, TV, SM> other) {
		for(Entry<AnalysisTask<Instance, TV, SM>, HashSet<AnalysisTask<Instance, TV, SM>>> entry : other.parallelTasksMap.entrySet()) {
			HashSet<AnalysisTask<Instance, TV, SM>> mySet = parallelTasksMap.get(entry.getKey());
			if(mySet == null) {
				parallelTasksMap.put(entry.getKey(), new HashSet<AnalysisTask<Instance, TV, SM>>(entry.getValue()));
			} else {
				mySet.addAll(entry.getValue());
			}
		}
	}
	
	public void setParallel(AnalysisTask<Instance, TV, SM> one, AnalysisTask<Instance, TV, SM> other) {
		addIntoMap(one, other);
		addIntoMap(other, one);
	}
		
	public void setParallel(Collection<AnalysisTask<Instance, TV, SM>> one, Collection<AnalysisTask<Instance, TV, SM>> other) {
		for(AnalysisTask<Instance, TV, SM> t1 : one) {
			for(AnalysisTask<Instance, TV, SM> t2 : other) {
				setParallel(t1, t2);
			}
		}
	}
	
	public void setParallel(AnalysisTask<Instance, TV, SM> one, Collection<AnalysisTask<Instance, TV, SM>> others) {
		for(AnalysisTask<Instance, TV, SM> other : others) {
			setParallel(one, other);
		}
	}
	
	public boolean isParallel(AnalysisTask<Instance, TV, SM> one, AnalysisTask<Instance, TV, SM> other) {
		HashSet<AnalysisTask<Instance, TV, SM>> set = parallelTasksMap.get(one);
		if(set == null)
			return false;
		
		return set.contains(other);
	}
	
	public boolean isOrdered(AnalysisTask<Instance, TV, SM> one, AnalysisTask<Instance, TV, SM> other) {
		return ! isParallel(one, other);
	}
	
	public Set<AnalysisTask<Instance, TV, SM>> parallelTasksFor(AnalysisTask<Instance, TV, SM> task) {
		HashSet<AnalysisTask<Instance, TV, SM>> set = parallelTasksMap.get(task);
		if(set == null)
			return Collections.emptySet();
		
		return set;
	}
}
