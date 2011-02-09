package xsched.instrumentation;

import java.lang.instrument.Instrumentation;

public class Instrumenter {

	public static void premain(String agentArgs, Instrumentation inst) {
		inst.addTransformer(new ScheduleSiteRewriter());
	}
}
