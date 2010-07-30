package xsched.analysis.db;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;

public class ActivationInfo {
	public static final TypeName theActivationTypeName = TypeName.string2TypeName("Lxsched/Activation");
	public static final Selector theAfterSelector = Selector.make("after(Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;)Lxsched/Activation;");
	public static final Selector theHBSelector = Selector.make("hb(Lxsched/Activation;)V;");
	
	public static boolean isActivationClass(IClass klass) {
		return isActivationClass(klass.getReference());
	}
	public static boolean isActivationClass(TypeReference ref) {
		return ref.getName().equals(theActivationTypeName);
	}
	
	public static boolean isAfterSelector(Selector selector) {
		return selector.equals(theAfterSelector);
	}
	
	public static boolean isHBSelector(Selector selector) {
		return selector.equals(theHBSelector);
	}
	
	public static boolean isAfterMethod(MethodReference method) {
		return isActivationClass(method.getDeclaringClass()) && isAfterSelector(method.getSelector());
	}
	
	public static boolean isHBMethod(MethodReference method) {
		return isActivationClass(method.getDeclaringClass()) && isHBSelector(method.getSelector());
	}
	
	public static boolean isActivationCreationMethod(MethodReference method) {
		return isAfterMethod(method);
	}
}
