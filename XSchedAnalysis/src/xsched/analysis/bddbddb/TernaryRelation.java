package xsched.analysis.bddbddb;

public class TernaryRelation<A, B, C> extends Relation<TernaryRelation<A, B, C>> {
	
	public TernaryRelation(String name, Domain<A> domA, Domain<B> domB, Domain<C> domC, String domainOrdering) {
		super(name, domainOrdering, domA, domB, domC);
	}
	
	public void add(A elemA, B elemB, C elemC) {
		System.out.println(String.format("Relation %s: adding (%s, %s, %s)", getName(), elemA, elemB, elemC));
		rel.add(elemA, elemB, elemC);
	}
	
	//can't remove from a ternary relation because Rel doesn't offer that...
	
	public boolean contains(A elemA, B elemB, C elemC) {
		return rel.contains(elemA, elemB, elemC);
	}
	
}
