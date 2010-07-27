package xsched.analysis.bddbddb;

import java.io.IOException;
import java.util.Iterator;

import chord.bddbddb.Dom;

public class Domain<T> implements Iterable<T> {
	//allow package friends to access the dom directly
	final Dom<T> dom;
	
	public Domain(String name) {
		dom = new Dom<T>() { 
			@Override
			public String toUniqueString(T val) {
				return asUniqueString(val);
			}
		};
		dom.setName(name);
	}
	
	//overwrite this for nicer strings
	public String asUniqueString(T val) {
		return val == null ? "null" : val.toString();
	}
	
	public String getName() {
		return dom.getName();
	}
	
	/**
	 * this method is here more for sanity checks than for usefullness.
	 * you know that elem is in the domain, return it; if its not, crash
	 */
	public T get(T elem) {
		return dom.get(dom.indexOf(elem));
	}
	
	/**
	 * returns the element itself; makes it easier to use as in: Foo f = domain.getOrAdd(new Foo());
	 * 
	 */
	public T getOrAdd(T elem) {
		System.out.println("Domain " + getName() + ": adding " + elem);
		dom.getOrAdd(elem);
		return elem;
	}
	
	/**
	 * returns true if the elem did not yet exist in the domain
	 */
	public boolean add(T elem) {
		System.out.println("Domain " + getName() + ": adding " + elem);
		return dom.add(elem);
	}
	
	public void save(String filename, boolean alsoMapFile) {
		try {
			dom.save(filename, alsoMapFile);
		} catch (IOException e) {
			//in Rel, the JChord people catch the exception like this, but in Dom they don't
			//i catch it here to make the API a little more uniform
			throw new RuntimeException(e);
		}
	}
	
	public T get(int idx) {
		return dom.get(idx);
	}
	
	public int indexOf(Object elem) {
		return dom.indexOf(elem);
	}
	
	public Iterator<T> iterator() {
		return dom.iterator();
	}

}
