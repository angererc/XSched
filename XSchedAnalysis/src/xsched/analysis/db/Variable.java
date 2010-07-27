package xsched.analysis.db;

import com.ibm.wala.types.MethodReference;

public class Variable {
	public final MethodReference methodRef;
	public final int ssaID;
	
	public Variable(MethodReference methodRef, int ssaID) {
		this.methodRef = methodRef;
		this.ssaID = ssaID;
	}
	
	@Override
	public String toString() {
		return methodRef.toString() + ".v" + ssaID; 
	}
	
	@Override
	public int hashCode() {
		return methodRef.hashCode() + ssaID;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof Variable) {
			Variable other = (Variable)o;
			return other.ssaID == ssaID && other.methodRef.equals(methodRef);
		} else {
			return false;
		}
	}
}
