/* Soot - a J*va Optimization Framework
 * Copyright (C) 2003 Ondrej Lhotak
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package soot.jimple.spark.pag;

import java.util.*;

import soot.*;
import soot.jimple.*;
import soot.jimple.spark.builder.*;
import soot.util.*;
import soot.util.queue.*;

/**
 * Part of a pointer assignment graph for a single method.
 * 
 * @author Ondrej Lhotak
 */
public final class MethodPAG {

	private static HashMap<SootMethod, MethodPAG> methodToMethodPAG = new HashMap<SootMethod,MethodPAG>();
	//this PAG contains all the MethodPAGs that created nodes in this pag
	public static MethodPAG methodPAGForMethod(SootMethod m) {
		MethodPAG methodPag = methodToMethodPAG.get(m);
		if(methodPag == null) {
			methodPag = new MethodPAG(m);
			methodToMethodPAG.put(m, methodPag);
		}
		return methodPag;
	}
	
	private final SootMethod method;
	private final PAGNodeFactory nodeFactory;
	
	protected MethodPAG(SootMethod m) {		
		this.method = m;
		this.nodeFactory = PAGNodeFactory.v();
	}

	public SootMethod getMethod() {
		return method;
	}

	public boolean hasBeenBuilt() {
		return this.hasBeenBuilt;
	}
	
	/**
	 * Adds this method to the main PAG, with all VarNodes parameterized by
	 * varNodeParameter.
	 */
	public void addToPAG(PAG pag, Context varNodeParameter) {
		assert(hasBeenBuilt);
		if (varNodeParameter == null) {
			if (pag.hasBeenAdded(this))
				return;
			
			pag.setHasBeenAdded(this);			
		} else {
			if(pag.hasBeenAdded(this, varNodeParameter))
				return;
			
			pag.setHasBeenAdded(this, varNodeParameter);			
		}
		QueueReader<?> reader = (QueueReader<?>) internalReader.clone();
		while (reader.hasNext()) {
			Node src = (Node) reader.next();
			src = parameterize(src, varNodeParameter);
			Node dst = (Node) reader.next();
			dst = parameterize(dst, varNodeParameter);
			pag.addEdge(src, dst);
		}
		reader = (QueueReader<?>) constantReader.clone();
		while (reader.hasNext()) {
			Node src = (Node) reader.next();
			Node dst = (Node) reader.next();
			pag.addEdge(src, dst);
		}
		reader = (QueueReader<?>) dereferencesReader.clone();
		while (reader.hasNext()) {
			VarNode node = (VarNode) reader.next();
			pag.addDereference(node);
		}
		reader = (QueueReader<?>) inReader.clone();
		while (reader.hasNext()) {
			Node src = (Node) reader.next();
			Node dst = (Node) reader.next();
			dst = parameterize(dst, varNodeParameter);
			pag.addEdge(src, dst);
		}
		reader = (QueueReader<?>) outReader.clone();
		while (reader.hasNext()) {
			Node src = (Node) reader.next();
			src = parameterize(src, varNodeParameter);
			Node dst = (Node) reader.next();
			pag.addEdge(src, dst);
		}
	}

	public void addConstantEdge(AllocNode src, Node dst) {
		assert(!hasBeenBuilt);
		constantEdges.add(src);
		constantEdges.add(dst);		
	}

	public void addInternalEdge(Node src, Node dst) {
		if (src == null)
			return;
		assert(!hasBeenBuilt);
		internalEdges.add(src);
		internalEdges.add(dst);		
	}

	public void addInEdge(Node src, Node dst) {
		if (src == null)
			return;
		assert(!hasBeenBuilt);
		inEdges.add(src);
		inEdges.add(dst);		
	}

	public void addOutEdge(Node src, Node dst) {
		if (src == null)
			return;
		assert(!hasBeenBuilt);
		outEdges.add(src);
		outEdges.add(dst);		
	}

