package xsched.analysis.schedule;

import java.util.HashMap;

import soot.Scene;
import soot.SootMethod;
import soot.Type;
import soot.jimple.spark.pag.AllocNode;
import soot.jimple.spark.pag.PAG;
import xsched.Activation;

public class Schedule {

	private HashMap<AllocNode, ScheduleNode> nodesByAllocationNode = new HashMap<AllocNode, ScheduleNode>();
	
	private final PAG pag;
	
	public final ScheduleNode exitNode;
	
	public Schedule(PAG pag) {
		this.pag = pag;
		Type activationType = Scene.v().getRefType(Activation.class.getName());
        AllocNode exit = this.pag.makeAllocNode(this, activationType, null);
		this.exitNode = new ScheduleNode(exit, null);
		nodesByAllocationNode.put(exit, exitNode);
	}
		
	public ScheduleNode addNode(AllocNode allocSite, SootMethod task) {
		assert(allocSite.getType().equals(Scene.v().getRefType(Activation.class.getName()))) : "alloc node is not an activation";
		if(nodesByAllocationNode.containsKey(allocSite)) {
			return nodesByAllocationNode.get(allocSite);
		}
		
		ScheduleNode node = new ScheduleNode(allocSite, task);
		nodesByAllocationNode.put(allocSite, node);
		
		//make sure that the soot method is considered to be reachable
		//this will have the effect that the OnFlyCallGraph creates new nodes in the pag through the MethodPAG
		node.addToCallGraph();
		
		return node;
	}
	
	/*
	 * when adding something to the PAG, it's added
	 * in the call graph it works like this: when adding a new edge to something with a new context
	 * and this edge did not yet exist, it will add the edge and notify the observer which will then run through the body and add PAG nodes
	 * 
	 * the schedule has a context manager which can choose the context (the schedule node, the "original context" or something else)
	 * 
	 * MethodPAG.addToPAG(context) adds a method exactly once for the given context
	 * 
	 *  
	 *  we analyze a certain schedule node "now"
	 *  points-to encounters a new allocation
	 *  our context manager may or may not use "now" as a context for new allocations
	 *  after the points-to is done, we find all new Activation alloc nodes
	 *  for each such node, we add a schedule node if it did not yet exist
	 *  a new node is ALWAYS translated into new PAG nodes; so we don't do anything crazy here
	 *  
	 */
	public void addHappensBefore(ScheduleNode earlier, ScheduleNode later) {
		
	}
}
