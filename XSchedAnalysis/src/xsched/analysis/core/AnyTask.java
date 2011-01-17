package xsched.analysis.core;

public class AnyTask extends TaskVariable<Void> {
	public static final AnyTask instance = new AnyTask();
	
	private AnyTask() {
		super(null);
	}

	@Override
	public boolean doesHappenAfter(TaskVariable<?> before) {
		return false;
	}

	@Override
	public boolean doesHappenBefore(TaskVariable<?> after) {
		return false;
	}

	@Override
	public boolean equals(Object otherObj) {
		return otherObj == this;
	}

	@Override
	public void happensBefore(TaskVariable<?> later) {
		//nothing to do?
	}

	@Override
	public int hashCode() {
		return System.identityHashCode(this);
	}

	@Override
	public boolean isOrderedWith(TaskVariable<?> other) {
		return false;
	}
	
	

}
