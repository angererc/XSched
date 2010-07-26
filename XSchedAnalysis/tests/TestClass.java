
public class TestClass {
	public void foo(String[] args){
		int x = 0, y = 1, z = 0;
		
		if(x == y) {
			z = 42;
		} else {
			z = 99;
		}
		
		args[z] = "foo";
	}
}
