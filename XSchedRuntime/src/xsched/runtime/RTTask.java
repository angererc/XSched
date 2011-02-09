package xsched.runtime;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import xsched.Task;

public class RTTask<R> extends Task<R> implements Runnable {
	
	static final ExecutorService Pool = Executors.newCachedThreadPool();
	//the task that is currently executing in this thread
	static final ThreadLocal<RTTask<?>> Now = new ThreadLocal<RTTask<?>>();
	
	/*
	 * 
	 */
	private final Object receiver;
	private final Method method;
	private final Object[] params;
	private volatile R result;
	
	/** retain count; once retain count drops to 0 the activation can start */
	private static final int EXECUTING = -42; //flag to indicate that this activation has been executed
	private static final int RETIRED = -84; //flag to indicate that this activation has been executed
	private final AtomicInteger retainCount;
	
	private ArrayList<RTTask<?>> retainedTasks; //somebody called this.hb(other) so we retain other
	
	//theParams do not contain "now"
	public RTTask(Object object, String taskName, Object... theParams) {
		this.receiver = object;
		
		//splice in "this" as the first parameter
		int len = theParams.length;
		this.params = new Object[len+1];
		Class<?>[] paramTypes = new Class<?>[len+1];
		
		params[0] = this;
		paramTypes[0] = Task.class;
		
		for(int i = 0; i < len; i++) {
			Object param = theParams[i];
			params[i+1] = param;
			paramTypes[i+1] = param.getClass();
		}
		
		Method found = null;
		//find the corresponding method
		try {
			Method[] methods = object.getClass().getMethods();
			//iterate all methods
			findMethod: for(Method m : methods) {
				//name must be equal
				if(m.getName().equals(taskName)) {
					Class<?>[] methodParams = m.getParameterTypes();
					//param lengths must be equal
					if(methodParams.length == paramTypes.length) {
						//check that all param types are OK
						for(int i = 0; i < methodParams.length; i++) {
							Class<?> methodParam = methodParams[i];
							Class<?> myParam = paramTypes[i];
							if( ! methodParam.isAssignableFrom(myParam)) {
								//no, continue with a different method
								continue findMethod;
							}
						}
						//params are OK, keep the method and finish
						found = m;
						break findMethod;
					}
				}
			}
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}	
		
		if(found == null) {
			throw new RuntimeException("didn't find valid method for " + taskName);
		}
		this.method = found;
		
		this.retainCount = new AtomicInteger(0);
		if(taskName.startsWith(Task.NormalTaskMethodPrefix)) {	
			RTTask<?> now = Now.get();
			assert(now != null) : "no now activation found!!!";
			now.retain(this);
		} else if (taskName.startsWith(Task.MainTaskMethodPrefix)) {
			assert(Now.get() == null) : "main task must be the first task to be scheduled";			
			try {
				Pool.execute(this);
				Pool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
			} catch (InterruptedException e) {
				Pool.shutdownNow();
				e.printStackTrace();
			}
		} else {
			throw new RuntimeException("illegal task method: " + taskName);
		}
	}
	
	@Override
	public Object receiver() {
		return this.receiver;
	}
	
	@Override
	public String taskName() {
		return this.method.getName();
	}

	@Override
	public Object[] parameters() {
		return this.params;
	}
	
	@Override
	public R result() {
		return result;
	}

	@Override
	public void setResult(R result) {
		this.result = result;
	}

	@Override
	public boolean isAboutToExecute() {
		return this.retainCount.get() == 0;
	}

	@Override
	public boolean isExecuting() {
		return this.retainCount.get() == EXECUTING;
	}

	@Override
	public boolean isInFuture() {
		return this.retainCount.get() > 0;
	}
	
	@Override
	public boolean hasRetired() {
		return this.retainCount.get() == RETIRED;
	}

	@Override
	public synchronized boolean isOrderedBefore(Task<?> later) {
		if(this.retainedTasks == null) {
			return false;
		} else if(this.retainedTasks.contains(later)) {
			return true;
		} else {
			for(Task<?> succ : this.retainedTasks) {
				if(succ.isOrderedBefore(later)) {
					return true;
				}
			}
			return false;
		}
	}

	private boolean doesRetain(Task<?> other) {
		return this.retainedTasks != null && this.retainedTasks.contains(other);
	}
	
	private synchronized void retain(RTTask<?> later) {
		//we know that other happens after now and therefore it's retain count is > 0 and will remain so until we're done;
		//we just have to make sure that our increment to later isn't swallowed, therefore we use an atomic integer
		if(this.retainedTasks==null)
			this.retainedTasks = new ArrayList<RTTask<?>>();
		
		this.retainedTasks.add(later);
		int count = later.retainCount.incrementAndGet();
		System.out.println(this + " retains " + later +"; new retain count is " + count);
	}

	private synchronized void release() {
		assert(this.isInFuture());
		int count = this.retainCount.decrementAndGet();
		if(count == 0) {
			Pool.execute(this);
		}
	}
		
	@Override
	public void hb(Task<?> later) {			

		assert(! later.isOrderedBefore(this)) : "cycle detected";
		assert(later.isInFuture()) : "rhs of happens-before must be in future";
		assert(Now.get().isOrderedBefore(later)) : "now does not happen before later";

		//happensBefore() has only an effect if this has not already been executed
		//and we do not already retain later
		if(!this.hasRetired() && !this.doesRetain(later)) {			
			this.retain((RTTask<?>)later);
		}
	}
	
	private void releaseRetained_unsynced() {
		if(this.retainedTasks == null)
			return;
		//release retained
		for(RTTask<?> succ : this.retainedTasks) {
			succ.release();
		}
	}
	
	@Override
	public void run() {
		assert(this.retainCount.get() == 0) : "retain count must be 0 but was " + this.retainCount;
		
		this.retainCount.set(EXECUTING);
		
		Now.set(this);
		
		try {
			this.method.invoke(this.receiver, this.params);
			this.retainCount.set(RETIRED);
			//clean up and give successors a chance to execute				
			this.releaseRetained_unsynced();
		} catch (Exception e) {
			//we kill the program if there is ever an unhandled exception
			//so we know that either all works according to the schedule or we die
			throw new Error(e);
		} finally {
			//we're out'a here
			Now.set(null);
			//could set fields to null but why should we... GC will do that sooner or later
			//and if the user keeps the thread around he might have a reason.
			//we are just clearing the array lists to avoid keeping everything alive when only retaining one task
			this.retainedTasks = null;
		}
	}
	
	@Override
	public String toString() {
		return "Task@" + System.identityHashCode(this) + "(" + this.taskName() + ")";
	}
}
