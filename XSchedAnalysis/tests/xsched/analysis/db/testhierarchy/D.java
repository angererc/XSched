package xsched.analysis.db.testhierarchy;

public class D extends A {

	public static Object myField1;
	public static Object myField2;
	
	public static Object staticInD() {
		myField1 = myField2;		
		return myField1;
	}
	
	public Object instanceInD() {
		D.myField1 = D.myField2;
		return D.myField1;
	}
	
	public void classAsObject() {
		D.myField1 = Object.class;
	}
}
