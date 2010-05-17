package xsched.analysis.schedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import soot.PointsToAnalysis;
import soot.SootMethod;
import soot.Value;
import soot.jimple.InvokeExpr;
import soot.jimple.StringConstant;
import soot.jimple.spark.pag.AllocNode;
import soot.jimple.spark.pag.GlobalVarNode;
import soot.jimple.spark.pag.LocalVarNode;
import soot.jimple.spark.pag.Node;
import soot.jimple.spark.pag.PAG;
import soot.jimple.spark.sets.PointsToSetInternal;
import soot.toolkits.scalar.Pair;
import soot.util.queue.QueueReader;
import xsched.analysis.XSchedAnalyzer;

public class Heap {
	
	private final PAG pag;
	private final QueueReader<Pair<InvokeExpr, Pair<Node,Node>>> callAssignsReader;
	
	public Heap(PAG pag) {
		this.pag = pag;
		this.callAssignsReader = this.pag.callAssignsReader();
	}
	
	public PAG pag() {
		return pag;
	}
	
	private class NewHBRelationship {		
		PointsToSetInternal lhs;
		PointsToSetInternal rhs;
	}
	
	private class NewActivation {
		AllocNode activation;
		PointsToSetInternal receiver;
		String task;
		ArrayList<PointsToSetInternal> params = new ArrayList<PointsToSetInternal>();
	}
	
	public List<AllocNode> findNewHBDeclarations() {
		ArrayList<AllocNode> result = new ArrayList<AllocNode>();
		HashMap<InvokeExpr, NewHBRelationship> newHBRelationships = new HashMap<InvokeExpr, NewHBRelationship>();
		HashMap<InvokeExpr, NewActivation> newActivations = new HashMap<InvokeExpr, NewActivation>();
		while(callAssignsReader.hasNext()) {
            Pair<InvokeExpr, Pair<Node,Node>> assignment = callAssignsReader.next();
            InvokeExpr ie = assignment.getO1();
            
            SootMethod method = ie.getMethod();
            if(method.equals(XSchedAnalyzer.ACTIVATION_CONSTRUCTOR0)
            	|| method.equals(XSchedAnalyzer.ACTIVATION_CONSTRUCTOR1)
            	|| method.equals(XSchedAnalyzer.ACTIVATION_CONSTRUCTOR2)
            	|| method.equals(XSchedAnalyzer.ACTIVATION_CONSTRUCTOR3)
            ) { 
            	//constructor <init>(java.lang.Object,java.lang.String, ...)
            	NewActivation act = newActivations.get(ie);
            	if(act == null) {
            		act = new NewActivation();
            		newActivations.put(ie, act);
            	}
            	
            	Node src = assignment.getO2().getO1();
                Node tgt = assignment.getO2().getO2();
                
                assert(tgt instanceof LocalVarNode);
                assert(((LocalVarNode)tgt).getVariable() instanceof Pair<?,?>);
                
                LocalVarNode target = (LocalVarNode)tgt;
                Object which = ((Pair<?,?>)((LocalVarNode)target).getVariable()).getO2();
                if(which.equals(PointsToAnalysis.THIS_NODE)) {
                	assert(act.activation == null);
                	assert(src.getP2Set() != null);
                	List<Node> allocs = src.getP2Set().contents();
                	assert(allocs.size() == 1);
                	act.activation = (AllocNode)allocs.get(0);
                	
                } else if(which.equals(0)) {
                	assert(act.receiver == null);
                	act.receiver = src.getP2Set();
                } else if(which.equals(1)) {
                	assert(act.task == null);
                	assert(src instanceof GlobalVarNode);
                	Value task = ie.getArgBox(1).getValue();                	
                	if(task instanceof StringConstant) {                		
                		act.task = ((StringConstant)task).value;
                	} else {
                		throw new RuntimeException("tasks must be specified as string constants!");
                	}
                } else {
                	assert(which instanceof Integer);                	
                	//we assume that the param edges were created in the correct order, so we don't check too much here
                	assert(((Integer)which).intValue()-2 == act.params.size());
                	act.params.add(src.getP2Set());
                }
                
            } else if (ie.getMethod().equals(XSchedAnalyzer.HB_METHOD)) {
            	NewHBRelationship rel = newHBRelationships.get(ie);
            	if(rel == null) {
            		rel = new NewHBRelationship();
            		newHBRelationships.put(ie, rel);
            	}
            	
            	Node src = assignment.getO2().getO1();
                Node tgt = assignment.getO2().getO2();
                assert(tgt instanceof LocalVarNode);
                assert(((LocalVarNode)tgt).getVariable() instanceof Pair<?,?>);
                
                LocalVarNode target = (LocalVarNode)tgt;
                Object which = ((Pair<?,?>)((LocalVarNode)target).getVariable()).getO2();
                if(which.equals(PointsToAnalysis.THIS_NODE)) {
                	assert(rel.lhs == null);
                	rel.lhs = src.getP2Set();
                } else {
                	assert(which instanceof Integer);
                	assert(which.equals(0));
                	assert(rel.rhs == null);
                	rel.rhs = src.getP2Set();
                }
                System.out.println("new hb record: " + rel);
            }
                        
        }
				
		return result;
	}
	
	public Heap mergeWith(Heap other) {
		throw new RuntimeException("not yet implemented");
	}
	
	public Heap zipWith(Heap other) {
		throw new RuntimeException("not yet implemented");
	}
}
