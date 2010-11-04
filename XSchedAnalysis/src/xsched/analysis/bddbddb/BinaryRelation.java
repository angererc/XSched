package xsched.analysis.bddbddb;

import java.util.ArrayList;
import java.util.Collection;

import chord.bddbddb.Rel.RelView;
import chord.util.tuple.object.Pair;

public class BinaryRelation<A, B> extends Relation<BinaryRelation<A, B>> {
	
	public BinaryRelation(String name, Domain<? super A> domA, Domain<? super B> domB, String domainOrdering) {
		super(name, domainOrdering, domA, domB);		
	}
	
	public void add(A elemA, B elemB) {
		//System.out.println(String.format("Relation %s: adding (%s, %s)", getName(), elemA, elemB));
		rel.add(elemA, elemB);
	}
	
	public void remove(A elemA, B elemB) {
		rel.remove(elemA, elemB);
	}
	
	public boolean contains(A elemA, B elemB) {
		return rel.contains(elemA, elemB);
	}
	
	public boolean containsKey(A val) {
		RelView view = rel.getView();
		view.select(0, val);
		return view.size() > 0;
	}
	
	@Override
	public Collection<String> stringify() {
		ArrayList<String> result = new ArrayList<String>();
		for(Pair<Object, Object> tuple : rel.getAry2ValTuples()) {
			result.add(tuple.toString());
		}
		return result;
	}
}
