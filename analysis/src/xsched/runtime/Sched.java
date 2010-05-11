package xsched.runtime;

import java.lang.reflect.Method;
import java.util.ArrayList;

import xsched.Activation;
import xsched.runtime.ThreadPool.Worker;

public class Sched<T, R extends Object> extends Activation.WithThis<T, R> implements ThreadPool.WorkItem {
	
	static final ThreadPool POOL = new ThreadPool();
	static final ThreadLocal<Sched<?,?>> NOW = new ThreadLocal<Sched<?,?>>();
	
	private void scheduleActivationImmediately(ActivationImpl<?,?> activation) {
		assert(NOW.get() == null);
		NOW.set(activation);
		//kick it off
		activation.release();
	}
	
	@Override
	public <T, R> void main(T object, String taskName, Class<?>[] parameterTypes, Object... params) {
		ActivationImpl<T, R> activation = new ActivationImpl<T, R>(object, taskName, parameterTypes, params);
		scheduleActivationImmediately(activation);
	}
	
	/*
	 * 
	 */
	private T object;
	private Method method;
	private Object[] params;
	private R result;
	
	private void scheduleActivationAfterNow(Sched<?,?> activation) {
		Sched<?,?> now = NOW.get();
		assert(now != null) : "no now activation found!!!";
		now.successors.add(activation);
		if(Debug.ENABLED)
			Debug.newActivation(activation);		
	}
		
	/** retain count; once retain count drops to 0 the activation can start */
	public static final int EXECUTING = -42; //flag to indicate that this activation has been executed
	public static final int RETIRED = -84; //flag to indicate that this activation has been executed
	private int retainCount = 1;
	ArrayList<Activation<?>> successors = new ArrayList<Activation<?>>(); //synchronized with this
			
	public Sched(T object, String taskName, Object... params) {
		this.object = object;
		this.params = params;
		
		for (Method method : object.getClass().getMethods()) {
            if (taskName.equals(method.getName())) {
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
		
		scheduleActivationAfterNow(this);
	}
	
	public Sched(T object, String taskName, Class<?>[] parameterTypes, Object... params) {
		assert(parameterTypes.length == params.length);
		
		this.object = object;
		this.params = params;
		
		try {
			this.method = object.getClass().getMethod(taskName, parameterTypes);
		} catch (SecurityException e) {
			throw new Error(e);
		} catch (NoSuchMethodException e) {
			throw new Error(e);
		}
		
		scheduleActivationAfterNow(this);
	}
	
	public T object() {
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
	
	public synchronized int retainCount() {
		return this.retainCount;
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
	
	private synchronized void retain() {
		assert(this.isInFuture());
		this.retainCount++;
		if(Debug.ENABLED)
			Debug.activationStateChange(this);
	}
	
	synchronized void release() {
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
			((Sched<?,?>)later).retain();
			this.successors.add(later);
		}
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
				((Sched<?,?>)succ).release();
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
