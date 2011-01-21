package xsched.analysis.wala.schedule_extraction;

import com.ibm.wala.ssa.ISSABasicBlock;

public class BackEdgeFlowData extends EdgeFlowData {
	
	BackEdgeFlowData(ISSABasicBlock from, ISSABasicBlock to) {
		super(from, to);
	}
	
	@Override
	public String toString() {
		return "Back-" + super.toString();
	}
}
