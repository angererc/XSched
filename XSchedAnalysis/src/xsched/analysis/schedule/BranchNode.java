package xsched.analysis.schedule;

import java.util.List;

import soot.jimple.spark.solver.Propagator;

public class BranchNode extends ScheduleNode {
	private List<ScheduleNode> options;
	BranchNode(Schedule schedule, List<ScheduleNode> options) {
		super(schedule);
		this.options = options;
	}
	public List<ScheduleNode> options() {
		return options;
	}
	@Override
	public void analyze(Propagator propagator) {
		
	}
}
