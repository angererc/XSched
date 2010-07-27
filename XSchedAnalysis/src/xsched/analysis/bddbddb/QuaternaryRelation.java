package xsched.analysis.bddbddb;

public class QuaternaryRelation<A, B, C, D> extends Relation<QuaternaryRelation<A, B, C, D>> {
	
	public QuaternaryRelation(String name, Domain<A> domA, Domain<B> domB, Domain<C> domC, Domain<D> domD, String domainOrdering) {
		super(name, domainOrdering, domA, domB, domC, domD);
	}
	
	public void add(A elemA, B elemB, C elemC, D elemD) {
		System.out.println(String.format("Relation %s: adding (%s, %s, %s, %s)", getName(), elemA, elemB, elemC, elemD));
		rel.add(elemA, elemB, elemC, elemD);
	}
	
	//can't remove from a this relation because Rel doesn't offer that...
	
	public boolean contains(A elemA, B elemB, C elemC, D elemD) {
		return rel.contains(elemA, elemB, elemC);
	}
	
}
