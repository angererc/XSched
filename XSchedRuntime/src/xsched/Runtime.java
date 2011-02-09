package xsched;

public class Runtime {

	public static void scheduleMainTask(Object receiver, String taskName, Object[] args) {
		assert taskName.startsWith(Task.MainTaskMethodPrefix);
		//System.out.println("schedule main task called: " + receiver + "." + taskName + "(" + args + ")");
		Task<?> task = (Task<?>)args[0];
		task.scheduleMainTask(receiver, taskName, args);
	}
	
	public static void scheduleNormalTask(Object receiver, String taskName, Object[] args) {
		assert taskName.startsWith(Task.NormalTaskMethodPrefix);
		//System.out.println("schedule normal task called: " + receiver + "." + taskName + "(" + args + ")");
		Task<?> task = (Task<?>)args[0];
		task.scheduleNormalTask(receiver, taskName, args);
	}
}
