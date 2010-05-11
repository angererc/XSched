package xsched;

public abstract class Activation<R> {
	
	public abstract Object object();
	public abstract String taskName();	
	public abstract Object[] parameters();
	public abstract R result();
	
	public abstract boolean isInFuture();
	public abstract boolean isAboutToExecute();
	public abstract boolean isExecuting();
	public abstract boolean hasRetired();
	
	public abstract boolean comesBefore(Activation<?> later);
	public abstract void hb(Activation<?> later);

	public static abstract class WithThis<T, R2> extends Activation<R2> {
		public abstract T object();
	}
}
