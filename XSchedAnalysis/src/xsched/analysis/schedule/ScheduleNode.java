package xsched.analysis.schedule;

import soot.Context;
import soot.jimple.spark.solver.Propagator;

public abstract class ScheduleNode implements Context {
	protected final Schedule schedule;
	ScheduleNode(Schedule schedule) {
		this.schedule = schedule;
	}
	
	public abstract void analyze(Propagator propagator);
}
