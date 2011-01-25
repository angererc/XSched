package xsched.analysis.core;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Set;

//the abstract schedule of a task
public abstract class TaskSchedule<TV, SS> {
	
	public interface SiteMapper<T, SS> {
		public SS scheduleSiteForTask(T t);
	}
	
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
	
	private final ArrayList<TV> nodes;
	private final Relation[][] relations;
	private final SiteMapper<TV, SS> siteMapper;
	
	protected TaskSchedule(SiteMapper<TV, SS> siteMapper, Set<TV> nodeSet) {
		this.siteMapper = siteMapper;
		this.nodes = new ArrayList<TV>(nodeSet);
		int dimensions = nodes.size();
		relations = new Relation[dimensions][dimensions];
		this.computeFullSchedule();
		assert matrixIsFull();
	}
	
	private boolean matrixIsFull() {
		for(int i = 0; i < relations.length; i++) {
			for(int j = 0; j < relations.length; j++) {
				if(relations[i][j] == null)
					return false;
			}
		}
		return true;
	}

	protected abstract void computeFullSchedule();
	
	public int nodeNumber(TV node) {
		return nodes.indexOf(node);
	}
	
	protected void addRelation(TV lhs, Relation rel, TV rhs) {
		int lhsIndex = nodeNumber(lhs);
		int rhsIndex = nodeNumber(rhs);
		assert relations[lhsIndex][rhsIndex] == null;
		relations[lhsIndex][rhsIndex] = rel;
		
		if(lhs.equals(rhs))
			return;
		
		assert relations[rhsIndex][lhsIndex] == null;
		relations[rhsIndex][lhsIndex] = rel.inverse();	
	}
	
	public SS scheduleSite(TV node) {
		return siteMapper.scheduleSiteForTask(node);
	}
	
	public Relation relation(TV lhs, TV rhs) {
		int lhsIndex = nodeNumber(lhs);
		int rhsIndex = nodeNumber(rhs);
		return relations[lhsIndex][rhsIndex];		
	}
	
	public void print(PrintStream out) {
		for(int i = 0; i < relations.length; i++) {
			for(int j = 0; j < relations.length; j++) {
				out.println(i + " " + relations[i][j] + " " + j);			
			}
		}
		
	}
	
}
