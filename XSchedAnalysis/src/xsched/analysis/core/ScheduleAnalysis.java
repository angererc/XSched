package xsched.analysis.core;
import java.util.Collection;
import java.util.HashMap;


public class ScheduleAnalysis<T, SS> {

	private HashMap<T, AnalysisTask<T, SS>> tasks = new HashMap<T, AnalysisTask<T, SS>>();
	
	public Collection<AnalysisTask<T, SS>> tasks() {
		return tasks.values();
	}
	
	public AnalysisTask<T, SS> taskForID(T id) {
		AnalysisTask<T, SS> result = tasks.get(id);
		if(result == null) {
			result = new AnalysisTask<T, SS>(id);
			tasks.put(id, result);
		}
		return result;
	}
}
