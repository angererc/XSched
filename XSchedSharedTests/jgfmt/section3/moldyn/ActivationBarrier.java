package jgfmt.section3.moldyn;

import xsched.Activation;

public class ActivationBarrier {
	private Activation<Void> next;
	private Activation<Void> start;
	
	public ActivationBarrier() {
		next = Activation.schedule(this, "next");
		start = Activation.schedule(this, "start");
		next.hb(start);
	}

	public void next() {
		next = Activation.schedule(this, "next");
		start.hb(next);
	}
	
	public void start(Activation<Void> next) {
		start = Activation.schedule(this, "start");
		next.hb(start);
	}
}
