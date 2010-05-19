package xsched.analysis.schedule;

import java.util.HashMap;
import java.util.List;

import soot.MethodContext;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.spark.pag.AllocNode;
import soot.jimple.spark.pag.Node;
import xsched.analysis.XSchedAnalyzer;

public class Schedule {

	private HashMap<AllocNode, ActivationNode> nodesByAllocationNode = new HashMap<AllocNode, ActivationNode>();
	
	public final ScheduleNode exitNode;
	
	public Schedule() {		        
		this.exitNode = new ExitNode(this);		
	}
	
	public BranchNode addBranchNode(List<ScheduleNode> options) {
		return new BranchNode(this, options);
	}
			
	public ActivationNode addActivationNode(AllocNode activation, AllocNode receiver, SootMethod task, List<Node> params) {
		assert(activation.getType().equals(XSchedAnalyzer.ACTIVATION_TYPE)) : "alloc node is not an activation";
		
		if(nodesByAllocationNode.containsKey(activation)) {
			ActivationNode node = nodesByAllocationNode.get(activation);
			assert(node.activation == activation);
			assert(node.params.equals(params));
			assert(node.receiver.equals(receiver));
			assert(node.task.equals(task));
		}
		
		//new schedule node
		ActivationNode node = new ActivationNode(this, activation, receiver, task, params);
		nodesByAllocationNode.put(activation, node);
		
		//make sure that the soot method is considered to be reachable
		//this will have the effect that the OnFlyCallGraph creates new nodes in the pag through the MethodPAG
		MethodOrMethodContext context = MethodContext.v(task, node);
		Scene.v().getReachableMethods().addCustomMethodOrMethodContext(context);
		
		return node;
	}
	
	public ActivationNode activationNodeForAllocNode(AllocNode node) {
		return nodesByAllocationNode.get(node);
	}
	
	public void addHappensBefore(ScheduleNode earlier, ScheduleNode later) {
		
	}
}
