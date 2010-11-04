package xsched;

public class Activation<R> {

	public static Activation<?> now() {
		throw new RuntimeException();
	}
	
	public static <T> Activation<T> schedule(Object object, String taskName, Object...params) {
		throw new RuntimeException();
	}
	
	public static void kickOffMain(Activation<?> main) {
		throw new RuntimeException();
	}
	
	private Activation()
	{
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

