package xsched.analysis.core;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;


public class AnalysisSession<Instance, TV, SM extends TaskScheduleManager<TV>> {

	private HashMap<Instance, AnalysisTask<Instance, TV, SM>> tasks = new HashMap<Instance, AnalysisTask<Instance, TV, SM>>();
	
	public Collection<AnalysisTask<Instance, TV, SM>> tasks() {
		return tasks.values();
	}
	
	public AnalysisTask<Instance, TV, SM> taskForID(Instance id) {
		AnalysisTask<Instance, TV, SM> result = tasks.get(id);
		assert result != null;		
		return result;
	}
	
	public Collection<AnalysisTask<Instance, TV, SM>> tasksWithIDs(Collection<Instance> ids) {
		HashSet<AnalysisTask<Instance, TV, SM>> result = new HashSet<AnalysisTask<Instance, TV, SM>>();
		for(Instance id : ids) {
			result.add(tasks.get(id));
		}
		return result;
	}
	
	public AnalysisTask<Instance, TV, SM> createTask(Instance id, TaskSchedule<TV, SM> schedule) {
		assert ! tasks.containsKey(id);
		AnalysisTask<Instance, TV, SM> result = new AnalysisTask<Instance, TV, SM>(this, id, schedule);
		tasks.put(id, result);
		return result;		
	}
}
