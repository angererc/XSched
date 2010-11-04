package xsched.analysis.db;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;

public class ActivationInfo {
	public static final TypeName theActivationTypeName = TypeName.string2TypeName("Lxsched/Activation");
	public static final Selector theScheduleSelector = Selector.make("schedule(Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;)Lxsched/Activation;");
	public static final Selector theNowSelector = Selector.make("now()Lxsched/Activation;");
	public static final Selector theHBSelector = Selector.make("hb(Lxsched/Activation;)V;");
	
	public static boolean isActivationClass(IClass klass) {
		return isActivationClass(klass.getReference());
	}
	public static boolean isActivationClass(TypeReference ref) {
		return ref.getName().equals(theActivationTypeName);
	}
	
	public static boolean isScheduleSelector(Selector selector) {
		return selector.equals(theScheduleSelector);
	}
	
	public static boolean isNowSelector(Selector selector) {
		return selector.equals(theNowSelector);
	}
	
	public static boolean isHBSelector(Selector selector) {
		return selector.equals(theHBSelector);
	}
	
	public static boolean isScheduleMethod(MethodReference method) {
		return isActivationClass(method.getDeclaringClass()) && isScheduleSelector(method.getSelector());
	}
	
	public static boolean isNowMethod(MethodReference method) {
		return isActivationClass(method.getDeclaringClass()) && isNowSelector(method.getSelector());
	}
	
	public static boolean isHBMethod(MethodReference method) {
		return isActivationClass(method.getDeclaringClass()) && isHBSelector(method.getSelector());
	}
	
}
