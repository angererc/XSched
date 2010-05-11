package xsched;

public class Activation<R> {

	public Activation(Object object, String taskName) {
		
	}
	
	public Activation(Object object, String taskName, Object param) {
		
	}
	
	public Activation(Object object, String taskName, Object param1, Object param2) {
		
	}
	
	public Activation(Object object, String taskName, Object param1, Object param2, Object param3) {
		
	}
	
	public Object object() {
		return null;
	}
	
	public String taskName() {
		return null;
	}
	
	public Object[] parameters() {
		return null;
	}
	
	public R result() {
		return null;
	}
	
	//scheduled in the future
	public synchronized boolean isInFuture() {
		return true;
	}
	
	//retain count == 0; it's in the thread pool but not yet executing
	public synchronized boolean isAboutToExecute() {
		return true;
	}
	
	//method is currently executing
	public synchronized boolean isExecuting() {
		return true;
	}
	
	//done executing and is retired now
	public synchronized boolean hasRetired() {
		return true;
	}
	
	public synchronized boolean comesBefore(Activation<?> later) {
		return false;
	}
	
	public synchronized void hb(Activation<?> later) {			
		
	}
}
