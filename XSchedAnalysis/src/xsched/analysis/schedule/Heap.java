package xsched.analysis.schedule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

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
	private final QueueReader<Pair<InvokeExpr, Pair<Node, Node>>> callAssignsReader;

	public Heap(PAG pag) {
		this.pag = pag;
		this.callAssignsReader = this.pag.callAssignsReader();
	}

	public PAG pag() {
		return pag;
	}

	public static class NewHBRelationshipRecord {
		public final InvokeExpr source;
		private PointsToSetInternal lhs;
		private PointsToSetInternal rhs;
		private NewHBRelationshipRecord(InvokeExpr source) {
			this.source = source;
		}
		public String toString() {
			return "happens-before declaration: " + lhs + " -> " + rhs;
		}
	}

	public static class NewActivationRecord {
		public final InvokeExpr source;
		private AllocNode activation;
		private PointsToSetInternal receiver;
		private String task;
		private ArrayList<PointsToSetInternal> params = new ArrayList<PointsToSetInternal>();
		private NewActivationRecord(InvokeExpr source) {
			this.source = source;
		}
		public String toString() {
			return "schedule declaration: " + activation + " := " + receiver + "." + task + "(" + params + ")";
		}
	}

	public Pair<Collection<NewActivationRecord>, Collection<NewHBRelationshipRecord>> findNewHBDeclarations() {
		
		//todo: right now, I observe the pag for new call assigns; however, maybe it would be better to observe the call graph
		//for new edges and then find the correct data in the PAG. 
		//right now, if I use a call insensitive context manager, this context manager strips away the schedule node as the context
		//from the source which results in a weird state... not sure why, but somehow it's whacky
		
		HashMap<InvokeExpr, NewHBRelationshipRecord> newHBRelationships = new HashMap<InvokeExpr, NewHBRelationshipRecord>();
		HashMap<InvokeExpr, NewActivationRecord> newActivations = new HashMap<InvokeExpr, NewActivationRecord>();
		
		while (callAssignsReader.hasNext()) {
			Pair<InvokeExpr, Pair<Node, Node>> assignment = callAssignsReader
					.next();
			InvokeExpr ie = assignment.getO1();

			SootMethod method = ie.getMethod();
			if (method.equals(XSchedAnalyzer.ACTIVATION_CONSTRUCTOR0)
					|| method.equals(XSchedAnalyzer.ACTIVATION_CONSTRUCTOR1)
					|| method.equals(XSchedAnalyzer.ACTIVATION_CONSTRUCTOR2)
					|| method.equals(XSchedAnalyzer.ACTIVATION_CONSTRUCTOR3)) {
				// constructor <init>(java.lang.Object,java.lang.String, ...)
				NewActivationRecord act = newActivations.get(ie);
				if (act == null) {
					act = new NewActivationRecord(ie);
					newActivations.put(ie, act);
				}

				Node src = assignment.getO2().getO1();
				Node tgt = assignment.getO2().getO2();

				assert (tgt instanceof LocalVarNode);
				assert (((LocalVarNode) tgt).getVariable() instanceof Pair<?, ?>);

				LocalVarNode target = (LocalVarNode) tgt;
				Object which = ((Pair<?, ?>) ((LocalVarNode) target)
						.getVariable()).getO2();
				if (which.equals(PointsToAnalysis.THIS_NODE)) {
					assert (act.activation == null);
					assert (src.getP2Set() != null);
					List<Node> allocs = src.getP2Set().contents();
					assert (allocs.size() == 1);
					act.activation = (AllocNode) allocs.get(0);

				} else if (which.equals(0)) {
					assert (act.receiver == null);
					assert(src.getP2SetForReal() != null);
					act.receiver = src.getP2Set();
				} else if (which.equals(1)) {
					assert (act.task == null);
					assert (src instanceof GlobalVarNode);
					Value task = ie.getArgBox(1).getValue();
					if (task instanceof StringConstant) {
						act.task = ((StringConstant) task).value;
					} else {
						throw new RuntimeException(
								"tasks must be specified as string constants!");
					}
				} else {
					assert (which instanceof Integer);
					// we assume that the param edges were created in the
					// correct order, so we don't check too much here
					assert (((Integer) which).intValue() - 2 == act.params
							.size());
					act.params.add(src.getP2Set());
				}

			} else if (ie.getMethod().equals(XSchedAnalyzer.HB_METHOD)) {
				NewHBRelationshipRecord rel = newHBRelationships.get(ie);
				if (rel == null) {
					rel = new NewHBRelationshipRecord(ie);
					newHBRelationships.put(ie, rel);
				}

				Node src = assignment.getO2().getO1();
				Node tgt = assignment.getO2().getO2();
				assert (tgt instanceof LocalVarNode);
				assert (((LocalVarNode) tgt).getVariable() instanceof Pair<?, ?>);

				LocalVarNode target = (LocalVarNode) tgt;
				Object which = ((Pair<?, ?>) ((LocalVarNode) target)
						.getVariable()).getO2();
				if (which.equals(PointsToAnalysis.THIS_NODE)) {
					assert (rel.lhs == null);
					rel.lhs = src.getP2Set();
				} else {
					assert (which instanceof Integer);
					assert (which.equals(0));
					assert (rel.rhs == null);
					rel.rhs = src.getP2Set();
				}				
			}

		}

		return new Pair<Collection<NewActivationRecord>, Collection<NewHBRelationshipRecord>>(newActivations.values(), newHBRelationships.values());
	}

	public Heap mergeWith(Heap other) {
		throw new RuntimeException("not yet implemented");
	}

	public Heap zipWith(Heap other) {
		throw new RuntimeException("not yet implemented");
	}
}
