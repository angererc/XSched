package xsched.analysis.db.testhierarchy;

public class C extends B {
	
	@Override
	public void abstractInB() {};
	
	@Override
	public void overriddenInC() {};
	
	@Override 
	public void implementedInC() {};
	
	public void onlyImplementedInC() {};
}
