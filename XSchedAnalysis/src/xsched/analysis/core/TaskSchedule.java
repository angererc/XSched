package xsched.analysis.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//the abstract schedule of a task
public class TaskSchedule<SS> {
	
	public enum Relation {
		happensBefore,
		happensAfter,
		ordered,
		unordered;
		
		public Relation inverse() {
			switch(this) {
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
	
	public void addRelation(SS lhs, Relation rel, SS rhs) {
		HashMap<SS, Relation> lhsRelations = relations.get(lhs);
		if(lhsRelations == null) {
			lhsRelations = new HashMap<SS, Relation>();
			relations.put(lhs, lhsRelations);
		}
		assert ! lhsRelations.containsKey(rhs);
		
		lhsRelations.put(rhs, rel);
		addRelation(rhs, rel.inverse(), lhs);
	}
	
	public Relation relation(SS lhs, SS rhs) {
		HashMap<SS, Relation> lhsRelations = relations.get(lhs);
		assert lhsRelations != null;
		assert lhsRelations.containsKey(rhs);
		return lhsRelations.get(rhs);
	}
}
