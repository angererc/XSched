package xsched.analysis.schedule;

import soot.jimple.spark.pag.PAG;

public class Heap {

	private final PAG pag;
	
	public Heap(PAG pag) {
		this.pag = pag;
	}
	
	public PAG pag() {
		return pag;
	}
	
	public Heap mergeWith(Heap other) {
		throw new RuntimeException("not yet implemented");
	}
	
	public Heap zipWith(Heap other) {
		throw new RuntimeException("not yet implemented");
	}
}
