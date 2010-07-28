package xsched.analysis.db;

import com.ibm.wala.classLoader.IMethod;

public class Variable {
	public final IMethod method;
	public final int ssaID;
	
	public Variable(IMethod method, int ssaID) {
		this.method = method;
		this.ssaID = ssaID;
	}
	
	@Override
	public String toString() {
		return method.getSignature() + ".v" + ssaID; 
	}
	
	@Override
	public int hashCode() {
		return method.hashCode() + ssaID;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof Variable) {
			Variable other = (Variable)o;
			return other.ssaID == ssaID && other.method.equals(method);
		} else {
			return false;
		}
	}
}
