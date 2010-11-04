package xsched.analysis.bddbddb;

import java.util.ArrayList;
import java.util.Collection;

import chord.util.tuple.object.Quad;

public class QuaternaryRelation<A, B, C, D> extends Relation<QuaternaryRelation<A, B, C, D>> {
	
	public QuaternaryRelation(String name, Domain<? super A> domA, Domain<? super B> domB, Domain<? super C> domC, Domain<? super D> domD, String domainOrdering) {
		super(name, domainOrdering, domA, domB, domC, domD);
	}
	
	public void add(A elemA, B elemB, C elemC, D elemD) {
		//System.out.println(String.format("Relation %s: adding (%s, %s, %s, %s)", getName(), elemA, elemB, elemC, elemD));
		rel.add(elemA, elemB, elemC, elemD);
	}
	
	//can't remove from a this relation because Rel doesn't offer that...
	
	public boolean contains(A elemA, B elemB, C elemC, D elemD) {
		return rel.contains(elemA, elemB, elemC);
	}
	
	@Override
	public Collection<String> stringify() {
		ArrayList<String> result = new ArrayList<String>();
		for(Quad<Object, Object, Object, Object> tuple : rel.getAry4ValTuples()) {
			result.add(tuple.toString());
		}
		return result;
	}
	
}
