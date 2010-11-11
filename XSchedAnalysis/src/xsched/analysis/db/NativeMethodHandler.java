package xsched.analysis.db;

import com.ibm.wala.classLoader.IMethod;

public class NativeMethodHandler {

	public static void processMethod(ExtensionalDatabase database, IMethod method) {
		//System.err.println("native method handler cheating bigtime!");
		//database.visitedMethods.add(method);
		String name = method.toString();
		if(name.equals("< Primordial, Ljava/util/Vector, addElement(Ljava/lang/Object;)V >")) {
			System.out.println("break here");
		}
	}

}
