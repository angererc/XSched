/* Soot - a J*va Optimization Framework
 * Copyright (C) 2002 Ondrej Lhotak
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

import soot.*;
import soot.util.LargeNumberedMap;

import java.util.*;

/**
 * Represents a simple variable node (Green) in the pointer assignment graph
 * that is specific to a particular method invocation.
 * 
 * @author Ondrej Lhotak
 */
public class LocalVarNode extends VarNode {
	
	private static final LargeNumberedMap internalizedLocals = new LargeNumberedMap( Scene.v().getLocalNumberer() );
	private static final HashMap<Object,LocalVarNode> internalizedVals = new HashMap<Object,LocalVarNode>();
	public static LocalVarNode localVarNode(Object value) {
		if(value instanceof Local)
			return (LocalVarNode) internalizedLocals.get((Local)value);
		else
			return internalizedVals.get(value);
	}
	public static LocalVarNode internalized(Object variable, Type t, SootMethod m) {
		return internalize(new LocalVarNode(variable, t, m));
	}
	
	protected static LocalVarNode internalize(LocalVarNode node) {		
		if(node.variable instanceof Local) {
			Local val = (Local)node.variable;
			if(val.getNumber() == 0) Scene.v().getLocalNumberer().add(val);
			LocalVarNode ret = (LocalVarNode)internalizedLocals.get(val);
			if(ret == null) {
				ret = new LocalVarNode(node.variable, node.type, node.method);
				VarNode.nodeNumberer.add(ret);
				internalizedLocals.put(val, ret);
				ret.addNodeTag(node.method);
			} else if( !( ret.getType().equals( node.type ) ) ) {
                throw new RuntimeException( "Value "+val+" of type "+node.type+
                        " previously had type "+ret.getType() );
            }
			return ret;
		} else {
			LocalVarNode ret = internalizedVals.get(node.variable);
			if(ret == null) {
				ret = new LocalVarNode(node.variable, node.type, node.method);
				VarNode.nodeNumberer.add(ret);
				internalizedVals.put(node.variable, ret);
			} else if( !( ret.getType().equals( node.type ) ) ) {
	            throw new RuntimeException( "Value "+node.variable+" of type "+node.type+
	                    " previously had type "+ret.getType() );
	        }
			return ret;
		}
	}
	
	public ContextVarNode context(Object context) {
		return cvns == null ? null : cvns.get(context);
	}

	public SootMethod getMethod() {
		return method;
	}

	public String toString() {
		return "LocalVarNode " + getNumber() + " " + variable + " " + method;
	}

	/* End of public methods. */

	protected LocalVarNode(Object variable, Type t, SootMethod m) {
		super(variable, t);
		this.method = m;
		// if( m == null ) throw new RuntimeException(
		// "method shouldn't be null" );
	}

	/** Registers a cvn as having this node as its base. */
	void addContext(ContextVarNode cvn, Object context) {
		if (cvns == null)
			cvns = new HashMap<Object, ContextVarNode>();
		cvns.put(context, cvn);
	}

	/* End of package methods. */

	protected Map<Object, ContextVarNode> cvns;
	protected SootMethod method;
	
	
}
