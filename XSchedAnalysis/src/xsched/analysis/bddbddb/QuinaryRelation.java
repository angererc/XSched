package xsched.analysis.bddbddb;

import java.util.ArrayList;
import java.util.Collection;

import chord.util.tuple.object.Pent;

public class QuinaryRelation<A, B, C, D, E> extends Relation<QuinaryRelation<A, B, C, D, E>> {
	
	public QuinaryRelation(String name, Domain<? super A> domA, Domain<? super B> domB, Domain<? super C> domC, Domain<? super D> domD, Domain<? super E> domE, String domainOrdering) {
		super(name, domainOrdering, domA, domB, domC, domD, domE);
	}
	
	public void add(A elemA, B elemB, C elemC, D elemD, E elemE) {
		//System.out.println(String.format("Relation %s: adding (%s, %s, %s, %s)", getName(), elemA, elemB, elemC, elemD));
		rel.add(elemA, elemB, elemC, elemD, elemE);
	}
	
	//can't remove from a this relation because Rel doesn't offer that...
	
	public boolean contains(A elemA, B elemB, C elemC, D elemD, E elemE) {
		return rel.contains(elemA, elemB, elemC, elemE);
	}
	
	@Override
	public Collection<String> stringify() {
		ArrayList<String> result = new ArrayList<String>();
		for(Pent<Object, Object, Object, Object, Object> tuple : rel.getAry5ValTuples()) {
			result.add(tuple.toString());
		}
		return result;
	}
	
}
