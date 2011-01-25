package xsched.analysis.core;
import java.util.Collection;
import java.util.HashMap;


public class AnalysisSchedule<Instance, TV, SS> {

	private HashMap<Instance, AnalysisTask<Instance, TV, SS>> tasks = new HashMap<Instance, AnalysisTask<Instance, TV, SS>>();
	
	public Collection<AnalysisTask<Instance, TV, SS>> tasks() {
		return tasks.values();
	}
	
	public AnalysisTask<Instance, TV, SS> taskForID(Instance id) {
		AnalysisTask<Instance, TV, SS> result = tasks.get(id);
		assert result != null;		
		return result;
	}
	
	public AnalysisTask<Instance, TV, SS> createTask(Instance id, TaskSchedule<TV, SS> schedule) {
		assert ! tasks.containsKey(id);
		AnalysisTask<Instance, TV, SS> result = new AnalysisTask<Instance, TV, SS>(id, schedule);
		tasks.put(id, result);
		return result;		
	}
}
