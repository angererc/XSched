package xsched.analysis.db;

import java.util.Collection;

import junit.framework.TestCase;

public class TestParallelMapOperationPreprocessing extends TestCase {
	
	private static ExtensionalDatabase database;
	private static TestCheater cheater;
	
	static {
		database = new ExtensionalDatabase();
		cheater = new TestCheater();
		//"/Users/angererc/Projects/XSched/XSchedAnalysis/bin/TestClass.class"
		try {
			new FillExtensionalDatabase(database, "bin/xsched/tests/_1/ParallelMapOperation.class", cheater);
			//database.save("/Users/angererc/Desktop/test/dataset");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void testNewStatementRelation() {
		
		Collection<String> result = database.newStatement.stringify();
		//for(String s : result) System.out.println(s);
		
		int i = 0;
		assertTrue("a"+i++, result.contains("<xsched.tests._1.ParallelMapOperation.process(Ljava/lang/Object;)Ljava/lang/Object;.v4, 4 = new <Application,Ljava/lang/Object>@0>"));
		assertTrue("a"+i++, result.contains("<xsched.tests._1.ParallelMapOperation.writeToOut(Ljava/util/Vector;)V.v15, 15 = new <Application,Lxsched/Activation>@26>"));
		assertTrue("a"+i++, result.contains("<xsched.tests._1.ParallelMapOperation.writeToOut(Ljava/util/Vector;)V.v18, 18 = new <Application,Lxsched/Activation>@40>"));
		
		assertTrue("a"+i++, result.contains("<xsched.tests._1.ParallelMapOperation.writeToOut(Ljava/util/Vector;)V.v16, -1 = new <Primordial,Ljava/lang/String>@0>"));
		assertTrue("a"+i++, result.contains("<xsched.tests._1.ParallelMapOperation.writeToOut(Ljava/util/Vector;)V.v19, -1 = new <Primordial,Ljava/lang/String>@0>"));
		
	}
	
	public void testMethodInvokeRelation() {
		Collection<String> result = database.methodInvokes.stringify();
		//for(String s : result) System.out.println(s);
		
		int i = 0;
		assertTrue("b"+i++, result.contains("<8 = invokevirtual < Application, Ljava/util/Vector, add(Ljava/lang/Object;)Z > 4,6 @8 exception:7, add(Ljava/lang/Object;)Z>"));
		
	}
	
	public void testStaticInvokeRelation() {
		Collection<String> result = database.staticInvokes.stringify();
		//for(String s : result) System.out.println(s);
		
		int i = 0;
		assertTrue("c"+i++, result.contains("<invokespecial < Application, Ljava/lang/Object, <init>()V > 4 @4 exception:5, < Primordial, Ljava/lang/Object, <init>()V >>"));
		
	}
	
	public void testFormalsRelation() {
		Collection<String> result = database.formals.stringify();
		//for(String s : result) System.out.println(s);
		
		int i = 0;
		assertTrue("d"+i++, result.contains("<< Application, Lxsched/tests/_1/ParallelMapOperation, <init>()V >, 0, xsched.tests._1.ParallelMapOperation.<init>()V.v1>"));
		assertTrue("d"+i++, result.contains("<< Application, Lxsched/tests/_1/ParallelMapOperation, <init>()V >, 0, xsched.tests._1.ParallelMapOperation.<init>()V.v1>"));
		assertTrue("d"+i++, result.contains("<< Application, Lxsched/tests/_1/ParallelMapOperation, process(Ljava/lang/Object;)Ljava/lang/Object; >, 1, xsched.tests._1.ParallelMapOperation.process(Ljava/lang/Object;)Ljava/lang/Object;.v2>"));
		assertTrue("d"+i++, result.contains("<< Application, Lxsched/tests/_1/ParallelMapOperation, write(Lxsched/Activation;)V >, 0, xsched.tests._1.ParallelMapOperation.write(Lxsched/Activation;)V.v1>"));
		assertTrue("d"+i++, result.contains("<< Application, Lxsched/tests/_1/ParallelMapOperation, write(Lxsched/Activation;)V >, 1, xsched.tests._1.ParallelMapOperation.write(Lxsched/Activation;)V.v2>"));
		assertTrue("d"+i++, result.contains("<< Application, Lxsched/tests/_1/ParallelMapOperation, writeToOut(Ljava/util/Vector;)V >, 0, xsched.tests._1.ParallelMapOperation.writeToOut(Ljava/util/Vector;)V.v1>"));
		assertTrue("d"+i++, result.contains("<< Application, Lxsched/tests/_1/ParallelMapOperation, writeToOut(Ljava/util/Vector;)V >, 1, xsched.tests._1.ParallelMapOperation.writeToOut(Ljava/util/Vector;)V.v2>"));	
	}
	
	public void testActualRelation() {
		Collection<String> result = database.actuals.stringify();
		for(String s : result) System.out.println(s);
		
		int i = 0;
		assertTrue("e"+i++, result.contains("<8 = invokevirtual < Application, Ljava/util/Vector, add(Ljava/lang/Object;)Z > 4,6 @8 exception:7, 0, xsched.tests._1.ParallelMapOperation.write(Lxsched/Activation;)V.v4>"));
		assertTrue("e"+i++, result.contains("<8 = invokevirtual < Application, Ljava/util/Vector, add(Ljava/lang/Object;)Z > 4,6 @8 exception:7, 1, xsched.tests._1.ParallelMapOperation.write(Lxsched/Activation;)V.v6>"));
		
		//params flow into new Activation(obj, task, params)
		assertTrue("e"+i++, result.contains("<invokespecial < Application, Lxsched/Activation, <init>(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;)V > 15,1,16,14 @35 exception:17, 0, xsched.tests._1.ParallelMapOperation.writeToOut(Ljava/util/Vector;)V.v15>"));
		assertTrue("e"+i++, result.contains("<invokespecial < Application, Lxsched/Activation, <init>(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;)V > 15,1,16,14 @35 exception:17, 1, xsched.tests._1.ParallelMapOperation.writeToOut(Ljava/util/Vector;)V.v1>"));
		assertTrue("e"+i++, result.contains("<invokespecial < Application, Lxsched/Activation, <init>(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;)V > 15,1,16,14 @35 exception:17, 2, xsched.tests._1.ParallelMapOperation.writeToOut(Ljava/util/Vector;)V.v16>"));
		assertTrue("e"+i++, result.contains("<invokespecial < Application, Lxsched/Activation, <init>(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;)V > 15,1,16,14 @35 exception:17, 3, xsched.tests._1.ParallelMapOperation.writeToOut(Ljava/util/Vector;)V.v14>"));
				
	}
	
	public void testEveryVariableHasType() {
		for(Variable var : database.variables) {
			assertTrue("missing type for variable: " + var, database.variableType.containsKey(var));
		}
	}
}
