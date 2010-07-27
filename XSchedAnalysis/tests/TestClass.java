
public class TestClass {
	private int myX;
	public void foo(String[] args){
		int x = myX, y = 1, z = 0;
		
		if(x == y) {
			synchronized(this) {
				z = 42;
			}
		} else {
			z = 99;
		}
		
		args[z] = "foo";
	}
}
