package xsched;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Task {
	//list of activations and their task types: "A:Param1*Param2:Singleton:some.Class.taskMethod()"
	//other patterns are FwdChain, BckwdChain, Ordered, Unordered
	String activations();
	//creation implications: "A, B, B=>C, C<=>D, E(+)F".
	String implications();	
	//the schedule in the form of arrows: "A->B, B->C"
	String schedule();
}
