package xsched.analysis.wala;

import java.util.HashMap;
import java.util.Iterator;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSACache;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.ssa.SSAOptions;
import com.ibm.wala.types.MethodReference;

public class ScheduleSitesInformation {

	public static ScheduleSitesInformation make(SSACache cache, SSAOptions options, IMethod method) {
		ScheduleSitesInformation result = new ScheduleSitesInformation(method);
		
		IR ir = cache.findOrCreateIR(method, Everywhere.EVERYWHERE, options);
		DefUse defUse = cache.findOrCreateDU(ir, Everywhere.EVERYWHERE);
		
		Iterator<CallSiteReference> callSites = ir.iterateCallSites();
		while(callSites.hasNext()) {
			CallSiteReference callSite = callSites.next();
			MethodReference calledMethod = callSite.getDeclaredTarget();
			if(WalaConstants.isTaskMethod(calledMethod)) {
				//get the ssa invoke instruction
				int pc = callSite.getProgramCounter();
				SSAInvokeInstruction invoke = (SSAInvokeInstruction)ir.getInstructions()[pc];
				
				//the task as an ssa variable
				int ssaTaskVariable = invoke.isStatic() ? invoke.getUse(0) : invoke.getUse(1);
				
				//the corresponding new site; we just trust the compiler that there is one
				SSANewInstruction newTaskInstruction = (SSANewInstruction)defUse.getDef(ssaTaskVariable);
				
				result.callSitesByTask.put(ssaTaskVariable, invoke);
				result.newSitesByTask.put(ssaTaskVariable, newTaskInstruction);
			}
		}
		
		return result;
	}
	
	public final IMethod method;
	private final HashMap<Integer, SSANewInstruction> newSitesByTask = new HashMap<Integer, SSANewInstruction>();
	private final HashMap<Integer, SSAInvokeInstruction> callSitesByTask = new HashMap<Integer, SSAInvokeInstruction>();
	
	private ScheduleSitesInformation(IMethod method) {
		this.method = method;
	}
	
	public SSANewInstruction newSiteForTask(int ssaVariable) {
		return newSitesByTask.get(ssaVariable);
	}
	
	public SSAInvokeInstruction callSiteForTask(int ssaVariable) {
		return callSitesByTask.get(ssaVariable);
	}
	
}
