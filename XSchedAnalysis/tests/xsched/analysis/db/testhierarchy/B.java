package xsched.analysis.db.testhierarchy;

public abstract class B extends A implements IB {
	@Override
	public abstract void abstractInB();
	
	public void implementedInB() {}
	
	@Override
	public void overriddenInC() {}
	
	public void onlyImplementedInB() {}
}
