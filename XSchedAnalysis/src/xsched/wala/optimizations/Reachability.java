package xsched.wala.optimizations;

import java.util.Set;

public interface Reachability<Task, Node> {
	public boolean canReach(Task task, Node method);
	public Set<Node> nonTaskNodesReachableByTask(Task task);
}
