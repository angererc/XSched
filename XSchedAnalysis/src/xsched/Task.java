package xsched;

/*
 * this task "implementation" is only for the analysis side to make examples compile. To actually use tasks in a program, use the XSchedRuntime implementation
 * 
 * because we have to "trick" the points to analysis etc. we do not use Task.schedule(...) and then rewrite that for the analysis but do it the other way around
 * so we write in the code Task t = new Task(); obj.foo(t, otherTask, 42, "abc") and rewrite that into Task t = Task.schedule(obj, "foo", otherTask, 42, "abc").
 * 
 * the first parameter in a task method must always be the now object and on the caller site t must always be a "freshly created" task method
 * 
 */
public final class Task<R> {
	
	public Task() {
	}
	
	public R result() {
		return null;
	}
	
	public synchronized void hb(Task<R> later) {
	}

}
