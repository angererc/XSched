package xsched;

import org.junit.Test;
import xsched.tests._1.SimpleTriangle;


public class Test_1 {

	@Test
	public void activateMain() {
		SimpleTriangle st = new SimpleTriangle();
		Activation<Void> main = new Activation<Void>(st, "main");
		main.kickOffMain();
		
		
//		synchronized(this) {
//			try {
//				wait(500);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
	}
}
