package xsched.analysis.bddbddb;

public class UnaryRelation<A> extends Relation<UnaryRelation<A>> {
	
	public UnaryRelation(String name, Domain<A> domA,String domainOrdering) {
		super(name, domainOrdering, domA);		
	}
	
	public void add(A elemA) {
		rel.add(elemA);
	}
	
	public void remove(A elemA) {
		rel.remove(elemA);
	}
	
	public boolean contains(A elemA) {
		return rel.contains(elemA);
	}
	
}
