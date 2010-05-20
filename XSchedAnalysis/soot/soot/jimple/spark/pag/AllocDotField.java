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

import soot.util.ArrayNumberer;

/** Represents an alloc-site-dot-field node (Yellow) in the pointer
 * assignment graph.
 * @author Ondrej Lhotak
 */
public class AllocDotField extends Node {
	
	private static final ArrayNumberer nodeNumberer = new ArrayNumberer();
	public static ArrayNumberer allocDotFieldNodeNumberer() {
		return nodeNumberer;
	}
	@Override
	public AllocDotField internalized() {
		if(this.getNumber() > 0)
			return this;
		
		//alloc dot fields are unique on their own; but we need a number
		this.fetchNumber();		
		return this;		
	}
	
	@Override
	protected void fetchNumber() {
		nodeNumberer.add(this);
	}
	
	/** Returns the base AllocNode. */
	public AllocNode getBase() { return base; }
	/** Returns the field of this node. */
	public SparkField getField() { return field; }
	public String toString() {
		return "AllocDotField "+getNumber()+" "+base+"."+field;
	}

	/* End of public methods. */

	public AllocDotField( AllocNode base, SparkField field ) {
		super( null );
		if( field == null ) throw new RuntimeException( "null field" );
		this.base = base;
		this.field = field;
		base.addField( this, field );		
	}

	/* End of package methods. */

	protected AllocNode base;
	protected SparkField field;
	
}

