package xsched.analysis.core;
import java.util.HashMap;


public class ScheduleAnalysis<T, SS> {

	private HashMap<T, Task<T, SS>> tasks = new HashMap<T, Task<T, SS>>();
		
	public Task<T, SS> taskForID(T id) {
		Task<T, SS> result = tasks.get(id);
		if(result == null) {
			result = new Task<T, SS>(id);
			tasks.put(id, result);
		}
		return result;
	}
}