	public void addDereference(VarNode base) {
		dereferences.add(base);
	}

	protected boolean hasBeenBuilt = false;
	
	private final ChunkedQueue<VarNode> dereferences = new ChunkedQueue<VarNode>();
	private final ChunkedQueue<Node> internalEdges = new ChunkedQueue<Node>();
	private final ChunkedQueue<Node> constantEdges = new ChunkedQueue<Node>();
	private final ChunkedQueue<Node> inEdges = new ChunkedQueue<Node>();
	private final ChunkedQueue<Node> outEdges = new ChunkedQueue<Node>();
	private final QueueReader<VarNode> dereferencesReader = dereferences
			.reader();
	private final QueueReader<Node> internalReader = internalEdges.reader();
	private final QueueReader<Node> constantReader = constantEdges.reader();
	private final QueueReader<Node> inReader = inEdges.reader();
	private final QueueReader<Node> outReader = outEdges.reader();

	public void build() {
		if (hasBeenBuilt)
			return;
		hasBeenBuilt = true;
		if (method.isNative()) {
			if (PAG.opts().simulate_natives()) {
				buildNative();
			}
		} else {
			if (method.isConcrete() && !method.isPhantom()) {
				buildNormal();
			}
		}
		addMiscEdges();
	}

	protected VarNode parameterize(LocalVarNode vn, Context varNodeParameter) {
		SootMethod m = vn.getMethod();
		if (m != method && m != null)
			throw new RuntimeException("VarNode " + vn + " with method " + m
					+ " parameterized in method " + method);
		// System.out.println( "parameterizing "+vn+" with "+varNodeParameter );
		return nodeFactory.makeContextVarNode(vn, varNodeParameter);
	}

	protected FieldRefNode parameterize(FieldRefNode frn,
			Context varNodeParameter) {
		return nodeFactory.makeFieldRefNode((VarNode) parameterize(frn
				.getBase(), varNodeParameter), frn.getField());
	}

	public Node parameterize(Node n, Context varNodeParameter) {
		if (varNodeParameter == null)
			return n;
		if (n instanceof LocalVarNode)
			return parameterize((LocalVarNode) n, varNodeParameter);
		if (n instanceof FieldRefNode)
			return parameterize((FieldRefNode) n, varNodeParameter);
		return n;
	}

	public Node buildNodeForValue(Value value) {
		ShimpleValueBuilder valueBuilder = new ShimpleValueBuilder(this);
		value.apply(valueBuilder);
		return valueBuilder.getNode();
	}

	protected void buildNormal() {
		ShimpleValueBuilder valueBuilder = new ShimpleValueBuilder(this);
		ShimpleStatementBuilder statementBuilder = new ShimpleStatementBuilder(
				valueBuilder);

		Body b = method.retrieveActiveBody();
		Iterator<?> unitsIt = b.getUnits().iterator();
		while (unitsIt.hasNext()) {
			Stmt s = (Stmt) unitsIt.next();

			if (!s.containsInvokeExpr()) {
				s.apply(statementBuilder);
			}
		}
	}

	protected void buildNative() {
		ValNode thisNode = null;
		ValNode retNode = null;
		if (!method.isStatic()) {
			thisNode = (ValNode) nodeFactory.makeThis(method);
		}
		if (method.getReturnType() instanceof RefLikeType) {
			retNode = (ValNode) nodeFactory.makeRet(method);
		}
		ValNode[] args = new ValNode[method.getParameterCount()];
		for (int i = 0; i < method.getParameterCount(); i++) {
			if (!(method.getParameterType(i) instanceof RefLikeType))
				continue;
			args[i] = (ValNode) nodeFactory.makeParm(method, i);
		}
		PAG.nativeMethodDriver().process(method, thisNode, retNode, args);
	}

