/**
 * 
 */
package xsched.analysis.heap;

import xsched.analysis.schedule.P2Set;

public class NewHBRelationshipRecord<Context> {
	public final Context context;
	public P2Set<Context> lhs;
	public P2Set<Context> rhs;
	
	public NewHBRelationshipRecord(Context context) {
		this.context = context;
	}
	public String toString() {
		return "happens-before declaration: " + lhs + " -> " + rhs;
	}
	
}