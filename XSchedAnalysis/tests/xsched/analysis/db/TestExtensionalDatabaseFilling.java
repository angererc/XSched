package xsched.analysis.db;

import java.util.Collection;

import junit.framework.TestCase;

public class TestExtensionalDatabaseFilling extends TestCase {
	
	private static ExtensionalDatabase database;
	private static TestCheater cheater;
	
	static {
		database = new ExtensionalDatabase();
		cheater = new TestCheater();
		//"/Users/angererc/Projects/XSched/XSchedAnalysis/bin/TestClass.class"
		try {
			new FillExtensionalDatabase(database, "bin/xsched/analysis/db/testhierarchy", cheater);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void testAssignableRelation() {
		
		Collection<String> assignable = database.assignable.stringify();
		
		//all from object
		int i = 0;
		assertTrue("a"+i++, assignable.contains("<Ljava/lang/Object, Lxsched/analysis/db/testhierarchy/A>"));
		assertTrue("a"+i++, assignable.contains("<Ljava/lang/Object, Lxsched/analysis/db/testhierarchy/B>"));
		assertTrue("a"+i++, assignable.contains("<Ljava/lang/Object, Lxsched/analysis/db/testhierarchy/C>"));
		assertTrue("a"+i++, assignable.contains("<Ljava/lang/Object, Lxsched/analysis/db/testhierarchy/D>"));
		
		//all from self
		assertTrue("a"+i++, assignable.contains("<Lxsched/analysis/db/testhierarchy/A, Lxsched/analysis/db/testhierarchy/A>"));
		assertTrue("a"+i++, assignable.contains("<Lxsched/analysis/db/testhierarchy/B, Lxsched/analysis/db/testhierarchy/B>"));
		assertTrue("a"+i++, assignable.contains("<Lxsched/analysis/db/testhierarchy/C, Lxsched/analysis/db/testhierarchy/C>"));
		assertTrue("a"+i++, assignable.contains("<Lxsched/analysis/db/testhierarchy/D, Lxsched/analysis/db/testhierarchy/D>"));
		
		//all from A
		assertTrue("a"+i++, assignable.contains("<Lxsched/analysis/db/testhierarchy/A, Lxsched/analysis/db/testhierarchy/B>"));
		assertTrue("a"+i++, assignable.contains("<Lxsched/analysis/db/testhierarchy/A, Lxsched/analysis/db/testhierarchy/C>"));
		assertTrue("a"+i++, assignable.contains("<Lxsched/analysis/db/testhierarchy/A, Lxsched/analysis/db/testhierarchy/D>"));
		
		//C from B
		assertTrue("a"+i++, assignable.contains("<Lxsched/analysis/db/testhierarchy/B, Lxsched/analysis/db/testhierarchy/C>"));
		
		//B from interface IB
		assertTrue("a"+i++, assignable.contains("<Lxsched/analysis/db/testhierarchy/IB, Lxsched/analysis/db/testhierarchy/B>"));
		
	}
	
	public void testMethodImplementationRelation() {
		//*********************
		// test methods
		int i = 0;
		Collection<String> mImpl = database.methodImplementation.stringify();
		System.out.println(mImpl);
		assertTrue("b"+i++, mImpl.contains("<Lxsched/analysis/db/testhierarchy/B, overriddenInC()V, < Application, Lxsched/analysis/db/testhierarchy/B, overriddenInC()V >>"));
		assertTrue("b"+i++, mImpl.contains("<Lxsched/analysis/db/testhierarchy/B, implementedInB()V, < Application, Lxsched/analysis/db/testhierarchy/B, implementedInB()V >>"));
		assertTrue("b"+i++, mImpl.contains("<Lxsched/analysis/db/testhierarchy/B, onlyImplementedInB()V, < Application, Lxsched/analysis/db/testhierarchy/B, onlyImplementedInB()V >>"));
		assertFalse("b"+i++, mImpl.contains("<Lxsched/analysis/db/testhierarchy/B, abstractInB()V, < Application, Lxsched/analysis/db/testhierarchy/B, abstractInB()V >>"));
		assertFalse("b"+i++, mImpl.contains("<Lxsched/analysis/db/testhierarchy/B, implementedInC()V, < Application, Lxsched/analysis/db/testhierarchy/B, implementedInC()V >>"));
		
		assertTrue("b"+i++, mImpl.contains("<Lxsched/analysis/db/testhierarchy/C, abstractInB()V, < Application, Lxsched/analysis/db/testhierarchy/C, abstractInB()V >>"));
		assertTrue("b"+i++, mImpl.contains("<Lxsched/analysis/db/testhierarchy/C, overriddenInC()V, < Application, Lxsched/analysis/db/testhierarchy/C, overriddenInC()V >>"));
		assertTrue("b"+i++, mImpl.contains("<Lxsched/analysis/db/testhierarchy/C, implementedInC()V, < Application, Lxsched/analysis/db/testhierarchy/C, implementedInC()V >>"));
		assertTrue("b"+i++, mImpl.contains("<Lxsched/analysis/db/testhierarchy/C, onlyImplementedInC()V, < Application, Lxsched/analysis/db/testhierarchy/C, onlyImplementedInC()V >>"));
		assertTrue("b"+i++, mImpl.contains("<Lxsched/analysis/db/testhierarchy/C, implementedInB()V, < Application, Lxsched/analysis/db/testhierarchy/B, implementedInB()V >>"));
		assertTrue("b"+i++, mImpl.contains("<Lxsched/analysis/db/testhierarchy/C, onlyImplementedInB()V, < Application, Lxsched/analysis/db/testhierarchy/B, onlyImplementedInB()V >>"));

	}
}
