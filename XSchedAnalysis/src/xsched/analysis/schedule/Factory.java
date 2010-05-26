package xsched.analysis.schedule;

public interface Factory<Context> {

	public P2Set<Context> newP2Set();
	public Heap<Context> newHeap();
}
