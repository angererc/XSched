package xsched.analysis.schedule;

import java.util.List;

import soot.Context;
import soot.jimple.spark.solver.Propagator;

public abstract class ScheduleNode implements Context {
	
	protected final Schedule schedule;
	protected final ScheduleNode parent;
	
	ScheduleNode(Schedule schedule, ScheduleNode parent) {
		this.schedule = schedule;
		this.parent = parent;
	}
	
	public abstract void analyze(Propagator propagator);
	
	public void addHappensBefore(ScheduleNode later) {
		schedule.addHappensBefore(this, later);
	}
	
	public List<ScheduleNode> incomingEdges() {
		return schedule.incomingEdges(this);
	}
	
	public List<ScheduleNode> outgoingEdges() {
		return schedule.outgoingEdges(this);
	}
}
