package xsched.analysis.wala.schedule_extraction;

public class HappensBeforeEdge {
	final TaskVariable lhs;
	final TaskVariable rhs;
	
	public HappensBeforeEdge(TaskVariable lhs, TaskVariable rhs) {
		this.lhs = lhs;
		this.rhs = rhs;		
	}
	
	@Override
	public boolean equals(Object otherObject) {
		if(otherObject == this) {
			return true;
		} else if (otherObject instanceof HappensBeforeEdge) {
			HappensBeforeEdge other = (HappensBeforeEdge)otherObject;
			return other.lhs.equals(lhs) && other.rhs.equals(rhs);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return lhs.hashCode() * 7219 + rhs.hashCode();
	}
	
	@Override
	public String toString() {
		return lhs + "->" + rhs;
	}
}
