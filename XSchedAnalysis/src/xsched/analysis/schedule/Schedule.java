package xsched.analysis.schedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import soot.SootMethod;
import soot.jimple.spark.pag.AllocNode;
import soot.jimple.spark.pag.Node;
import xsched.analysis.XSchedAnalyzer;

public class Schedule {

	private HashMap<AllocNode, ActivationNode> nodesByAllocationNode = new HashMap<AllocNode, ActivationNode>();
	
	private HashMap<ScheduleNode,List<ScheduleNode>> outgoingEdges = new HashMap<ScheduleNode,List<ScheduleNode>>();
	private HashMap<ScheduleNode,List<ScheduleNode>> incomingEdges = new HashMap<ScheduleNode,List<ScheduleNode>>();
	
	public final ScheduleNode exitNode;
	
	public Schedule() {		        
		this.exitNode = new ExitNode(this);		
	}
	
	BranchNode createBranchNode(ScheduleNode parent, List<ScheduleNode> options) {
		BranchNode branch = new BranchNode(this, parent, options);
		branch.addHappensBefore(branch.joinNode);
		return branch;
	}
				
	public ActivationNode createInitialActivationNode(AllocNode activation, AllocNode receiver, SootMethod task, List<Node> params) {
		return this.createActivationNode(null, activation, receiver, task, params);
	}
	
	ActivationNode createActivationNode(ScheduleNode parent, 
			AllocNode activation, 
			AllocNode receiver, 
			SootMethod task, 
			List<Node> params) {
		assert(activation.getType().equals(XSchedAnalyzer.ACTIVATION_TYPE)) : "alloc node is not an activation";
		
		if(nodesByAllocationNode.containsKey(activation)) {
			ActivationNode node = nodesByAllocationNode.get(activation);
			assert(node.activation == activation);
			assert(node.params.equals(params));
			assert(node.receiver.equals(receiver));
			assert(node.task.equals(task));
		}
		
		//new schedule node
		ActivationNode node = new ActivationNode(this, parent, activation, receiver, task, params);
		nodesByAllocationNode.put(activation, node);
		
		return node;
	}
	
	public ActivationNode activationNodeForAllocNode(AllocNode node) {
		return nodesByAllocationNode.get(node);
	}
	
	//called by nodes
	
	void addHappensBefore(ScheduleNode earlier, ScheduleNode later) {
		List<ScheduleNode> outgoing = outgoingEdges.get(earlier);
		if(outgoing == null) {
			outgoing = new ArrayList<ScheduleNode>();
			outgoingEdges.put(earlier, outgoing);
		}
		outgoing.add(later);
		
		List<ScheduleNode> incoming = incomingEdges.get(later);
		if(incoming == null) {
			incoming = new ArrayList<ScheduleNode>();
			incomingEdges.put(earlier, incoming);
		}
		incoming.add(earlier);
	}
	
	List<ScheduleNode> incomingEdges(ScheduleNode node) {
		return incomingEdges.get(node);
	}
	
	List<ScheduleNode> outgoingEdges(ScheduleNode node) {
		return outgoingEdges.get(node);
	}
}
