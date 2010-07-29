package xsched.analysis.bddbddb;

import java.util.Collection;
import java.util.HashMap;

import chord.bddbddb.Dom;
import chord.bddbddb.Rel;

public abstract class Relation<Self extends Relation<Self>> {
	protected final Rel rel;
	
	public Relation(String name, String domainOrdering, Domain<?>... domains) {
		rel = new Rel();
		rel.setName(name);
	
		//we have to compute the minor components (e.g., V0, V1) for the domain names because otherwise the jchord validate function complains
		HashMap<String, Integer> numDomNames = new HashMap<String,Integer>();
		String[] domNames = new String[domains.length];
		Dom<?>[] doms = new Dom<?>[domains.length];
		
		for(int i = 0; i < domains.length; i++) {
			String domName = domains[i].getName();			
			Integer num = numDomNames.get(domName); //get a number we can use
			if(num == null) {
				num = 0;
			}
			numDomNames.put(domName, num+1); //inc for next use
			
			domNames[i] = domName + num;
			doms[i] = domains[i].dom;
		}
		
		rel.setSign(domNames, domainOrdering);
		rel.setDoms(doms);
	}
	
	public abstract Collection<String> stringify();
	
	public String getName() {
		return rel.getName();
	}
	
	@SuppressWarnings("unchecked")
	public Self zero() {
		rel.zero();
		return (Self)this;
	}
	
	@SuppressWarnings("unchecked")
	public Self one() {
		rel.one();
		return (Self) this;
	}
	
	public void save(String dirName) {
		rel.save(dirName);
	}
	
	public void load(String dirName) {
		rel.load(dirName);
	}
	
	public int size() {
		return rel.size();
	}
	
	public void close() {
		rel.close();
	}
}
