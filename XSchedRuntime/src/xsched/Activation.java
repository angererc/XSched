package xsched;

import java.lang.reflect.Method;
import java.util.ArrayList;

import xsched.ThreadPool.Worker;

public class Activation<R> implements ThreadPool.WorkItem {
	
	static final ThreadPool POOL = new ThreadPool();
	static boolean kickedOff = false;
	static final ThreadLocal<Activation<?>> NOW = new ThreadLocal<Activation<?>>();
		
	/*
	 * 
	 */
	private Object object;
	private Method method;
	private Object[] params;
	private R result;
	
	private void scheduleActivationAfterNow(Activation<?> activation) {
		if(! kickedOff)
			return;
		
		Activation<?> now = NOW.get();
		assert(now != null) : "no now activation found!!!";
		now.successors.add(activation);
		if(Debug.ENABLED)
			Debug.newActivation(activation);		
	}
	
	public static <T> Activation<T> schedule(Object object, String taskName, Object...params) {
		return new Activation<T>(object, taskName, params);
	}
	
	public static Activation<?> now() {
		return NOW.get();
	}
		
	/** retain count; once retain count drops to 0 the activation can start */
	private static final int EXECUTING = -42; //flag to indicate that this activation has been executed
	private static final int RETIRED = -84; //flag to indicate that this activation has been executed
	private int retainCount = 1;
	private ArrayList<Activation<?>> successors = new ArrayList<Activation<?>>(); //synchronized with this
	
	private Activation(Object object, String taskName, Object... params) {
		init(object, taskName, params);
		scheduleActivationAfterNow(this);
	}
	
	private void init(Object object, String taskName, Object... params) {
		this.object = object;
		this.params = params;
				
		for (Method method : object.getClass().getMethods()) {
			//TODO the "typesafe" way is to name a task by its full signature, e.g., foo(Ljava/lang/String)V;
			//so that's what we expect you to use in an Activation.schedule.
			//also, the WALA framework used during the analysis kinda expects such a full name
			//however, it seems to be a non standard thing to translate a java method into a string like that
			//therefore we simply search for the first method with the given name. Therefore, overloading
			//is currently not possible. Fix that.
            if (taskName.startsWith(method.getName() + "(")) {
            	if(method.getParameterTypes().length == params.length) {
            		if(this.method != null) 
            			throw new Error("ambiguous tasks with name " + taskName + " and " + params.length + " params in " + object.getClass());
            		this.method = method;
            		//when debugging, continue to search the methods to make sure that there is only one applicable task
            		if(! Debug.ENABLED) 
            			break;
            	}
            	
            }
        }
		if(this.method == null)
			throw new Error("no task with name " + taskName + " and " + params.length + " params found in " + object.getClass());		
	}

	public Object object() {
		return this.object;
	}
	
	public String taskName() {
		return this.method.getName();
	}
	
	public Object[] parameters() {
		return this.params;
	}
	
	public R result() {
		return this.result;
	}
	
	//scheduled in the future
	public synchronized boolean isInFuture() {
		return this.retainCount > 0;
	}
	
	//retain count == 0; it's in the thread pool but not yet executing
	public synchronized boolean isAboutToExecute() {
		return this.retainCount == 0;
	}
	
	//method is currently executing
	public synchronized boolean isExecuting() {
		return this.retainCount == EXECUTING;
	}
	
	//done executing and is retired now
	public synchronized boolean hasRetired() {
		return this.retainCount == RETIRED;
	}
	
	//used by Debug()
	int retainCount() {
		return this.retainCount;
	}
	
	private synchronized void retain() {
		assert(this.isInFuture());
		this.retainCount++;
		if(Debug.ENABLED)
			Debug.activationStateChange(this);
	}
	
	private synchronized void release() {
		assert(this.isInFuture());
		this.retainCount--;
		if(Debug.ENABLED)
			Debug.activationStateChange(this);
		
		if(this.retainCount == 0) {
			POOL.submit(this);
		}
	}
	
	public synchronized boolean comesBefore(Activation<?> later) {
		if(this.successors.contains(later)) {
			return true;
		} else {
			for(Activation<?> succ : this.successors) {
				if(succ.comesBefore(later)) {
					return true;
				}
			}
			return false;
		}
	}
	
	public synchronized void hb(Activation<?> later) {			
		
		assert(! later.comesBefore(this)) : "cycle detected";
		assert(NOW.get().comesBefore(later)) : "now does not happen before later";
		
		//happensBefore() has only an effect if this has not already been executed
		if(this.retainCount != RETIRED && !this.successors.contains(later)) {
			((Activation<?>)later).retain();
			this.successors.add(later);
		}
	}
	
	//this is only for the runtime, a normal program should never call this.
	//only called once in the beginning of a program
	//the analysis does not see this method because it has to happen "before"
	//this method waits until the POOL is empty
	
	public static void kickOffMain(Activation<?> main) {
		assert(NOW.get() == null);
		NOW.set(main);
		kickedOff = true;
		//kick it off
		main.release();
		POOL.waitTillDone();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void exec(Worker worker) {
		assert(this.retainCount == 0) : "retain count must be 0 but was " + this.retainCount;
		synchronized(this) { //this synchronized shouldn't be necessary because in a legal prog 
			//no other activation should call retain or release once this activation can be scheduled...
			this.retainCount = EXECUTING;
		}
		NOW.set(this);
		if(Debug.ENABLED)
			Debug.activationStateChange(this);
		
		try {
			this.result = (R) this.method.invoke(this.object, this.params);
		
			//we're out'a here
			NOW.set(null);
			this.retainCount = RETIRED;
			if(Debug.ENABLED)
				Debug.activationStateChange(this);
			
			//clean up and give successors a chance to execute				
			for(Activation<?> succ : this.successors) {
				((Activation<?>)succ).release();
			}
			this.successors = null;
			this.method = null;
			this.params = null;
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Error(e);
		}
	}

	@Override
	public String toString() {
		return "Activation@" + System.identityHashCode(this) + "(" + this.taskName() + ")";
	}
	
}
