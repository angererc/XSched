package xsched.analysis.core;
import java.util.HashSet;


public abstract class TaskVariable<TV> {
	
	public final TV id;
	
	private HashSet<TaskVariable<?>> happensBeforeEdges = new HashSet<TaskVariable<?>>();
	
	//use ScheduleSite.scheduleSiteForID() and FormalTaskParameter.formalTaskParameterForID() methods
	protected TaskVariable(TV id) {
		this.id = id;
	}
	
	//true if the task variable is equal to the other and it is not a "multiple" task variable representing multiple tasks (in a loop etc) 
	
	public boolean isOrderedWith(TaskVariable<?> other) {
		return this.doesHappenBefore(other) || other.doesHappenBefore(this);
	}
	
	public boolean doesHappenBefore(TaskVariable<?> after) {
		if(happensBeforeEdges.contains(after)) {
			return true;
		}
		for(TaskVariable<?> next : happensBeforeEdges) {
			if(next.doesHappenBefore(after)) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean doesHappenAfter(TaskVariable<?> before) {
		return before.doesHappenBefore(this);
	}
	
	public void happensBefore(TaskVariable<?> later) {
		assert(! later.doesHappenBefore(this));
		
		happensBeforeEdges.add(later);
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}
	
	//Important: make sure that for a given task, all task variables with the same id are actually the very same TaskVariable object!
	@Override
	public boolean equals(Object otherObj) {
		return this == otherObj;
	}
}
