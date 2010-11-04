package xsched.tests._1;

import org.junit.Assert;

import xsched.Activation;

public class SimpleTriangle {

	public boolean firstTask(String b) {
		return true;
	}
	
	public String secondTask() {
		return "howdy";
	}
	
	public void finalTask(Activation<Boolean> a, Activation<String> b) {
		Assert.assertTrue(a.result().booleanValue());
		Assert.assertTrue(b.result().equals("howdy"));		
	}
	
	public void main() {
		
		Activation<Boolean> a = Activation.schedule(this, "firstTask(Ljava/lang/String;)B;", "some parameter");
		Activation<String> b = Activation.schedule(this, "secondTask()Ljava/lang/String;");
		Activation<Void> x = Activation.schedule(this, "finalTask()V;", a, b);
		
		a.hb(x);
		b.hb(x);
	}
}
