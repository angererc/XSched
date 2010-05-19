package xsched.analysis.pag;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Context;
import soot.Local;
import soot.PointsToAnalysis;
import soot.PointsToSet;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.jimple.ClassConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.StringConstant;
import soot.jimple.spark.builder.GlobalNodeFactory;
import soot.jimple.spark.internal.TypeManager;
import soot.jimple.spark.pag.AllocDotField;
import soot.jimple.spark.pag.AllocNode;
import soot.jimple.spark.pag.ContextVarNode;
import soot.jimple.spark.pag.FieldRefNode;
import soot.jimple.spark.pag.GlobalVarNode;
import soot.jimple.spark.pag.LocalVarNode;
import soot.jimple.spark.pag.Node;
import soot.jimple.spark.pag.PAG;
import soot.jimple.spark.pag.SparkField;
import soot.jimple.spark.pag.VarNode;
import soot.jimple.spark.sets.P2SetFactory;
import soot.jimple.spark.solver.OnFlyCallGraph;
import soot.options.SparkOptions;
import soot.tagkit.Tag;
import soot.toolkits.scalar.Pair;
import soot.util.ArrayNumberer;
import soot.util.queue.QueueReader;
import xsched.analysis.XSchedAnalyzer;

public class PAGProxy extends PAG {

	private final PAG parent;
	private HashMap<InvokeExpr, NewHBRelationshipRecord> newHBRelationshipRecords = new HashMap<InvokeExpr, NewHBRelationshipRecord>();
	private HashMap<InvokeExpr, NewActivationRecord> newActivationRecords = new HashMap<InvokeExpr, NewActivationRecord>();
	
	public PAGProxy(PAG parent) {
		super(parent.opts());
		this.parent = parent;
	}

	public Collection<NewHBRelationshipRecord> newHBRelationshipRecords() {
		return newHBRelationshipRecords.values();
	}
	
	public Collection<NewActivationRecord> newActivationRecords() {
		return newActivationRecords.values();
	}
	
	@Override
	public boolean addAllocEdge(AllocNode from, VarNode to) {
		// TODO Auto-generated method stub
		return super.addAllocEdge(from, to);
	}

	@Override
	public void addDereference(VarNode base) {
		// TODO Auto-generated method stub
		super.addDereference(base);
	}

	@Override
	public boolean addLoadEdge(FieldRefNode from, VarNode to) {
		// TODO Auto-generated method stub
		return super.addLoadEdge(from, to);
	}

	@Override
	public boolean addSimpleEdge(VarNode from, VarNode to) {
		// TODO Auto-generated method stub
		return super.addSimpleEdge(from, to);
	}

	@Override
	public boolean addStoreEdge(VarNode from, FieldRefNode to) {
		// TODO Auto-generated method stub
		return super.addStoreEdge(from, to);
	}

