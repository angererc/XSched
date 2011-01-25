package xsched.analysis.core;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//the abstract schedule of a task
public abstract class TaskSchedule<SS> {
	
	public enum Relation {
		singleton,
		happensBefore,
		happensAfter,
		ordered,
		unordered;
		
		public Relation inverse() {
			switch(this) {
			case singleton: return singleton;
			case happensBefore: return happensAfter;
			case happensAfter: return happensBefore;
			case ordered: return ordered;
			case unordered: return unordered;
			default: assert false; return null;
			}
		}
	}
	
	private final ArrayList<SS> scheduleSites = new ArrayList<SS>();
	private final HashMap<SS, HashMap<SS, Relation>> relations = new HashMap<SS, HashMap<SS, Relation>>();
	
	public List<SS> scheduleSites() {
		return scheduleSites;
	}
	
	protected TaskSchedule() {
		this.computeFullSchedule();
	}
	
	protected abstract void computeFullSchedule();
	
	protected void addRelation(SS lhs, Relation rel, SS rhs) {
		HashMap<SS, Relation> lhsRelations = relations.get(lhs);
		if(lhsRelations == null) {
			lhsRelations = new HashMap<SS, Relation>();
			relations.put(lhs, lhsRelations);
		}
		assert ! lhsRelations.containsKey(rhs);
		
		lhsRelations.put(rhs, rel);
		
		HashMap<SS, Relation> rhsRelations = relations.get(rhs);
		if(rhsRelations == null) {
			rhsRelations = new HashMap<SS, Relation>();
			relations.put(rhs, rhsRelations);
		}
		assert ! rhsRelations.containsKey(lhs) || rhsRelations.get(lhs).equals(rel.inverse());
		
		rhsRelations.put(lhs, rel.inverse());
	}
	
	public Relation relation(SS lhs, SS rhs) {
		HashMap<SS, Relation> lhsRelations = relations.get(lhs);
		assert lhsRelations != null;
		assert lhsRelations.containsKey(rhs);
		return lhsRelations.get(rhs);
	}
	
	public void print(PrintStream out) {
		out.println(relations);
	}
}
