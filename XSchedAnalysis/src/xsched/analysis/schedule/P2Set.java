package xsched.analysis.schedule;

import java.util.List;

public interface P2Set<Context> {

	public List<Context> contents();
	
	public void merge(P2Set<Context> other);
}
