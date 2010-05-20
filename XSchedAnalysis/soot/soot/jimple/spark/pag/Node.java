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
import java.util.Map;

import soot.tagkit.LinkTag;
import soot.tagkit.StringTag;
import soot.tagkit.Tag;
import soot.util.*;
import soot.SootMethod;
import soot.Type;
import soot.jimple.toolkits.pointer.representations.ReferenceVariable;
import soot.jimple.spark.internal.TypeManager;
import soot.jimple.spark.sets.PointsToSetInternal;

/** Represents every node in the pointer assignment graph.
 * @author Ondrej Lhotak
 */
public abstract class Node implements ReferenceVariable, Numberable {
	
	//internalize the node into the node's pag. called when the node is added to the pag (e.g., through the addEdge method).
	//the methodpag creates fresh nodes (since it doesn't alter the pag in any way) but we want to make sure that equivalent nodes
	//are only added once. therefore, the pag will call internalized() which gives the node a chance to return
	//an internalized version that can be compared with ==
	public abstract Node internalized();
	//when a node is internalized, it should fetch its number from somewhere (some node numberer).
	//I keep this to be somwhat backwards compatible...
	//numbers of nodes are global, so they don't depend on the PAG
	protected abstract void fetchNumber(); 
	
	//we use a static hash map so that the instances don't need a tag field
	//because creating tags can be turned on and off and because the nodes now don't know the PAG any more
	//we use a nodeToTag == nil as a hint that we don't collect tags
	//this is a legacy of the original Spark impl but I keep it
	private static Map<Node, Tag> nodeToTag;
	static void collectNodeTags() {
		nodeToTag = new HashMap<Node, Tag>();
	}
	protected void addNodeTag( SootMethod m ) {
        if( nodeToTag != null ) {
            Tag tag;
            if( m == null ) {
                tag = new StringTag( this.toString() );
            } else {
                tag = new LinkTag( this.toString(), m, m.getDeclaringClass().getName() );
            }
            nodeToTag.put( this, tag );
        }
    }
	
	//convenience method, because so much code did call node.getP2Set(pag) it was easier to change it like this
	public PointsToSetInternal getP2Set(PAG pag) {
		return pag.getP2SetForNode(this);
	}
	
	public PointsToSetInternal makeP2Set(PAG pag) {
		return pag.makeP2SetForNode(this);
	}
	//convenience
	public Node getReplacement(PAG pag) {
		return pag.getReplacementForNode(this);
	}
	
	public void mergeWith(PAG pag, Node other) {
		pag.mergeNodes(this, other);
	}
	
	//the PAG forwards the getReplacementForNode methods to the node because FieldRefNode needs to overwrite this behavior 
	Node getReplacement(PAG pag, HashMap<Node,Node> replacements) {
		Node current = this;
		Node replacement = replacements.get(current);
			
		while(replacement != null) {			
			current = replacement;
			replacement = replacements.get(replacement);						
		}
		return current;
	}
	
	public Tag getNodeTag() {
		if(nodeToTag == null)
			return null;
		else
			return nodeToTag.get(this);
	 }
	
    public final int hashCode() { return number; }
    public final boolean equals( Object other ) { 
        return this == other;
    }
    /** Returns the declared type of this node, null for unknown. */
    public Type getType() { return type; }
    /** Sets the declared type of this node, null for unknown. */
    public void setType( Type type ) {
        if( TypeManager.isUnresolved(type) ) throw new RuntimeException("Unresolved type "+type );
        this.type = type; 
    }
            
    /* End of public methods. */

    /** Creates a new node of pointer assignment graph pag, with type type. */
    Node( Type type ) {
        if( TypeManager.isUnresolved(type) ) throw new RuntimeException("Unresolved type "+type );
        this.type = type;  
    }

    /* End of package methods. */

    public final int getNumber() { return number; }
    public final void setNumber( int number ) { this.number = number; }

    private int number = 0;

    protected Type type;
}
