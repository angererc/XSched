package testclasses;

import xsched.TaskMethod;
import xsched.Task;

public class ClassUsingTasks {

	public void TaskA() {
		Task<Void> t;
		this.TaskB((t = new Task<Void>()), "hello world");
		
	}
	
	@TaskMethod
	public void TaskB(Task<Void> now, String s) {
		System.out.println(s);
	}
}
