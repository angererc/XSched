package xsched.analysis.core;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

//this class contains the result of analyzing a task.
//for each task it contains other tasks that have been found to be potentially parallel
//if t1 is parallel to t2 then t2 is parallel to t1
public class AnalysisResult<Instance> {
	private HashMap<Instance, HashSet<Instance>> parallelTasksMap =  new HashMap<Instance, HashSet<Instance>>();
	
	public AnalysisResult() {
		
	}
	
	public interface MappingOperation<Instance, T> {
		public T map(Instance i);
	}
	
	public <T> AnalysisResult<T> collapse(MappingOperation<Instance, T> mapper) {
		AnalysisResult<T> result = new AnalysisResult<T>();
		
		for(Entry<Instance, HashSet<Instance>> entry : parallelTasksMap.entrySet()) {
			T newID = mapper.map(entry.getKey());
			HashSet<T> newValues = result.parallelTasksMap.get(newID);
			if(newValues == null) {
				newValues = new HashSet<T>();
				result.parallelTasksMap.put(newID, newValues);
			}
			for(Instance val : entry.getValue()) {
				newValues.add(mapper.map(val));
			}
		}
		
		return result;
	}
		
	@Override
	public String toString() {
		return parallelTasksMap.toString();
	}
	
	public <TV, SM extends TaskScheduleManager<TV>> AnalysisResult(ParallelTasksResult<Instance, TV, SM> parResults) {
		for(Entry<AnalysisTask<Instance, TV, SM>, HashSet<AnalysisTask<Instance, TV, SM>>> entry : parResults.parallelTasksMap.entrySet()) {
			
			HashSet<Instance> mySet = parallelTasksMap.get(entry.getKey().id);
			if(mySet == null) {
				mySet = new HashSet<Instance>();
				parallelTasksMap.put(entry.getKey().id, mySet);
			}
			
			for(AnalysisTask<Instance, TV, SM> task : entry.getValue()) {
				mySet.add(task.id);
			}
		}
	}
	
	public void mergeWith(AnalysisResult<Instance> other) {
		for(Entry<Instance, HashSet<Instance>> entry : other.parallelTasksMap.entrySet()) {
			HashSet<Instance> mySet = parallelTasksMap.get(entry.getKey());
			if(mySet == null) {
				mySet = new HashSet<Instance>();
				parallelTasksMap.put(entry.getKey(), mySet);				
			}
			
			for(Instance task : entry.getValue()) {
				mySet.add(task);
			}
			
		}
	}
		
	public boolean isParallel(Instance one, Instance other) {
		HashSet<Instance> set = parallelTasksMap.get(one);
		if(set == null)
			return false;
		
		return set.contains(other);
	}
	
	public boolean isOrdered(Instance one, Instance other) {
		return ! isParallel(one, other);
	}
	
	public Set<Instance> parallelTasksFor(Instance task) {
		HashSet<Instance> set = parallelTasksMap.get(task);
		if(set == null)
			return Collections.emptySet();
		
		return set;
	}
}
