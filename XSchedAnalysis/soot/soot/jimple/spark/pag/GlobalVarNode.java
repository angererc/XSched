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
import java.util.HashMap;

import soot.*;

/** Represents a simple variable node (Green) in the pointer assignment graph
 * that is not associated with any particular method invocation.
 * @author Ondrej Lhotak
 */
public class GlobalVarNode extends VarNode {
	
	private static final HashMap<Object,GlobalVarNode> internalized = new HashMap<Object,GlobalVarNode>();
	public static GlobalVarNode globalVarNode(Object var) {
		return internalized.get(var);
	}
	
	public static GlobalVarNode internalized(Object variable, Type t) {
		GlobalVarNode ret = internalized.get(variable);
		if(ret == null) {
			ret = new GlobalVarNode(variable, t);
			VarNode.nodeNumberer.add(ret);
			internalized.put(variable, ret);
			ret.addNodeTag(null);			
		} else if( !( ret.getType().equals( t ) ) ) {
			    throw new RuntimeException( "Value "+variable+" of type "+t+
				    " previously had type "+ret.getType() );
		}
		return ret;
	}
	
    private GlobalVarNode( Object variable, Type t ) {
    	super(variable, t );
    }
    public String toString() {
    	return "GlobalVarNode "+getNumber()+" "+variable;
    }
}

