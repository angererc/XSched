package xsched.analysis.schedule;

import java.util.ArrayList;
import java.util.List;

import soot.FastHierarchy;
import soot.MethodContext;
import soot.MethodOrMethodContext;
import soot.PointsToAnalysis;
import soot.Scene;
import soot.SootMethod;
import soot.SourceLocator;
import soot.jimple.spark.pag.AllocNode;
import soot.jimple.spark.pag.Node;
import soot.jimple.spark.pag.PAG;
import soot.jimple.spark.pag.VarNode;
import soot.jimple.spark.solver.Propagator;
import soot.toolkits.scalar.Pair;
import xsched.analysis.pag.NewActivationRecord;
import xsched.analysis.pag.NewHBRelationshipRecord;
import xsched.analysis.pag.PAGProxy;
import xsched.utils.PAG2DOT;

public class ActivationNode extends ScheduleNode {
	public final AllocNode activation;
	public final AllocNode receiver;
	public final SootMethod task;
	public final List<Node> params;
	
	private PAGProxy resultPAG;
		
	ActivationNode(Schedule schedule, ScheduleNode parent, AllocNode activation, AllocNode receiver, SootMethod task, List<Node> params) {
		super(schedule, parent);
		this.activation = activation;
		this.receiver = receiver;
		this.task = task;
		this.params = params;
		
		FastHierarchy fh = Scene.v().getOrMakeFastHierarchy();
		if( ! fh.canStoreType(receiver.getType(), task.getDeclaringClass().getType())) {
			throw new RuntimeException("receiver " + receiver.getType() + " does not define task " + task);
		}
	}
	
	public PAGProxy resultPAG() {
		return resultPAG;
	}
	
	private void initializePAG(PAG pag) {
		
		//make sure that the soot method is considered to be reachable
		//this will have the effect that the OnFlyCallGraph creates new nodes in the pag through the MethodPAG
		MethodOrMethodContext context = MethodContext.v(task, this);
		pag.getOnFlyCallGraph().callGraph().reachableMethods().addCustomMethodOrMethodContext(context);
		pag.getOnFlyCallGraph().build();
		
		//add an edge from the alloc node to the this node
		VarNode thisNode = pag.findContextVarNode(new Pair<SootMethod,String>(task, PointsToAnalysis.THIS_NODE), this);
		pag.addAllocEdge(receiver, thisNode);
		
		for(int i = 0; i < params.size(); i++) {
			VarNode paramNode = pag.findContextVarNode(new Pair<SootMethod,Integer>(task,i), this);
			assert (paramNode != null);
			pag.addEdge(params.get(i), paramNode);			
		}
	}
	
	private void createActivationNodes(NewActivationRecord record) {
		List<Node> receivers = record.receivers().contents();
		
		if(receivers.size() == 1) {
			AllocNode receiver = (AllocNode)receivers.get(0);
			ScheduleNode newNode = schedule.createActivationNode(this, record.activation(), receiver, record.taskForReceiver(receiver), record.params());
			this.addHappensBefore(newNode);
		} else {
			List<ScheduleNode> options = new ArrayList<ScheduleNode>();
			for(Node receiver : receivers) {
				ScheduleNode option = schedule.createActivationNode(this, record.activation(), (AllocNode)receiver, record.taskForReceiver(receiver), record.params());
				options.add(option);
			}
			ScheduleNode newNode = schedule.createBranchNode(this, options);
			this.addHappensBefore(newNode);
		}
	}
	
	private void createHBRelationships(NewHBRelationshipRecord record) {
		List<Node> lhsds = record.lhs().contents();
		List<Node> rhsds = record.rhs().contents();
		
		for(Node lhs : lhsds) {
			for(Node rhs : rhsds) {
				ActivationNode lhsScheduleNode = schedule.activationNodeForAllocNode((AllocNode)lhs);
				ActivationNode rhsScheduleNode = schedule.activationNodeForAllocNode((AllocNode)rhs);
				lhsScheduleNode.addHappensBefore(rhsScheduleNode);
			}
		}
	}
	
	@Override
	public void analyze(Propagator propagator) {
		
		PAG pag = propagator.pag();
		initializePAG(pag);
		
		resultPAG = new PAGProxy(pag);
		
		propagator.propagate();		
		propagator.donePropagating();
		
		new PAG2DOT().dump(pag, SourceLocator.v().getOutputDir() + "/after.dot");
						
		for(NewActivationRecord activationRecord : resultPAG.newActivationRecords()) {
			createActivationNodes(activationRecord);
		}
		
		for(NewHBRelationshipRecord hbRecord : resultPAG.newHBRelationshipRecords()) {
			createHBRelationships(hbRecord);
		}
				
	}
}
