package xsched.analysis.schedule;

public abstract class ScheduleNode<Context> {

	public abstract void analyze(Schedule<Context> schedule);
}
