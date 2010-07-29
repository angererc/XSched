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
		if(method == null)
			return "<fake variable>.v" + ssaID;
		else
			return method.getSignature() + ".v" + ssaID; 
	}
	
	@Override
	public int hashCode() {
		//allow for null methods for testing
		if(method == null)
			return ssaID;
		else
			return method.hashCode() + ssaID;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof Variable) {
			Variable other = (Variable)o;
			if(other.method == null) 
				return other.ssaID == ssaID && method == null;
			else
				return other.ssaID == ssaID && other.method.equals(method);
		} else {
			return false;
		}
	}
}
