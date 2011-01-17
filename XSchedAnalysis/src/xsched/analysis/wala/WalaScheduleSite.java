package xsched.analysis.wala;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.ssa.SSAInvokeInstruction;

public class WalaScheduleSite {
	//method and task creation site are relevant for equality; the invocation site is only for convenience reasons
	public final IMethod method;	
	public final NewSiteReference taskCreationSite;
	
	SSAInvokeInstruction invoke;
	
	public WalaScheduleSite(IMethod method, NewSiteReference taskCreationSite) {
		this.method = method;
		this.taskCreationSite = taskCreationSite;	
	}
	
	void setSSAInvokeInstruction(SSAInvokeInstruction invoke) {
		this.invoke = invoke;
	}
	
	SSAInvokeInstruction ssaInvokeInstruction() {
		return invoke;
	}
	
	@Override
	public int hashCode() {
		return method.hashCode() * taskCreationSite.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if(this == other) {
			return true;
		} else if(other instanceof WalaScheduleSite) {
			WalaScheduleSite otherSite = (WalaScheduleSite)other;
			return (otherSite.method.equals(method) && otherSite.taskCreationSite.equals(taskCreationSite));
		} else {
			return false;
		}
	}
	
}