	protected void addMiscEdges() {
		// Add node for parameter (String[]) in main method
		if (method.getSubSignature().equals(
				SootMethod.getSubSignature("main", new SingletonList(ArrayType
						.v(RefType.v("java.lang.String"), 1)), VoidType.v()))) {
			addInEdge(addArgv(), nodeFactory.makeParm(method, 0));
		} else

		if (method
				.getSignature()
				.equals(
						"<java.lang.Thread: void <init>(java.lang.ThreadGroup,java.lang.String)>")) {
			addInEdge(addMainThread(), nodeFactory.makeThis(method));
			addInEdge(addMainThreadGroup(), nodeFactory.makeParm(method, 0));
		} else

		if (method.getSignature().equals(
				"<java.lang.ref.Finalizer: void <init>(java.lang.Object)>")) {
			addInEdge(nodeFactory.makeThis(method), nodeFactory
					.makeFinalizeQueue());
		} else

		if (method.getSignature().equals(
				"<java.lang.ref.Finalizer: void runFinalizer()>")) {
			addInEdge(nodeFactory.makeFinalizeQueue(), nodeFactory
					.makeThis(method));
		} else

		if (method.getSignature().equals(
				"<java.lang.ref.Finalizer: void access$100(java.lang.Object)>")) {
			addInEdge(nodeFactory.makeFinalizeQueue(), nodeFactory.makeParm(
					method, 0));
		} else

		if (method.getSignature().equals(
				"<java.lang.ClassLoader: void <init>()>")) {
			addInEdge(addDefaultClassLoader(), nodeFactory.makeThis(method));
		} else

		if (method.getSignature().equals("<java.lang.Thread: void exit()>")) {
			addInEdge(addMainThread(), nodeFactory.makeThis(method));
		} else

		if (method
				.getSignature()
				.equals(
						"<java.security.PrivilegedActionException: void <init>(java.lang.Exception)>")) {
			addInEdge(nodeFactory.makeThrow(), nodeFactory.makeParm(method, 0));
			addInEdge(addPrivilegedActionException(), nodeFactory
					.makeThis(method));
		}

		if (method.getNumberedSubSignature().equals(sigCanonicalize)) {
			SootClass cl = method.getDeclaringClass();
			while (true) {
				if (cl.equals(Scene.v().getSootClass("java.io.FileSystem"))) {
					addInEdge(addCanonicalPath(), nodeFactory.makeRet(method));
				}
				if (!cl.hasSuperclass())
					break;
				cl = cl.getSuperclass();
			}
		}

		boolean isImplicit = false;
		for (SootMethod implicitMethod : EntryPoints.v().implicit()) {
			if (implicitMethod.getNumberedSubSignature().equals(
					method.getNumberedSubSignature())) {
				isImplicit = true;
				break;
			}
		}
		if (isImplicit) {
			SootClass c = method.getDeclaringClass();
			outer: do {
				while (!c.getName().equals("java.lang.ClassLoader")) {
					if (!c.hasSuperclass()) {
						break outer;
					}
					c = c.getSuperclass();
				}
				if (method.getName().equals("<init>"))
					continue;
				addInEdge(addDefaultClassLoader(), nodeFactory.makeThis(method));
				addInEdge(addMainClassNameString(), nodeFactory.makeParm(
						method, 0));
			} while (false);
		}
	}

	protected final NumberedString sigCanonicalize = Scene.v()
			.getSubSigNumberer().findOrAdd(
					"java.lang.String canonicalize(java.lang.String)");

	private Node addDefaultClassLoader() {
		AllocNode a = nodeFactory.makeAllocNode(
				PointsToAnalysis.DEFAULT_CLASS_LOADER, AnySubType.v(RefType
						.v("java.lang.ClassLoader")), null);
		VarNode v = nodeFactory.makeGlobalVarNode(
				PointsToAnalysis.DEFAULT_CLASS_LOADER_LOCAL, RefType
						.v("java.lang.ClassLoader"));
		constantEdges.add(a);
		constantEdges.add(v);		
		return v;
	}

