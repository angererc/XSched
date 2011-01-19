package xsched.analysis.wala.schedule_extraction;

public class EdgeFlowData extends FlowData {

	NodeFlowData data; 
	
	NodeFlowData data() {
		return data;
	}
	
	@Override
	boolean stateEquals(FlowData other) {
		return data.stateEquals(other);
	}
	
	@Override
	public void copyState(FlowData v) {
		assert v instanceof NodeFlowData;
		this.data = (NodeFlowData)v;
	}
	
}
