package xsched.analysis.wala.schedule_extraction;

public class TaskVariable {

	final LoopContext loopContext;
	final int ssaVariable;
		
	TaskVariable(LoopContext loopContext, int ssaVariable) {
		this.loopContext = loopContext;
		this.ssaVariable = ssaVariable;
	}
	
	public int ssaVariable() {
		return ssaVariable;
	}
	
	public LoopContext loopContext() {
		return loopContext;
	}
		
	@Override
	public int hashCode() {
		return ssaVariable * 9973 + loopContext.hashCode();
	}
	
	@Override
	public boolean equals(Object otherObject) {
		if (otherObject == this) {
			return true;
		} else if (otherObject instanceof TaskVariable) {
			TaskVariable other = (TaskVariable)otherObject;
			return other.ssaVariable == ssaVariable && other.loopContext.equals(loopContext);			
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return loopContext + "<" + ssaVariable + ">";
	}
}
