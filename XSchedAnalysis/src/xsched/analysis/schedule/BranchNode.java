package xsched.analysis.schedule;

import java.util.List;

import soot.jimple.spark.solver.Propagator;

public class BranchNode extends ScheduleNode {
	private List<ScheduleNode> options;
	final JoinNode joinNode;
	
	BranchNode(Schedule schedule, ScheduleNode parent, List<ScheduleNode> options) {
		super(schedule, parent);
		this.options = options;
		this.joinNode = new JoinNode(schedule, parent, this);
	}
	public List<ScheduleNode> options() {
		return options;
	}
	@Override
	public void analyze(Propagator propagator) {
		
	}
}
