package xsched.analysis.core;

public class FormalTaskParameter extends TaskVariable<Integer> {
	
	FormalTaskParameter(int position) {
		super(position);
	}
	
	@Override
	public String toString() {
		return "<" + id + ">";
	}
	
}
