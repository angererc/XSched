package xsched.analysis.wala;

import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;

public final class WalaConstants {
	public static final TypeName TaskMethodAnnotation = TypeName.findOrCreate("Lxsched/TaskMethod");
	public static final TypeReference TaskType = TypeReference.findOrCreate(ClassLoaderReference.Extension, "Lxsched/Task");
	public static final String HappensBeforeSignature = "xsched.Task.hb(Lxsched/Task;)V";
	public static final Selector HappensBeforeSelector = Selector.make("hb(Lxsched/Task)V");
	public static final MethodReference HappensBeforeMethod = MethodReference.findOrCreate(TaskType, HappensBeforeSelector);
}
