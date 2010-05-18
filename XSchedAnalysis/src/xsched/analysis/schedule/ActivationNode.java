package xsched.analysis.schedule;

import java.util.List;

import soot.FastHierarchy;
import soot.PointsToAnalysis;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.spark.pag.AllocNode;
import soot.jimple.spark.pag.Node;
import soot.jimple.spark.pag.PAG;
import soot.jimple.spark.pag.VarNode;
import soot.toolkits.scalar.Pair;

public class ActivationNode extends ScheduleNode {
	public final AllocNode activation;
	public final AllocNode receiver;
	public final SootMethod task;
	public final List<Node> params;
	
	private Heap resultHeap;
		
	ActivationNode(AllocNode activation, AllocNode receiver, SootMethod task, List<Node> params) {
		this.activation = activation;
		this.receiver = receiver;
		this.task = task;
		this.params = params;
		
		FastHierarchy fh = Scene.v().getOrMakeFastHierarchy();
		if( ! fh.canStoreType(receiver.getType(), task.getDeclaringClass().getType())) {
			throw new RuntimeException("receiver " + receiver.getType() + " does not define task " + task);
		}
	}
	
	public Heap resultHeap() {
		return resultHeap;
	}
	
	public void setResultHeap(Heap resultHeap) {
		this.resultHeap = resultHeap;
	}
	
	public void initializePAG(PAG pag) {
		
		//add an edge from the alloc node to the this node
		VarNode thisNode = pag.findContextVarNode(new Pair<SootMethod,String>(task, PointsToAnalysis.THIS_NODE), this);
		pag.addAllocEdge(receiver, thisNode);
		
		for(int i = 0; i < params.size(); i++) {
			VarNode paramNode = pag.findContextVarNode(new Pair<SootMethod,Integer>(task,i), this);
			assert (paramNode != null);
			pag.addEdge(params.get(i), paramNode);			
		}
	}
}
