package xsched.analysis.bddbddb;

public class BinaryRelation<A, B> extends Relation<BinaryRelation<A, B>> {
	
	public BinaryRelation(String name, Domain<A> domA, Domain<B> domB, String domainOrdering) {
		super(name, domainOrdering, domA, domB);		
	}
	
	public void add(A elemA, B elemB) {
		rel.add(elemA, elemB);
	}
	
	public void remove(A elemA, B elemB) {
		rel.remove(elemA, elemB);
	}
	
	public boolean contains(A elemA, B elemB) {
		return rel.contains(elemA, elemB);
	}
	
}
