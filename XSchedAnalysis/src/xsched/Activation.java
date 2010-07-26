package xsched;

public class Activation<R> {

	
	public Activation(Object object, String taskName) {		
		throw new AnalysisScaffoldingException();
	}
	
	public Activation(Object object, String taskName, Object param) {
		throw new AnalysisScaffoldingException();
	}
	
	public Activation(Object object, String taskName, Object param1, Object param2) {
		throw new AnalysisScaffoldingException();
	}
	
	public Activation(Object object, String taskName, Object param1, Object param2, Object param3) {
		throw new AnalysisScaffoldingException();
	}
	
	public Object object() {	
		throw new AnalysisScaffoldingException();
	}
	
	public String taskName() {
		throw new AnalysisScaffoldingException();
	}
	
	public Object[] parameters() {
		throw new AnalysisScaffoldingException();
	}
	
	public R result() {
		throw new AnalysisScaffoldingException();
	}
	
	//scheduled in the future
	public synchronized boolean isInFuture() {
		throw new AnalysisScaffoldingException();
	}
	
	//retain count == 0; it's in the thread pool but not yet executing
	public synchronized boolean isAboutToExecute() {
		throw new AnalysisScaffoldingException();
	}
	
	//method is currently executing
	public synchronized boolean isExecuting() {
		throw new AnalysisScaffoldingException();
	}
	
	//done executing and is retired now
	public synchronized boolean hasRetired() {
		throw new AnalysisScaffoldingException();
	}
	
	public synchronized boolean comesBefore(Activation<?> later) {
		throw new AnalysisScaffoldingException();
	}
	
	public synchronized void hb(Activation<?> later) {			
		throw new AnalysisScaffoldingException();
	}

}

