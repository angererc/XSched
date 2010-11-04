package xsched.analysis.bddbddb;

import java.util.ArrayList;
import java.util.Collection;

public class UnaryRelation<A> extends Relation<UnaryRelation<A>> {
	
	public UnaryRelation(String name, Domain<? super A> domA,String domainOrdering) {
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
	
	@Override
	public Collection<String> stringify() {
		ArrayList<String> result = new ArrayList<String>();
		for(Object tuple : rel.getAry1ValTuples()) {
			result.add(tuple.toString());
		}
		return result;
	}
	
}
