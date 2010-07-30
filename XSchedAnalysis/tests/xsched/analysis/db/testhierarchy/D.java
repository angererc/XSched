package xsched.analysis.db.testhierarchy;

public class D extends A {

	public static Object myField;
	
	public static Object staticInD() {
		return myField;
	}
	
	public Object instanceInD() {
		return D.myField;
	}
}
