package xsched.analysis.schedule;

import soot.jimple.spark.solver.Propagator;

public class ExitNode extends ScheduleNode {
	ExitNode(Schedule schedule) {
		super(schedule, null);
	}

	@Override
	public void analyze(Propagator propagator) {
		
	}
}
