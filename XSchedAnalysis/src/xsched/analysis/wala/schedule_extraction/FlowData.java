package xsched.analysis.wala.schedule_extraction;


import com.ibm.wala.fixedpoint.impl.AbstractVariable;

public abstract class FlowData extends AbstractVariable<FlowData> {
	
	public FlowData() {
		
	}
		
	abstract boolean stateEquals(FlowData other);
}