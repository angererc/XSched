/**
 * 
 */
package xsched.analysis.pag;

import java.util.ArrayList;
import java.util.List;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.InvokeExpr;
import soot.jimple.spark.pag.AllocNode;
import soot.jimple.spark.pag.Node;
import soot.jimple.spark.sets.PointsToSetInternal;

public class NewActivationRecord {
	public final InvokeExpr source;
	
	AllocNode activation;
	PointsToSetInternal receivers;
	String task;
	ArrayList<Node> params = new ArrayList<Node>();
	
	NewActivationRecord(InvokeExpr source) {
		this.source = source;
	}
	public String toString() {
		return "schedule declaration: " + activation + " := " + receivers + "." + task + "(" + params + ")";
	}
	public AllocNode activation() {
		return activation;
	}
	public PointsToSetInternal receivers() {
		return receivers;
	}
	public List<Node> params() {
		return params;
	}
	public SootMethod taskForReceiver(Node receiver) {
		SootClass receiverClass = Scene.v().getSootClass(receiver.getType().toString());
		return receiverClass.getMethodByName(task);			
	}
}