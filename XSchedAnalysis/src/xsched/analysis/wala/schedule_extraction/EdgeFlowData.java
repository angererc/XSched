package xsched.analysis.wala.schedule_extraction;

import com.ibm.wala.ssa.ISSABasicBlock;

public class EdgeFlowData extends FlowData {

	//data can be null when the edge has not yet been evaluated
	private final NormalNodeFlowData data; 
	
	final ISSABasicBlock from;
	final ISSABasicBlock to;
	
	EdgeFlowData(ISSABasicBlock src, ISSABasicBlock trgt) {
		this.data = new NormalNodeFlowData(src);
		this.from = src;
		this.to = trgt;
	}
	
	boolean isInitial() {
		return data.isInitial();
	}
	
	NormalNodeFlowData getData() {
		return data;
	}
	
	@Override
	boolean stateEquals(FlowData other) {
		assert other != null;
		return data.stateEquals(other);
	}
	
	@Override
	public void copyState(FlowData v) {
		assert v instanceof NormalNodeFlowData;
		this.data.copyState(v);
	}
	
	@Override
	public String toString() {
		return "Edge Flow Data " + from.getGraphNodeId() + "->" + to.getGraphNodeId();
	}
	
}
