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
		
		Activation<Boolean> a = new Activation<Boolean>(this, "firstTask", "some parameter");
		Activation<String> b = new Activation<String>(this, "secondTask");
		Activation<Void> x = new Activation<Void>(this, "finalTask", a, b);
		
		a.hb(x);
		b.hb(x);
	}
}
