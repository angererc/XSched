package xsched.analysis.db;

import com.ibm.wala.classLoader.IMethod;

public class Variable {
	public final String description;
	public final int ssaID;
	
	public Variable(IMethod method, int ssaID) {
		this.description = method.getSignature().toString();
		this.ssaID = ssaID;
	}
	
	public Variable(String description, int ssaID) {
		this.description = description;
		this.ssaID = ssaID;
	}
	
	@Override
	public String toString() {
		return description + ".v" + ssaID; 
	}
	
	@Override
	public int hashCode() {
		return description.hashCode() + ssaID;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof Variable) {
			Variable other = (Variable)o;
			return other.ssaID == ssaID && other.description.equals(description);
		} else {
			return false;
		}
	}
}
