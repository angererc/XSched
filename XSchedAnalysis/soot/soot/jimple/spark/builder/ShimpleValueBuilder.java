package soot.jimple.spark.builder;

import soot.ArrayType;
import soot.Local;
import soot.PointsToAnalysis;
import soot.RefType;
import soot.Scene;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.CastExpr;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.ClassConstant;
import soot.jimple.InstanceFieldRef;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.NullConstant;
import soot.jimple.ParameterRef;
import soot.jimple.StaticFieldRef;
import soot.jimple.StringConstant;
import soot.jimple.ThisRef;
import soot.jimple.spark.pag.AllocNode;
import soot.jimple.spark.pag.ArrayElement;
import soot.jimple.spark.pag.MethodPAG;
import soot.jimple.spark.pag.Node;
import soot.jimple.spark.pag.VarNode;
import soot.options.SparkOptions;
import soot.shimple.AbstractShimpleValueSwitch;
import soot.shimple.PhiExpr;
import soot.toolkits.scalar.Pair;

public class ShimpleValueBuilder extends AbstractShimpleValueSwitch {
	private final SootMethod method;
	private final SparkOptions opts;
	private final MethodPAG mpag;
	private final PAGNodeFactory nodeFactory;
	
	public ShimpleValueBuilder(MethodPAG mpag) {
		this.mpag = mpag;
		opts = mpag.pag().getOpts();
		this.method = mpag.getMethod();
		this.nodeFactory = PAGNodeFactory.v();
	}
	
	public MethodPAG mpag() {
		return mpag;
	}
	
	final public Node getNode() {
		return (Node) getResult();
	}
	
	
	
	@Override
	final public void casePhiExpr(PhiExpr e) {
		Pair<PhiExpr,String> phiPair = new Pair<PhiExpr,String>(e, PointsToAnalysis.PHI_NODE);
		Node phiNode = nodeFactory.makeLocalVarNode(phiPair, e.getType(), method);
		for (Value op : e.getValues()) {
			op.apply(this);
			Node opNode = getNode();
			mpag.addInternalEdge(opNode, phiNode);
		}
		setResult(phiNode);
	}

	@Override	
	final public void caseArrayRef(ArrayRef ar) {
		caseLocal((Local) ar.getBase());
		
		Node result = nodeFactory.makeFieldRefNode((VarNode) getNode(), ArrayElement.v());
		setResult(result);
	}

	@Override
	final public void caseCastExpr(CastExpr ce) {
		Pair<CastExpr,String> castPair = new Pair<CastExpr,String>(ce, PointsToAnalysis.CAST_NODE);
		ce.getOp().apply(this);
		Node opNode = getNode();
		Node castNode = nodeFactory.makeLocalVarNode(castPair, ce.getCastType(), method);
		mpag.addInternalEdge(opNode, castNode);
		setResult(castNode);
	}

	@Override
	final public void caseCaughtExceptionRef(CaughtExceptionRef cer) {
		setResult(nodeFactory.makeThrow());
	}

	@Override
	final public void caseInstanceFieldRef(InstanceFieldRef ifr) {
		if (opts.field_based() || opts.vta()) {
			setResult(nodeFactory.makeGlobalVarNode(ifr.getField(), ifr.getField()
					.getType()));
		} else {
			setResult(nodeFactory.makeLocalFieldRefNode(ifr.getBase(), ifr.getBase()
					.getType(), ifr.getField(), method));
		}
	}

	@Override
	final public void caseLocal(Local l) {
		setResult(nodeFactory.makeLocalVarNode(l, l.getType(), method));
	}

	@Override
	final public void caseNewArrayExpr(NewArrayExpr nae) {
		setResult(nodeFactory.makeAllocNode(nae, nae.getType(), method));
	}

	private boolean isStringBuffer(Type t) {
		if (!(t instanceof RefType))
			return false;
		RefType rt = (RefType) t;
		String s = rt.toString();
		if (s.equals("java.lang.StringBuffer"))
			return true;
		if (s.equals("java.lang.StringBuilder"))
			return true;
		return false;
	}

	@Override
	final public void caseNewExpr(NewExpr ne) {
		if (opts.merge_stringbuffer() && isStringBuffer(ne.getType())) {
			setResult(nodeFactory.makeAllocNode(ne.getType(), ne.getType(), null));
		} else {
			setResult(nodeFactory.makeAllocNode(ne, ne.getType(), method));
		}
	}

	@Override
	final public void caseNewMultiArrayExpr(NewMultiArrayExpr nmae) {
		ArrayType type = (ArrayType) nmae.getType();
		AllocNode prevAn = nodeFactory.makeAllocNode(new Pair<NewMultiArrayExpr, Integer>(nmae, new Integer(
				type.numDimensions)), type, method);
		VarNode prevVn = nodeFactory.makeLocalVarNode(prevAn, prevAn.getType(), method);
		mpag.addInternalEdge(prevAn, prevVn);
		setResult(prevAn);
		while (true) {
			Type t = type.getElementType();
			if (!(t instanceof ArrayType))
				break;
			type = (ArrayType) t;
			AllocNode an = nodeFactory.makeAllocNode(new Pair<NewMultiArrayExpr,Integer>(nmae, new Integer(
					type.numDimensions)), type, method);
			VarNode vn = nodeFactory.makeLocalVarNode(an, an.getType(), method);
			mpag.addInternalEdge(an, vn);
			mpag.addInternalEdge(vn, nodeFactory.makeFieldRefNode(prevVn, ArrayElement
					.v()));
			prevAn = an;
			prevVn = vn;
		}
	}

	@Override
	final public void caseParameterRef(ParameterRef pr) {
		setResult(nodeFactory.makeParm(method, pr.getIndex()));
	}

	@Override
	final public void caseStaticFieldRef(StaticFieldRef sfr) {
		setResult(nodeFactory.makeGlobalVarNode(sfr.getField(), sfr.getField()
				.getType()));
	}

	@Override
	final public void caseStringConstant(StringConstant sc) {
		AllocNode stringConstant;
		if (opts.string_constants()
				|| Scene.v().containsClass(sc.value)
				|| (sc.value.length() > 0 && sc.value.charAt(0) == '[')) {
			stringConstant = nodeFactory.makeStringConstantNode(sc.value);
		} else {
			stringConstant = nodeFactory.makeAllocNode(PointsToAnalysis.STRING_NODE,
					RefType.v("java.lang.String"), null);
		}
		VarNode stringConstantLocal = nodeFactory.makeGlobalVarNode(stringConstant,
				RefType.v("java.lang.String"));
		mpag.addConstantEdge(stringConstant, stringConstantLocal);
		setResult(stringConstantLocal);
	}

	@Override
	final public void caseThisRef(ThisRef tr) {
		setResult(nodeFactory.makeThis(method));
	}

	@Override
	final public void caseNullConstant(NullConstant nr) {
		setResult(null);
	}

	@Override
	final public void caseClassConstant(ClassConstant cc) {
		AllocNode classConstant = nodeFactory.makeClassConstantNode(cc);
		VarNode classConstantLocal = nodeFactory.makeGlobalVarNode(classConstant,
				RefType.v("java.lang.Class"));
		mpag.addConstantEdge(classConstant, classConstantLocal);		
		setResult(classConstantLocal);
	}

	@Override
	final public void defaultCase(Object v) {
		throw new RuntimeException("failed to handle " + v);
	}
}
