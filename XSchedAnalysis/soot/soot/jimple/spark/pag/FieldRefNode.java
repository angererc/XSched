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

import java.util.HashMap;

import soot.jimple.spark.builder.PAGNodeFactory;
import soot.util.ArrayNumberer;

/** Represents a field reference node (Red) in the pointer assignment graph.
 * @author Ondrej Lhotak
 */
public class FieldRefNode extends ValNode {

	private final static ArrayNumberer nodeNumberer = new ArrayNumberer();
	public static ArrayNumberer fieldRefNodeNumberer() {
		return nodeNumberer;
	}
	@Override
	protected void fetchNumber() {
		nodeNumberer.add(this);
	}

	public FieldRefNode internalized() {
		//a field ref is defined by its base and the field and the PAGNodeFactory already does that
		//so no reason to internalize much here...
		if(this.getNumber() > 0)
			return this;

		this.fetchNumber();
		if( base instanceof LocalVarNode ) {
			this.addNodeTag( ((LocalVarNode) base).getMethod() );
		} else {
			this.addNodeTag( null );
		}
		return this;
	}

	/** Returns the base of this field reference. */
	public VarNode getBase() { return base; }
	
	Node getReplacement(PAG pag, HashMap<Node,Node> replacements) {
		Node replacement = super.getReplacement(pag, replacements);
		
		if( replacement == this ) {
			Node baseReplacement = base.getReplacement(pag, replacements);
			if( baseReplacement == base ) return this;
			
			FieldRefNode newRep = PAGNodeFactory.v().makeFieldRefNode( (VarNode) baseReplacement, field );
			pag.mergeNodes(newRep, this);
			assert(newRep.getReplacement(pag, replacements) == this);
			return this;
		} else {
			//replacement is not this
			return replacement;
		}
	}
	/** Returns the field of this field reference. */
	public SparkField getField() { return field; }
	public String toString() {
		return "FieldRefNode "+getNumber()+" "+base+"."+field;
	}

	/* End of public methods. */

	public FieldRefNode( VarNode base, SparkField field ) {
		super( null );
		if( field == null ) throw new RuntimeException( "null field" );
		this.base = base;
		this.field = field;
		base.addField( this, field );		
	}

	/* End of package methods. */

	protected VarNode base;
	protected SparkField field;
}

