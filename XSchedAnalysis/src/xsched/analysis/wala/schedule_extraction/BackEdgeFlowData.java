package xsched.analysis.wala.schedule_extraction;

import com.ibm.wala.ssa.ISSABasicBlock;

public class BackEdgeFlowData extends EdgeFlowData {
	
	final ISSABasicBlock sourceBasicBlock;
	final ISSABasicBlock targetBasicBlock;
	
	BackEdgeFlowData(ISSABasicBlock sourceBasicBlock, ISSABasicBlock targetBasicBlock) {
		this.sourceBasicBlock = sourceBasicBlock;
		this.targetBasicBlock = targetBasicBlock;		
	}
	
	@Override
	public String toString() {
		return sourceBasicBlock.getNumber() + "->" + targetBasicBlock.getNumber();
	}
}
