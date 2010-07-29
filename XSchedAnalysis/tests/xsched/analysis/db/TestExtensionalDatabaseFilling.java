package xsched.analysis.db;

import java.util.Collection;

import junit.framework.TestCase;

public class TestExtensionalDatabaseFilling extends TestCase {
	
	private ExtensionalDatabase database;
	private TestCheater cheater;
	
	@Override
	public void setUp() {
		database = new ExtensionalDatabase();
		cheater = new TestCheater();
	}
	
	public void testInheritance() throws Exception {
		//"/Users/angererc/Projects/XSched/XSchedAnalysis/bin/TestClass.class"
		new FillExtensionalDatabase(database, "bin/xsched/analysis/db/testhierarchy", cheater);
		
		Collection<String> assignable = database.assignable.stringify();
		
		//all from object
		int i = 0;
		assertTrue(""+i++, assignable.contains("<Ljava/lang/Object, Lxsched/analysis/db/testhierarchy/A>"));
		assertTrue(""+i++, assignable.contains("<Ljava/lang/Object, Lxsched/analysis/db/testhierarchy/B>"));
		assertTrue(""+i++, assignable.contains("<Ljava/lang/Object, Lxsched/analysis/db/testhierarchy/C>"));
		assertTrue(""+i++, assignable.contains("<Ljava/lang/Object, Lxsched/analysis/db/testhierarchy/D>"));
		
		//all from self
		assertTrue(""+i++, assignable.contains("<Lxsched/analysis/db/testhierarchy/A, Lxsched/analysis/db/testhierarchy/A>"));
		assertTrue(""+i++, assignable.contains("<Lxsched/analysis/db/testhierarchy/B, Lxsched/analysis/db/testhierarchy/B>"));
		assertTrue(""+i++, assignable.contains("<Lxsched/analysis/db/testhierarchy/C, Lxsched/analysis/db/testhierarchy/C>"));
		assertTrue(""+i++, assignable.contains("<Lxsched/analysis/db/testhierarchy/D, Lxsched/analysis/db/testhierarchy/D>"));
		
		//all from A
		assertTrue(""+i++, assignable.contains("<Lxsched/analysis/db/testhierarchy/A, Lxsched/analysis/db/testhierarchy/B>"));
		assertTrue(""+i++, assignable.contains("<Lxsched/analysis/db/testhierarchy/A, Lxsched/analysis/db/testhierarchy/C>"));
		assertTrue(""+i++, assignable.contains("<Lxsched/analysis/db/testhierarchy/A, Lxsched/analysis/db/testhierarchy/D>"));
		
		//C from B
		assertTrue(""+i++, assignable.contains("<Lxsched/analysis/db/testhierarchy/B, Lxsched/analysis/db/testhierarchy/C>"));
		
		//B from interface IB
		assertTrue(""+i++, assignable.contains("<Lxsched/analysis/db/testhierarchy/IB, Lxsched/analysis/db/testhierarchy/B>"));
	}
}
