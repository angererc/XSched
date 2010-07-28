package xsched;

public class Activation<R> {

	public static <T> Activation<T> now() {
		throw new RuntimeException();
	}
	
	public Activation(Object object, String taskName) {		
		throw new RuntimeException();
	}
	
	public Activation(Object object, String taskName, Object param) {
		throw new RuntimeException();
	}
	
	public Activation(Object object, String taskName, Object param1, Object param2) {
		throw new RuntimeException();
	}
	
	public Activation(Object object, String taskName, Object param1, Object param2, Object param3) {
		throw new RuntimeException();
	}
	
	public Object object() {	
		throw new RuntimeException();
	}
	
	public String taskName() {
		throw new RuntimeException();
	}
	
	public Object[] parameters() {
		throw new RuntimeException();
	}
	
	public R result() {
		throw new RuntimeException();
	}
	
	//scheduled in the future
	public synchronized boolean isInFuture() {
		throw new RuntimeException();
	}
	
	//retain count == 0; it's in the thread pool but not yet executing
	public synchronized boolean isAboutToExecute() {
		throw new RuntimeException();
	}
	
	//method is currently executing
	public synchronized boolean isExecuting() {
		throw new RuntimeException();
	}
	
	//done executing and is retired now
	public synchronized boolean hasRetired() {
		throw new RuntimeException();
	}
	
	public synchronized boolean comesBefore(Activation<?> later) {
		throw new RuntimeException();
	}
	
	public synchronized void hb(Activation<?> later) {			
		throw new RuntimeException();
	}

}