	private Node addMainClassNameString() {
		AllocNode a = nodeFactory.makeAllocNode(
				PointsToAnalysis.MAIN_CLASS_NAME_STRING, RefType
						.v("java.lang.String"), null);
		VarNode v = nodeFactory.makeGlobalVarNode(
				PointsToAnalysis.MAIN_CLASS_NAME_STRING_LOCAL, RefType
						.v("java.lang.String"));
		constantEdges.add(a);
		constantEdges.add(v);
		return v;
	}

	private Node addMainThreadGroup() {
		AllocNode threadGroupNode = nodeFactory.makeAllocNode(
				PointsToAnalysis.MAIN_THREAD_GROUP_NODE, RefType
						.v("java.lang.ThreadGroup"), null);
		VarNode threadGroupNodeLocal = nodeFactory.makeGlobalVarNode(
				PointsToAnalysis.MAIN_THREAD_GROUP_NODE_LOCAL, RefType
						.v("java.lang.ThreadGroup"));		
		constantEdges.add(threadGroupNode);
		constantEdges.add(threadGroupNodeLocal);
		return threadGroupNodeLocal;
	}

	private Node addPrivilegedActionException() {
		AllocNode a = nodeFactory.makeAllocNode(
				PointsToAnalysis.PRIVILEGED_ACTION_EXCEPTION,
				AnySubType.v(RefType
						.v("java.security.PrivilegedActionException")), null);
		VarNode v = nodeFactory.makeGlobalVarNode(
				PointsToAnalysis.PRIVILEGED_ACTION_EXCEPTION_LOCAL, RefType
						.v("java.security.PrivilegedActionException"));
		constantEdges.add(a);
		constantEdges.add(v);
		return v;
	}

	private Node addCanonicalPath() {
		AllocNode a = nodeFactory.makeAllocNode(
				PointsToAnalysis.CANONICAL_PATH, RefType.v("java.lang.String"),
				null);
		VarNode v = nodeFactory.makeGlobalVarNode(
				PointsToAnalysis.CANONICAL_PATH_LOCAL, RefType
						.v("java.lang.String"));
		constantEdges.add(a);
		constantEdges.add(v);
		return v;
	}

	private Node addMainThread() {
		AllocNode threadNode = nodeFactory.makeAllocNode(
				PointsToAnalysis.MAIN_THREAD_NODE, RefType
						.v("java.lang.Thread"), null);
		VarNode threadNodeLocal = nodeFactory.makeGlobalVarNode(
				PointsToAnalysis.MAIN_THREAD_NODE_LOCAL, RefType
						.v("java.lang.Thread"));
		constantEdges.add(threadNode);
		constantEdges.add(threadNodeLocal);
		return threadNodeLocal;
	}

	private Node addArgv() {
		AllocNode argv = nodeFactory.makeAllocNode(
				PointsToAnalysis.STRING_ARRAY_NODE, ArrayType.v(RefType
						.v("java.lang.String"), 1), null);
		VarNode sanl = nodeFactory.makeGlobalVarNode(
				PointsToAnalysis.STRING_ARRAY_NODE_LOCAL, ArrayType.v(RefType
						.v("java.lang.String"), 1));
		AllocNode stringNode = nodeFactory.makeAllocNode(
				PointsToAnalysis.STRING_NODE, RefType.v("java.lang.String"),
				null);
		VarNode stringNodeLocal = nodeFactory.makeGlobalVarNode(
				PointsToAnalysis.STRING_NODE_LOCAL, RefType
						.v("java.lang.String"));
		
		constantEdges.add(argv);
		constantEdges.add(sanl);
		
		constantEdges.add(stringNode);
		constantEdges.add(stringNodeLocal);
		
		constantEdges.add(stringNodeLocal);
		constantEdges.add(nodeFactory.makeFieldRefNode(sanl, ArrayElement.v()));
		return sanl;
	}
}
