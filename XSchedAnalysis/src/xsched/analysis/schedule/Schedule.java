package xsched.analysis.schedule;

import java.util.HashMap;

import soot.MethodContext;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.spark.pag.AllocNode;
import soot.jimple.spark.pag.PAG;
import xsched.analysis.XSchedAnalyzer;

public class Schedule {

	private HashMap<AllocNode, ScheduleNode> nodesByAllocationNode = new HashMap<AllocNode, ScheduleNode>();
	
	public final ScheduleNode exitNode;
	
	public Schedule(PAG pag) {		
        AllocNode exit = pag.makeAllocNode(this, XSchedAnalyzer.ACTIVATION_TYPE, null);
		this.exitNode = new ScheduleNode(exit, null);
		nodesByAllocationNode.put(exit, exitNode);
	}
	
		
	public ScheduleNode addNode(AllocNode allocSite, SootMethod task) {
		assert(allocSite.getType().equals(XSchedAnalyzer.ACTIVATION_TYPE)) : "alloc node is not an activation";
		if(nodesByAllocationNode.containsKey(allocSite)) {
			return nodesByAllocationNode.get(allocSite);
		}
		
		ScheduleNode node = new ScheduleNode(allocSite, task);
		nodesByAllocationNode.put(allocSite, node);
		
		//make sure that the soot method is considered to be reachable
		//this will have the effect that the OnFlyCallGraph creates new nodes in the pag through the MethodPAG
		MethodOrMethodContext context = MethodContext.v(task, node);
		Scene.v().getReachableMethods().addCustomMethodOrMethodContext(context);
		
		return node;
	}
	
	public void addHappensBefore(ScheduleNode earlier, ScheduleNode later) {
		
	}
}
