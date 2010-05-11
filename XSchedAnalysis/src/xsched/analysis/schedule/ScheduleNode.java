package xsched.analysis.schedule;

import soot.Context;
import soot.SootMethod;
import soot.jimple.spark.pag.AllocNode;
import soot.jimple.spark.pag.MethodPAG;
import soot.jimple.spark.pag.PAG;

public class ScheduleNode implements Context {
	public final AllocNode object;
	public final SootMethod task;
		
	ScheduleNode(AllocNode allocNode, SootMethod task) {
		this.object = allocNode;
		this.task = task;
	}
		
	void addToPAG(PAG pag) {
		MethodPAG amp = MethodPAG.v(pag, task);
        amp.build();
        amp.addToPAG(this);        
	}
}
