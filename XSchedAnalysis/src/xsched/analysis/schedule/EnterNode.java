package xsched.analysis.schedule;

public class EnterNode<Context> extends ScheduleNode<Context> {
	
	EnterNode() {
		super();
	}

	@Override
	public void analyze(Schedule<Context> schedule) {
		schedule.setResultHeap(this, schedule.factory.newHeap());
		schedule.nodeChanged(this);
	}
}
