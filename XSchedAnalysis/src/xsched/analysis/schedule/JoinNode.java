package xsched.analysis.schedule;

import soot.jimple.spark.solver.Propagator;

public class JoinNode extends ScheduleNode {

	protected final BranchNode branch;
	
	JoinNode(Schedule schedule, ScheduleNode parent, BranchNode branch) {
		super(schedule, parent);
		this.branch = branch;
	}

	@Override
	public void analyze(Propagator propagator) {
		
	}

}
