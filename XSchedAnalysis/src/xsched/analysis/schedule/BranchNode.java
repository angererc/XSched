package xsched.analysis.schedule;

import java.util.List;

public class BranchNode extends ScheduleNode {
	private List<ScheduleNode> options;
	BranchNode(List<ScheduleNode> options) {
		this.options = options;
	}
	public List<ScheduleNode> options() {
		return options;
	}
}
