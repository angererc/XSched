package xsched.analysis.schedule;

import java.util.List;

import soot.jimple.spark.pag.Node;

public class ScheduleEdge {

	private List<Node> params;
	public final ScheduleNode source;
	public final ScheduleNode target;
	
	public ScheduleEdge(ScheduleNode source, ScheduleNode target) {
		this.source = source;
		this.target = target;
	}
	
	public void setParams(List<Node> params) {
		this.params = params;
	}
	
	public List<Node> params() {
		return params;
	}
}
