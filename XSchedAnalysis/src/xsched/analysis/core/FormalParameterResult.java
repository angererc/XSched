package xsched.analysis.core;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


//this class collects the results of analyzing one task with respect to its formal parameters

public class FormalParameterResult<Instance, TV, SM  extends TaskScheduleManager<TV>> {
	//per formal param we keep a pair of sets that keep tasks that are not ordered before the param and not ordered after the param
	private final ArrayList<UnorderedTasksSets<Instance, TV, SM>> parameters;
		
	public FormalParameterResult(int numParameters) {
		 parameters = new ArrayList<UnorderedTasksSets<Instance, TV, SM>>(numParameters);
		for(int i = 0; i < numParameters; i++) {
			parameters.add(new UnorderedTasksSets<Instance, TV, SM>(new HashSet<AnalysisTask<Instance, TV, SM>>(), new HashSet<AnalysisTask<Instance, TV, SM>>()));
		}
	}
	
	public int numTaskParameters() {
		return parameters.size();
	}
	
	public void setNotOrderedBefore(int param, AnalysisTask<Instance, TV, SM> task) {
		parameters.get(param).tasksNotOrderedBefore.add(task);
	}
	
	public void setTasksNotOrderedBefore(int param, Collection<AnalysisTask<Instance, TV, SM>> tasks) {
		parameters.get(param).tasksNotOrderedBefore.addAll(tasks);
	}
	
	public void setNotOrderedAfter(int param, AnalysisTask<Instance, TV, SM> task) {
		parameters.get(param).tasksNotOrderedAfter.add(task);
	}
	
	public void setTasksNotOrderedAfter(int param, Collection<AnalysisTask<Instance, TV, SM>> tasks) {
		parameters.get(param).tasksNotOrderedAfter.addAll(tasks);
	}
	
	public Set<AnalysisTask<Instance, TV, SM>> tasksNotOrderedBefore(int param) {
		return parameters.get(param).tasksNotOrderedBefore;
	}
	public Set<AnalysisTask<Instance, TV, SM>> tasksNotOrderedAfter(int param) {
		return parameters.get(param).tasksNotOrderedAfter;
	}
	
	public void mergeWith(FormalParameterResult<Instance, TV, SM> other) {
		assert parameters.size() == other.parameters.size();
		for(int i = 0; i < parameters.size(); i++) {
			UnorderedTasksSets<Instance, TV, SM> myPair = this.parameters.get(i);
			UnorderedTasksSets<Instance, TV, SM> otherPair = other.parameters.get(i);
			
			myPair.tasksNotOrderedBefore.addAll(otherPair.tasksNotOrderedBefore);
			myPair.tasksNotOrderedAfter.addAll(otherPair.tasksNotOrderedAfter);			
		}
	}
}
