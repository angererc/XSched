package xsched.analysis.schedule;

import soot.Context;
import soot.MethodContext;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.spark.pag.AllocNode;

public class ScheduleNode implements Context {
	public final AllocNode object;
	public final SootMethod task;
		
	ScheduleNode(AllocNode allocNode, SootMethod task) {
		this.object = allocNode;
		this.task = task;
	}
		
	public void addToCallGraph() {
		MethodOrMethodContext context = MethodContext.v(task, this);
		Scene.v().getReachableMethods().addCustomMethodOrMethodContext(context);
	}
}