	private void recordActivationConstructorCall(InvokeExpr expr, Node src, Node tgt) {
		// constructor <init>(java.lang.Object,java.lang.String, ...)
		NewActivationRecord act = newActivationRecords.get(expr);
		if (act == null) {
			act = new NewActivationRecord(expr);
			newActivationRecords.put(expr, act);
		}

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
			assert (act.receivers == null);
			assert(src.getP2SetForReal() != null);
			act.receivers = src.getP2Set();
		} else if (which.equals(1)) {
			assert (act.task == null);
			assert (src instanceof GlobalVarNode);
			Value task = expr.getArgBox(1).getValue();
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
			assert (((Integer) which).intValue() - 2 == act.params.size());
			act.params.add(src);
		}
	}
	
	private void handleNewHBRelationshipDeclaration(InvokeExpr expr, Node src, Node tgt) {
		NewHBRelationshipRecord rel = newHBRelationshipRecords.get(expr);
		if (rel == null) {
			rel = new NewHBRelationshipRecord(expr);
			newHBRelationshipRecords.put(expr, rel);
		}

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
	
	@Override
	protected void recordCallAssign(InvokeExpr expr, Pair<?, ?> assignment) {
		super.recordCallAssign(expr, assignment);
		
		SootMethod method = expr.getMethod();
		if (method.equals(XSchedAnalyzer.ACTIVATION_CONSTRUCTOR0)
				|| method.equals(XSchedAnalyzer.ACTIVATION_CONSTRUCTOR1)
				|| method.equals(XSchedAnalyzer.ACTIVATION_CONSTRUCTOR2)
				|| method.equals(XSchedAnalyzer.ACTIVATION_CONSTRUCTOR3)) {
			
			recordActivationConstructorCall(expr, (Node)assignment.getO1(), (Node)assignment.getO2());
		} else if (expr.getMethod().equals(XSchedAnalyzer.HB_METHOD)) {
			handleNewHBRelationshipDeclaration(expr, (Node)assignment.getO1(), (Node)assignment.getO2());
		}
		
	}

	@Override
	protected boolean addToMap(Map<Object, Object> m, Node key, Node value) {
		// TODO Auto-generated method stub
		return super.addToMap(m, key, value);
	}

	@Override
	public Map<Object, Object> allocEdges() {
		// TODO Auto-generated method stub
		return super.allocEdges();
	}

	@Override
	public Node[] allocInvLookup(VarNode key) {
		// TODO Auto-generated method stub
		return super.allocInvLookup(key);
	}

	@Override
	public Set<Object> allocInvSources() {
		// TODO Auto-generated method stub
		return super.allocInvSources();
	}

	@Override
	public Iterator<Object> allocInvSourcesIterator() {
		// TODO Auto-generated method stub
		return super.allocInvSourcesIterator();
	}

	@Override
	public Node[] allocLookup(AllocNode key) {
		// TODO Auto-generated method stub
		return super.allocLookup(key);
	}

	@Override
	public QueueReader allocNodeListener() {
		// TODO Auto-generated method stub
		return super.allocNodeListener();
	}

	@Override
	public Set<Object> allocSources() {
		// TODO Auto-generated method stub
		return super.allocSources();
	}

	@Override
	public Iterator<Object> allocSourcesIterator() {
		// TODO Auto-generated method stub
		return super.allocSourcesIterator();
	}

	@Override
	public void cleanUpMerges() {
		// TODO Auto-generated method stub
		super.cleanUpMerges();
	}

	@Override
	public boolean doAddAllocEdge(AllocNode from, VarNode to) {
		// TODO Auto-generated method stub
		return super.doAddAllocEdge(from, to);
	}

	@Override
	public boolean doAddLoadEdge(FieldRefNode from, VarNode to) {
		// TODO Auto-generated method stub
		return super.doAddLoadEdge(from, to);
	}

	@Override
	public boolean doAddSimpleEdge(VarNode from, VarNode to) {
		// TODO Auto-generated method stub
		return super.doAddSimpleEdge(from, to);
	}

	@Override
	public boolean doAddStoreEdge(VarNode from, FieldRefNode to) {
		// TODO Auto-generated method stub
		return super.doAddStoreEdge(from, to);
	}

	@Override
	public QueueReader edgeReader() {
		// TODO Auto-generated method stub
		return super.edgeReader();
	}

	@Override
	public AllocDotField findAllocDotField(AllocNode an, SparkField field) {
		// TODO Auto-generated method stub
		return super.findAllocDotField(an, field);
	}

	@Override
	public ContextVarNode findContextVarNode(Object baseValue, Context context) {
		// TODO Auto-generated method stub
		return super.findContextVarNode(baseValue, context);
	}

	@Override
	public FieldRefNode findGlobalFieldRefNode(Object baseValue,
			SparkField field) {
		// TODO Auto-generated method stub
		return super.findGlobalFieldRefNode(baseValue, field);
	}

	@Override
	public GlobalVarNode findGlobalVarNode(Object value) {
		// TODO Auto-generated method stub
		return super.findGlobalVarNode(value);
	}

	@Override
	public FieldRefNode findLocalFieldRefNode(Object baseValue, SparkField field) {
		// TODO Auto-generated method stub
		return super.findLocalFieldRefNode(baseValue, field);
	}

	@Override
	public LocalVarNode findLocalVarNode(Object value) {
		// TODO Auto-generated method stub
		return super.findLocalVarNode(value);
	}

	@Override
	public ArrayNumberer getAllocDotFieldNodeNumberer() {
		// TODO Auto-generated method stub
		return super.getAllocDotFieldNodeNumberer();
	}

	@Override
	public ArrayNumberer getAllocNodeNumberer() {
		// TODO Auto-generated method stub
		return super.getAllocNodeNumberer();
	}

	@Override
	public List<VarNode> getDereferences() {
		// TODO Auto-generated method stub
		return super.getDereferences();
	}

	@Override
	public ArrayNumberer getFieldRefNodeNumberer() {
		// TODO Auto-generated method stub
		return super.getFieldRefNodeNumberer();
	}

	@Override
	public Map<Node, Tag> getNodeTags() {
		// TODO Auto-generated method stub
		return super.getNodeTags();
	}

	@Override
	public int getNumAllocNodes() {
		// TODO Auto-generated method stub
		return super.getNumAllocNodes();
	}

	@Override
	public OnFlyCallGraph getOnFlyCallGraph() {
		// TODO Auto-generated method stub
		return super.getOnFlyCallGraph();
	}

	@Override
	public SparkOptions getOpts() {
		// TODO Auto-generated method stub
		return super.getOpts();
	}

	@Override
	public P2SetFactory getSetFactory() {
		// TODO Auto-generated method stub
		return super.getSetFactory();
	}

	@Override
	public TypeManager getTypeManager() {
		// TODO Auto-generated method stub
		return super.getTypeManager();
	}

	@Override
	public ArrayNumberer getVarNodeNumberer() {
		// TODO Auto-generated method stub
		return super.getVarNodeNumberer();
	}

	@Override
	public Map<Object, Object> loadEdges() {
		// TODO Auto-generated method stub
		return super.loadEdges();
	}

	@Override
	public Node[] loadInvLookup(VarNode key) {
		// TODO Auto-generated method stub
		return super.loadInvLookup(key);
	}

	@Override
	public Set<Object> loadInvSources() {
		// TODO Auto-generated method stub
		return super.loadInvSources();
	}

	@Override
	public Iterator<Object> loadInvSourcesIterator() {
		// TODO Auto-generated method stub
		return super.loadInvSourcesIterator();
	}

	@Override
	public Node[] loadLookup(FieldRefNode key) {
		// TODO Auto-generated method stub
		return super.loadLookup(key);
	}

	@Override
	public Set<Object> loadSources() {
		// TODO Auto-generated method stub
		return super.loadSources();
	}

	@Override
	public Iterator<Object> loadSourcesIterator() {
		// TODO Auto-generated method stub
		return super.loadSourcesIterator();
	}

	@Override
	protected Node[] lookup(Map<Object, Object> m, Object key) {
		// TODO Auto-generated method stub
		return super.lookup(m, key);
	}

	@Override
	public AllocDotField makeAllocDotField(AllocNode an, SparkField field) {
		// TODO Auto-generated method stub
		return super.makeAllocDotField(an, field);
	}

	@Override
	public AllocNode makeAllocNode(Object newExpr, Type type, SootMethod m) {
		// TODO Auto-generated method stub
		return super.makeAllocNode(newExpr, type, m);
	}

	@Override
	public AllocNode makeClassConstantNode(ClassConstant cc) {
		// TODO Auto-generated method stub
		return super.makeClassConstantNode(cc);
	}

	@Override
	public ContextVarNode makeContextVarNode(LocalVarNode base, Context context) {
		// TODO Auto-generated method stub
		return super.makeContextVarNode(base, context);
	}

	@Override
	public ContextVarNode makeContextVarNode(Object baseValue, Type baseType,
			Context context, SootMethod method) {
		// TODO Auto-generated method stub
		return super.makeContextVarNode(baseValue, baseType, context, method);
	}

	@Override
	public FieldRefNode makeFieldRefNode(VarNode base, SparkField field) {
		// TODO Auto-generated method stub
		return super.makeFieldRefNode(base, field);
	}

	@Override
	public FieldRefNode makeGlobalFieldRefNode(Object baseValue, Type baseType,
			SparkField field) {
		// TODO Auto-generated method stub
		return super.makeGlobalFieldRefNode(baseValue, baseType, field);
	}

	@Override
	public GlobalVarNode makeGlobalVarNode(Object value, Type type) {
		// TODO Auto-generated method stub
		return super.makeGlobalVarNode(value, type);
	}

	@Override
	public FieldRefNode makeLocalFieldRefNode(Object baseValue, Type baseType,
			SparkField field, SootMethod method) {
		// TODO Auto-generated method stub
		return super.makeLocalFieldRefNode(baseValue, baseType, field, method);
	}

	@Override
	public LocalVarNode makeLocalVarNode(Object value, Type type,
			SootMethod method) {
		// TODO Auto-generated method stub
		return super.makeLocalVarNode(value, type, method);
	}

	@Override
	public AllocNode makeStringConstantNode(String s) {
		// TODO Auto-generated method stub
		return super.makeStringConstantNode(s);
	}

	@Override
	public GlobalNodeFactory nodeFactory() {
		// TODO Auto-generated method stub
		return super.nodeFactory();
	}

	@Override
	public OnFlyCallGraph ofcg() {
		// TODO Auto-generated method stub
		return super.ofcg();
	}

	@Override
	public PointsToSet reachingObjects(Context c, Local l, SootField f) {
		// TODO Auto-generated method stub
		return super.reachingObjects(c, l, f);
	}

	@Override
	public PointsToSet reachingObjects(Context c, Local l) {
		// TODO Auto-generated method stub
		return super.reachingObjects(c, l);
	}

	@Override
	public PointsToSet reachingObjects(Local l, SootField f) {
		// TODO Auto-generated method stub
		return super.reachingObjects(l, f);
	}

	@Override
	public PointsToSet reachingObjects(Local l) {
		// TODO Auto-generated method stub
		return super.reachingObjects(l);
	}

	@Override
	public PointsToSet reachingObjects(PointsToSet s, SootField f) {
		// TODO Auto-generated method stub
		return super.reachingObjects(s, f);
	}

	@Override
	public PointsToSet reachingObjects(SootField f) {
		// TODO Auto-generated method stub
		return super.reachingObjects(f);
	}

	@Override
	public PointsToSet reachingObjectsOfArrayElement(PointsToSet s) {
		// TODO Auto-generated method stub
		return super.reachingObjectsOfArrayElement(s);
	}

	@Override
	public void setOnFlyCallGraph(OnFlyCallGraph ofcg) {
		// TODO Auto-generated method stub
		super.setOnFlyCallGraph(ofcg);
	}

	@Override
	public Map<Object, Object> simpleEdges() {
		// TODO Auto-generated method stub
		return super.simpleEdges();
	}

	@Override
	public Node[] simpleInvLookup(VarNode key) {
		// TODO Auto-generated method stub
		return super.simpleInvLookup(key);
	}

	@Override
	public Set<Object> simpleInvSources() {
		// TODO Auto-generated method stub
		return super.simpleInvSources();
	}

	@Override
	public Iterator<Object> simpleInvSourcesIterator() {
		// TODO Auto-generated method stub
		return super.simpleInvSourcesIterator();
	}

	@Override
	public Node[] simpleLookup(VarNode key) {
		// TODO Auto-generated method stub
		return super.simpleLookup(key);
	}

	@Override
	public Set<Object> simpleSources() {
		// TODO Auto-generated method stub
		return super.simpleSources();
	}

	@Override
	public Iterator<Object> simpleSourcesIterator() {
		// TODO Auto-generated method stub
		return super.simpleSourcesIterator();
	}

	@Override
	public Map<Object, Object> storeEdges() {
		// TODO Auto-generated method stub
		return super.storeEdges();
	}

	@Override
	public Node[] storeInvLookup(FieldRefNode key) {
		// TODO Auto-generated method stub
		return super.storeInvLookup(key);
	}

	@Override
	public Set<Object> storeInvSources() {
		// TODO Auto-generated method stub
		return super.storeInvSources();
	}

	@Override
	public Iterator<Object> storeInvSourcesIterator() {
		// TODO Auto-generated method stub
		return super.storeInvSourcesIterator();
	}

	@Override
	public Node[] storeLookup(VarNode key) {
		// TODO Auto-generated method stub
		return super.storeLookup(key);
	}

	@Override
	public Set<Object> storeSources() {
		// TODO Auto-generated method stub
		return super.storeSources();
	}

	@Override
	public Iterator<Object> storeSourcesIterator() {
		// TODO Auto-generated method stub
		return super.storeSourcesIterator();
	}

}
