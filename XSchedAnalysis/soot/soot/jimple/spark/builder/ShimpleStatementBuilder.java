package soot.jimple.spark.builder;

import soot.PointsToAnalysis;
import soot.RefLikeType;
import soot.RefType;
import soot.SootFieldRef;
import soot.SootMethod;
import soot.Value;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.ReturnStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.ThrowStmt;
import soot.jimple.spark.pag.MethodPAG;
import soot.jimple.spark.pag.Node;
import soot.jimple.spark.pag.Parm;
import soot.jimple.spark.pag.VarNode;
import soot.options.SparkOptions;

public class ShimpleStatementBuilder extends AbstractStmtSwitch {

	private final ShimpleValueBuilder valueBuilder;
	private final SootMethod method;
	private final SparkOptions opts;
	private final MethodPAG mpag;
	private final PAGNodeFactory nodeFactory;
	
	public ShimpleStatementBuilder(ShimpleValueBuilder valueBuilder) {
		this.valueBuilder = valueBuilder;
		this.mpag = valueBuilder.mpag();
		opts = mpag.pag().getOpts();
		this.method = mpag.getMethod();
		this.nodeFactory = PAGNodeFactory.v();
	}
	
	
	@Override
	final public void caseAssignStmt(AssignStmt as) {
		Value l = as.getLeftOp();
		Value r = as.getRightOp();
		if (!(l.getType() instanceof RefLikeType))
			return;
		l.apply(valueBuilder);
		Node dest = valueBuilder.getNode();
		r.apply(valueBuilder);
		Node src = valueBuilder.getNode();
		if (l instanceof InstanceFieldRef) {
			((InstanceFieldRef) l).getBase().apply(valueBuilder);
			mpag.addDereference((VarNode) valueBuilder.getNode());
		}
		if (r instanceof InstanceFieldRef) {
			((InstanceFieldRef) r).getBase().apply(valueBuilder);
			mpag.addDereference((VarNode) valueBuilder.getNode());
		}
		if (r instanceof StaticFieldRef) {
			StaticFieldRef sfr = (StaticFieldRef) r;
			SootFieldRef s = sfr.getFieldRef();
			if (opts.empties_as_allocs()) {
				if (s.declaringClass().getName().equals(
				"java.util.Collections")) {
					if (s.name().equals("EMPTY_SET")) {
						src = nodeFactory.makeAllocNode(RefType
								.v("java.util.HashSet"), RefType
								.v("java.util.HashSet"), method);
					} else if (s.name().equals("EMPTY_MAP")) {
						src = nodeFactory.makeAllocNode(RefType
								.v("java.util.HashMap"), RefType
								.v("java.util.HashMap"), method);
					} else if (s.name().equals("EMPTY_LIST")) {
						src = nodeFactory.makeAllocNode(RefType
								.v("java.util.LinkedList"), RefType
								.v("java.util.LinkedList"), method);
					}
				} else if (s.declaringClass().getName().equals(
				"java.util.Hashtable")) {
					if (s.name().equals("emptyIterator")) {
						src = nodeFactory.makeAllocNode(
								RefType
								.v("java.util.Hashtable$EmptyIterator"),
								RefType
								.v("java.util.Hashtable$EmptyIterator"),
								method);
					} else if (s.name().equals("emptyEnumerator")) {
						src = nodeFactory.makeAllocNode(
								RefType
								.v("java.util.Hashtable$EmptyEnumerator"),
								RefType
								.v("java.util.Hashtable$EmptyEnumerator"),
								method);
					}
				}
			}
		}
		mpag.addInternalEdge(src, dest);
	}

	private Node caseRet() {
		VarNode ret = nodeFactory.makeLocalVarNode(Parm.v(method,
				PointsToAnalysis.RETURN_NODE), method.getReturnType(), method);
		ret.setInterProcSource();
		return ret;
	}
	
	@Override
	final public void caseReturnStmt(ReturnStmt rs) {
		if (!(rs.getOp().getType() instanceof RefLikeType))
			return;
		rs.getOp().apply(valueBuilder);
		Node retNode = valueBuilder.getNode();
		mpag.addInternalEdge(retNode, caseRet());
	}

	@Override
	final public void caseIdentityStmt(IdentityStmt is) {
		if (!(is.getLeftOp().getType() instanceof RefLikeType))
			return;
		is.getLeftOp().apply(valueBuilder);
		Node dest = valueBuilder.getNode();
		is.getRightOp().apply(this);
		Node src = valueBuilder.getNode();
		mpag.addInternalEdge(src, dest);
	}

	@Override
	final public void caseThrowStmt(ThrowStmt ts) {
		ts.getOp().apply(valueBuilder);
		mpag.addOutEdge(valueBuilder.getNode(), nodeFactory.makeThrow());
	}
}
