package xsched.analysis.core;
import java.util.ArrayList;
import java.util.HashMap;


public class FormalParameterConstraints {

	public enum Relation {
		equal, happensBefore, happensAfter, ordered, unordered;
	}
	
	private static class Entry {		
		public final Relation relation;
		public final int rhs;
		
		public Entry(Relation relation, int rhs) {
			this.relation = relation;
			this.rhs = rhs;
		}
	}
	
	private HashMap<Integer, ArrayList<Entry>> entries = new HashMap<Integer, ArrayList<Entry>>();
	
	public FormalParameterConstraints() {
		
	}
	
	public FormalParameterConstraints(ArrayList<TaskVariable<?>> actuals) {
		int size = actuals.size();
		for(int i = 0; i < size-1; i++) {
			for(int j = i+1; j < size; j++) {
				TaskVariable<?> lhs = actuals.get(i);
				TaskVariable<?> rhs = actuals.get(j);
				if(lhs.equals(rhs)) {
					addEntry(i, Relation.equal, j);
				} else if(lhs.doesHappenBefore(rhs)) {
					addEntry(i, Relation.happensBefore, j);
				} else if(lhs.doesHappenAfter(rhs)) {
					addEntry(i, Relation.happensAfter, j);
				} else if(lhs.isOrderedWith(rhs)) {
					addEntry(i, Relation.ordered, j);
				} else {
					addEntry(i, Relation.unordered, j);
				}
			}
		}
	}
		
	private Entry findEntryWithRHS(ArrayList<Entry> entryList, int rhs) {
		for(Entry entry : entryList) {
			if(entry.rhs == rhs) {
				return entry;
			}
		}
		return null;
	}
	
	//add a relation between the lhs parameter and the rhs parameter; lhs and rhs must be ordered with respect to their parameter positions
	//that is #lhs < #rhs
	private void addEntry(int lhs, Relation relation, int rhs) {
		assert lhs < rhs;
		
		ArrayList<Entry> entryList = entries.get(lhs);
		if(entryList == null) {
			entryList = new ArrayList<Entry>();
			entries.put(lhs, entryList);
		}
		
		assert findEntryWithRHS(entryList, rhs) == null;
		entryList.add(new Entry(relation, rhs));		
	}
	
	public Relation relation(int lhs, int rhs) {
		assert lhs != rhs;
		if(lhs > rhs)
			return relation(rhs, lhs);
		
		ArrayList<Entry> entryList = entries.get(lhs);
		assert(entryList != null);
		
		Entry entry = findEntryWithRHS(entryList, rhs);
		assert(entry != null);
		
		return entry.relation;
	}
	
}
