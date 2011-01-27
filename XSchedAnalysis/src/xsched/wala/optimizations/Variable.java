package xsched.wala.optimizations;

import com.ibm.wala.classLoader.IMethod;

public class Variable {
	
	public static final int THIS = 1;
	public static final int CLASS = -1;
	
	public final IMethod method;
	public final int ssaVariable;
	
	public Variable(IMethod method, int ssaVariable) {
		this.method = method;
		this.ssaVariable = ssaVariable;
	}

	//true if this sync point is on the "this" variable and the method is synchronized
	public boolean isThis() {
		return ssaVariable == THIS;
	}
	
	public boolean isClassInStaticMethod() {
		return ssaVariable == CLASS;
	}
	
	@Override
	public int hashCode() {
		return method.hashCode() * 2083 + ssaVariable;
	}
	
	@Override
	public boolean equals(Object otherObj) {
		if(otherObj instanceof Variable) {
			Variable other = (Variable)otherObj;
			return other.ssaVariable == ssaVariable && other.method.equals(method);
		} else {
			return false;
		}
	}
}
