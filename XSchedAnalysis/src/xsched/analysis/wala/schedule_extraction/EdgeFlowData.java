package xsched.analysis.wala.schedule_extraction;

public class EdgeFlowData extends FlowData {

	//data can be null when the edge has not yet been evaluated
	NormalNodeFlowData data; 
	
	NormalNodeFlowData data() {
		return data;
	}
	
	@Override
	boolean stateEquals(FlowData other) {
		assert other != null;
		return data == null ? false : data.stateEquals(other);
	}
	
	@Override
	public void copyState(FlowData v) {
		assert v instanceof NormalNodeFlowData;
		this.data = (NormalNodeFlowData)v;
	}
	
}
