package xsched.analysis.wala;

import java.util.HashMap;
import java.util.Iterator;

import xsched.analysis.core.TaskSchedule;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSACache;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.ssa.SSAOptions;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.util.collections.Pair;

public class WalaScheduleSitesInformation implements TaskSchedule.SiteMapper<Integer, Pair<SSANewInstruction, SSAInvokeInstruction>>{

	public static WalaScheduleSitesInformation make(SSACache cache, SSAOptions options, IMethod method) {
		WalaScheduleSitesInformation result = new WalaScheduleSitesInformation();
		
		IR ir = cache.findOrCreateIR(method, Everywhere.EVERYWHERE, options);
		DefUse defUse = cache.findOrCreateDU(ir, Everywhere.EVERYWHERE);
		
		Iterator<SSAInstruction> instructions = ir.iterateNormalInstructions();
		while(instructions.hasNext()) {
			SSAInstruction instruction = instructions.next();
			if(instruction instanceof SSAInvokeInstruction) {
				SSAInvokeInstruction invoke = (SSAInvokeInstruction)instruction;
				if(WalaConstants.isTaskMethod(invoke.getDeclaredTarget())) {
					//the task as an ssa variable
					int ssaTaskVariable = invoke.isStatic() ? invoke.getUse(0) : invoke.getUse(1);
					
					//the corresponding new site; we just trust the compiler that there is one
					SSANewInstruction newTaskInstruction = (SSANewInstruction)defUse.getDef(ssaTaskVariable);
					
					result.scheduleSitesByTask.put(ssaTaskVariable, Pair.make(newTaskInstruction, invoke));
				}
			}
		}
				
		return result;
	}
	
	private final HashMap<Integer, Pair<SSANewInstruction, SSAInvokeInstruction>> scheduleSitesByTask = new HashMap<Integer, Pair<SSANewInstruction, SSAInvokeInstruction>>();
		
	private WalaScheduleSitesInformation() {
	}
		
	@Override
	public Pair<SSANewInstruction, SSAInvokeInstruction> scheduleSiteForTask(Integer ssaVariable) {
		return scheduleSitesByTask.get(ssaVariable);
	}
	
}
