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
	private HashMap<Task<T, SS>, HashSet<Task<T, SS>>> parallelTasksMap =  new HashMap<Task<T, SS>, HashSet<Task<T, SS>>>();
	
	private void addIntoMap(Task<T, SS> key, Task<T, SS> value) {
		HashSet<Task<T, SS>> set = parallelTasksMap.get(key);
		if(set == null) {
			set = new HashSet<Task<T, SS>>();
			parallelTasksMap.put(key, set);
		}
		set.add(value);
	}
	
	public void mergeWith(ParallelTasksResult<T, SS> other) {
		parallelTasksMap.putAll(other.parallelTasksMap);
	}
	
	public void setParallel(Task<T, SS> one, Task<T, SS> other) {
		addIntoMap(one, other);
		addIntoMap(other, one);
	}
		
	public void setParallel(Collection<Task<T, SS>> one, Collection<Task<T, SS>> other) {
		for(Task<T, SS> t1 : one) {
			for(Task<T, SS> t2 : other) {
				setParallel(t1, t2);
			}
		}
	}
	
	public boolean isParallel(Task<T, SS> one, Task<T, SS> other) {
		HashSet<Task<T, SS>> set = parallelTasksMap.get(one);
		if(set == null)
			return false;
		
		return set.contains(other);
	}
	
	public boolean isOrdered(Task<T, SS> one, Task<T, SS> other) {
		return ! isParallel(one, other);
	}
	
	public Set<Task<T, SS>> parallelTasksFor(Task<T, SS> task) {
		HashSet<Task<T, SS>> set = parallelTasksMap.get(task);
		if(set == null)
			return Collections.emptySet();
		
		return set;
	}
}
