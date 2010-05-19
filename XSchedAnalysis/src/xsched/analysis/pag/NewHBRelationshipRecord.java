/**
 * 
 */
package xsched.analysis.pag;

import soot.jimple.InvokeExpr;
import soot.jimple.spark.sets.PointsToSetInternal;

public class NewHBRelationshipRecord {
	public final InvokeExpr source;
	PointsToSetInternal lhs;
	PointsToSetInternal rhs;
	NewHBRelationshipRecord(InvokeExpr source) {
		this.source = source;
	}
	public String toString() {
		return "happens-before declaration: " + lhs + " -> " + rhs;
	}
	public PointsToSetInternal lhs() {
		return lhs;
	}
	
	public PointsToSetInternal rhs() {
		return rhs;
	}
}