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
import soot.jimple.spark.pag.PAG;
import soot.jimple.spark.pag.VarNode;
import soot.shimple.AbstractShimpleValueSwitch;
import soot.shimple.PhiExpr;
import soot.toolkits.scalar.Pair;

public class ShimpleValueBuilder extends AbstractShimpleValueSwitch {
	private final SootMethod method;
	private final MethodPAG mpag;
	private final PAGNodeFactory nodeFactory;
	
	public ShimpleValueBuilder(MethodPAG mpag) {
		this.mpag = mpag;		
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
	@Deprecated
	public void setResult(Object node) {
		throw new RuntimeException("Don't use me, use setResult(node, value)");
	}
	private void setResult(Node node, Value value) {
		mpag.registerNodeForValue(node, value);
		super.setResult(node);
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
		setResult(phiNode, e);
	}

	@Override	
	final public void caseArrayRef(ArrayRef ar) {
		caseLocal((Local) ar.getBase());
		
		Node result = nodeFactory.makeFieldRefNode((VarNode) getNode(), ArrayElement.v());
		setResult(result, ar);		
	}

	@Override
	final public void caseCastExpr(CastExpr ce) {
		Pair<CastExpr,String> castPair = new Pair<CastExpr,String>(ce, PointsToAnalysis.CAST_NODE);
		ce.getOp().apply(this);
		Node opNode = getNode();
		Node castNode = nodeFactory.makeLocalVarNode(castPair, ce.getCastType(), method);
		mpag.addInternalEdge(opNode, castNode);
		setResult(castNode, ce);		
	}

	@Override
	final public void caseCaughtExceptionRef(CaughtExceptionRef cer) {
		Node node = nodeFactory.makeThrow();
		setResult(node, cer);		
	}

	@Override
	final public void caseInstanceFieldRef(InstanceFieldRef ifr) {
		Node node;
		if (PAG.opts().field_based() || PAG.opts().vta()) {
			node = nodeFactory.makeGlobalVarNode(ifr.getField(), ifr.getField().getType());			
		} else {
			node = nodeFactory.makeLocalFieldRefNode(ifr.getBase(), ifr.getBase().getType(), ifr.getField(), method);
		}
		setResult(node, ifr);		
	}

	@Override
	final public void caseLocal(Local l) {
		Node node = nodeFactory.makeLocalVarNode(l, l.getType(), method);
		setResult(node, l);		
	}

	@Override
	final public void caseNewArrayExpr(NewArrayExpr nae) {
		Node node = nodeFactory.makeAllocNode(nae, nae.getType(), method);
		setResult(node, nae);		
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
		Node node;
		if (PAG.opts().merge_stringbuffer() && isStringBuffer(ne.getType())) {
			node = nodeFactory.makeAllocNode(ne.getType(), ne.getType(), null);
		} else {
			node = nodeFactory.makeAllocNode(ne, ne.getType(), method);
		}
		setResult(node, ne);		
	}

	@Override
	final public void caseNewMultiArrayExpr(NewMultiArrayExpr nmae) {		
		ArrayType type = (ArrayType) nmae.getType();
		AllocNode prevAn = nodeFactory.makeAllocNode(new Pair<NewMultiArrayExpr, Integer>(nmae, new Integer(
				type.numDimensions)), type, method);
		VarNode prevVn = nodeFactory.makeLocalVarNode(prevAn, prevAn.getType(), method);
		mpag.addInternalEdge(prevAn, prevVn);
		setResult(prevAn, nmae);		
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
		setResult(nodeFactory.makeParm(method, pr.getIndex()), pr);
	}

	@Override
	final public void caseStaticFieldRef(StaticFieldRef sfr) {
		setResult(nodeFactory.makeGlobalVarNode(sfr.getField(), sfr.getField()
				.getType()), sfr);
	}

	@Override
	final public void caseStringConstant(StringConstant sc) {
		AllocNode stringConstant;
		if (PAG.opts().string_constants()
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
		setResult(stringConstantLocal, sc);		
	}

	@Override
	final public void caseThisRef(ThisRef tr) {
		setResult(nodeFactory.makeThis(method), tr);
	}

	@Override
	final public void caseNullConstant(NullConstant nr) {
		setResult(null, nr);
	}

	@Override
	final public void caseClassConstant(ClassConstant cc) {
		AllocNode classConstant = nodeFactory.makeClassConstantNode(cc);
		VarNode classConstantLocal = nodeFactory.makeGlobalVarNode(classConstant,
				RefType.v("java.lang.Class"));
		mpag.addConstantEdge(classConstant, classConstantLocal);
		setResult(classConstantLocal, cc);		
	}

	@Override
	final public void defaultCase(Object v) {
		throw new RuntimeException("failed to handle " + v);
	}
}
