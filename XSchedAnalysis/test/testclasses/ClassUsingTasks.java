package testclasses;

import xsched.Task;

public class ClassUsingTasks {

	private boolean random() {
		return System.currentTimeMillis() % 8 == 0;
	}
	
	public void xschedTask_A1(Task<Void> now) {
		//simple linked list with t1->t2 and t2->t2
		Task<Void> t1;
		this.xschedTask_B((t1 = new Task<Void>()));
		
		while(random()) {
			Task<Void> t2;
			this.xschedTask_B((t2 = new Task<Void>()));
			t1.hb(t2);
			t1 = t2;
		}
		
	}

	public void xschedTask_A2(Task<Void> now) {
		//since t1_1 and t1_2 are exclusive, we can say that t1_1->t2 and t1_2->t2
		Task<Void> t1;
		if(random()) {
			this.xschedTask_B((t1 = new Task<Void>()));
		} else {
			this.xschedTask_B((t1 = new Task<Void>()));
		}
		
		Task<Void> t2;
		this.xschedTask_B((t2 = new Task<Void>()));
		t1.hb(t2);		
	}
	
	public void xschedTask_A3(Task<Void> now) {
		//shouldn't result in any hb relationships because exception is handled and
		//if something happens before t1->t2 we have two scheduled tasks but no hb relationship
		Task<Void> t1;
		try {
			this.xschedTask_B((t1 = new Task<Void>()));
			Task<Void> t2;
			this.xschedTask_B((t2 = new Task<Void>()));
			t1.hb(t2);
		} catch(Exception e) {
			//
		}				
	}
	
	public void xschedTask_B(Task<Void> now) {
		System.out.println("Hello World!");
	}
	
	public static void main(String[] args) {
		ClassUsingTasks c = new ClassUsingTasks();
		c.xschedTask_A1(new Task<Void>());
	}
}
