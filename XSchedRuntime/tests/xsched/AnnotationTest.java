package xsched;

import java.lang.reflect.Method;

import org.junit.Test;

public class AnnotationTest {

	@Task(
		activations= "A:B:Singleton:xsched.AnnotationTest.someTask(), " +
					 "B:C*A*0:FwdChain:xsched.AnnotationTest.someTask(), " +
					 "C:1:Unordered:xsched.AnnotationTest.someTask()",
		implications="A, A=>B, B<=>C",				
		schedule = "A->B, B->C, B->1"
	)
	public void someTask() {
		
	}
	
	@Test
	public void annotationTest() throws Exception {
		Class<AnnotationTest> c = AnnotationTest.class;
		Method m = c.getMethod("someTask", new Class[] {});		
		Task t = m.getAnnotation(Task.class);
		assert(t != null);
		TaskAnnotationParser tap = new TaskAnnotationParser(t);
		System.out.println(tap);
	}
}
