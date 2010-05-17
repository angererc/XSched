package xsched.analysis.schedule;

import soot.Context;
import soot.SootMethod;
import soot.jimple.spark.pag.AllocNode;

public class ScheduleNode implements Context {
	public final AllocNode object;
	public final SootMethod task;
	
	private Heap resultHeap;
		
	ScheduleNode(AllocNode allocNode, SootMethod task) {
		this.object = allocNode;
		this.task = task;
	}
	
	public Heap resultHeap() {
		return resultHeap;
	}
	
	public void setResultHeap(Heap resultHeap) {
		this.resultHeap = resultHeap;
	}
}
