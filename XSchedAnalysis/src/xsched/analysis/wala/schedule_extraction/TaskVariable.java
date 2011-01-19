package xsched.analysis.wala.schedule_extraction;

public class TaskVariable {

	final LoopContext loopContext;
	final int ssaVariable;
		
	TaskVariable(LoopContext loopContext, int ssaVariable) {
		this.loopContext = loopContext;
		this.ssaVariable = ssaVariable;
	}
		
	@Override
	public int hashCode() {
		return ssaVariable;
	}
	
	@Override
	public boolean equals(Object otherObject) {
		if (otherObject == this) {
			return true;
		} else if (otherObject instanceof TaskVariable) {
			TaskVariable other = (TaskVariable)otherObject;
			return other.ssaVariable == ssaVariable;			
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return "<" + ssaVariable + ">";
	}
}
