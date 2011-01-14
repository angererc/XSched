package xsched.analysis.core;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


//this class collects the results of analyzing one task with respect to its formal parameters

public class FormalParameterResult<T, SS> {
	//per formal param we keep a pair of sets that keep tasks that are not ordered before the param and not ordered after the param
	private ArrayList<UnorderedTasksSets<T, SS>> parameters = new ArrayList<UnorderedTasksSets<T, SS>>();
		
	public FormalParameterResult(int numParameters) {
		for(int i = 0; i < numParameters; i++) {
			parameters.add(new UnorderedTasksSets<T, SS>(new HashSet<AnalysisTask<T, SS>>(), new HashSet<AnalysisTask<T, SS>>()));
		}
	}
	
	public void setNotOrderedBefore(int param, AnalysisTask<T, SS> task) {
		parameters.get(param).tasksNotOrderedBefore.add(task);
	}
	
	public void setTasksNotOrderedBefore(int param, Collection<AnalysisTask<T, SS>> tasks) {
		parameters.get(param).tasksNotOrderedBefore.addAll(tasks);
	}
	
	public void setNotOrderedAfter(int param, AnalysisTask<T, SS> task) {
		parameters.get(param).tasksNotOrderedAfter.add(task);
	}
	
	public void setTasksNotOrderedAfter(int param, Collection<AnalysisTask<T, SS>> tasks) {
		parameters.get(param).tasksNotOrderedAfter.addAll(tasks);
	}
	
	public Set<AnalysisTask<T, SS>> tasksNotOrderedBefore(int param) {
		return parameters.get(param).tasksNotOrderedBefore;
	}
	public Set<AnalysisTask<T, SS>> tasksNotOrderedAfter(int param) {
		return parameters.get(param).tasksNotOrderedAfter;
	}
	
	public void mergeWith(FormalParameterResult<T, SS> other) {
		assert parameters.size() == other.parameters.size();
		for(int i = 0; i < parameters.size(); i++) {
			UnorderedTasksSets<T, SS> myPair = this.parameters.get(i);
			UnorderedTasksSets<T, SS> otherPair = other.parameters.get(i);
			
			myPair.tasksNotOrderedBefore.addAll(otherPair.tasksNotOrderedBefore);
			myPair.tasksNotOrderedAfter.addAll(otherPair.tasksNotOrderedAfter);			
		}
	}
}
