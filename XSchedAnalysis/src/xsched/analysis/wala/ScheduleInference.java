package xsched.analysis.wala;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import xsched.analysis.core.AnalysisTask;
import xsched.analysis.core.AnalysisSchedule;
import xsched.analysis.core.ScheduleSite;
import xsched.analysis.core.TaskVariable;
import xsched.analysis.core.ScheduleSite.Multiplicity;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.NewSiteReference;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.types.annotations.Annotations;

public class ScheduleInference {

	public static final TypeName TaskMethodAnnotation = TypeName.findOrCreate("Lxsched/TaskMethod");
	public static final TypeReference TaskType = TypeReference.findOrCreate(ClassLoaderReference.Extension, "Lxsched/Task");
	public static final String HappensBeforeSignature = "xsched.Task.hb(Lxsched/Task;)V";
	
	public static boolean isTaskMethod(IMethod method) {
		return Annotations.hasAnnotation(method, TaskMethodAnnotation);
	}
	
	//in a normal program there can not be a "new Task()" and we assume that the translation
	//is correct. Therefore, we can identify a "schedule site" by looking at the second param (first is 'this') and
	//checking whether that is a "new Task()" call
	//returns the task creation site if invoke is a schedule, or null otherwise
	public static SSANewInstruction getTaskCreationSiteForScheduleInstruction(SSAInvokeInstruction invoke, DefUse thisDefUse) {		
		CallSiteReference callSite = invoke.getCallSite();
		MethodReference calledMethod = callSite.getDeclaredTarget();
		
		
		if(calledMethod.getNumberOfParameters() >= 2 && calledMethod.getParameterType(1).equals(TaskType)) {
			int useValue = invoke.getUse(1);
			SSAInstruction def = thisDefUse.getDef(useValue);
			if(def instanceof SSANewInstruction) {
				return (SSANewInstruction)def;
			}
		}
		return null;
	}
	
	public static AnalysisSchedule<CGNode, WalaScheduleSite> populateScheduleAnalysis(CallGraph cg, Collection<CGNode> taskMethodNodes) {
		ScheduleInference inference = new ScheduleInference(cg);
		for(CGNode taskMethodNode : taskMethodNodes) {
			inference.analyzeTaskNode(taskMethodNode);
		}
		return inference.analysis;
	}

	/*
	 * instance implementation
	 */
	
	private final AnalysisSchedule<CGNode, WalaScheduleSite> analysis = new AnalysisSchedule<CGNode, WalaScheduleSite>();
	private final CallGraph cg;	
	//loops that do not pass through task methods
	private final LoopFinder<CGNode> callGraphLoops;
	
	private ScheduleInference(CallGraph cg) {
		this.cg = cg;
		this.callGraphLoops = new LoopFinder<CGNode>(cg, new LoopFinder.NodeFilter<CGNode>() {
			@Override
			public boolean ignoreNode(CGNode node) {
				return isTaskMethod(node.getMethod());
			}
		});		
	}
	
	private HashMap<SSACFG, LoopFinder<ISSABasicBlock>> loopFindersByCFG = new HashMap<SSACFG, LoopFinder<ISSABasicBlock>>();
	private LoopFinder<ISSABasicBlock> loopFinderForCFG(SSACFG cfg) {
		LoopFinder<ISSABasicBlock> result = loopFindersByCFG.get(cfg);
		if(result != null)
			return result;
		
		result = new LoopFinder<ISSABasicBlock>(cfg, null);
		loopFindersByCFG.put(cfg, result);
		return result;
	}

	protected void analyzeTaskNode(CGNode thisNode) {
		assert(isTaskMethod(thisNode.getMethod()));
		
		boolean thisNodeInsideLoop = callGraphLoops.isInLoop(thisNode);
		
		AnalysisTask<CGNode, WalaScheduleSite> thisTask = analysis.taskForID(thisNode);
		
		IMethod thisMethod = thisNode.getMethod();
		IR thisIR = thisNode.getIR();
		SymbolTable thisSymTab = thisIR.getSymbolTable();
		DefUse thisDefUse = thisNode.getDU();
		
		SSACFG thisCFG = thisIR.getControlFlowGraph();
		LoopFinder<ISSABasicBlock> cfgLoops = loopFinderForCFG(thisCFG);
		
		//handle task parameters
		for(int i = 0; i < thisMethod.getNumberOfParameters(); i++) {
			TypeReference paramType = thisMethod.getParameterType(i);
			if(paramType.equals(TaskType)) {				
				thisTask.addFormalParameter(i);
			}
		}
		
		ArrayList<SSAInvokeInstruction> happensBeforeCalls = new ArrayList<SSAInvokeInstruction>(); 
		//handle schedule sites and hb arrows
		for(ISSABasicBlock basicBlock : thisCFG) {
			boolean bbInsideLoop = cfgLoops.isInLoop(basicBlock);
			
			for(SSAInstruction instruction : basicBlock) {				
				if(instruction instanceof SSAInvokeInstruction) {
					SSAInvokeInstruction invoke = (SSAInvokeInstruction)instruction;
					CallSiteReference callSite = invoke.getCallSite();
					
					//instruction is a schedule site?
					SSANewInstruction taskCreationSite = getTaskCreationSiteForScheduleInstruction(invoke, thisDefUse); 
					if(taskCreationSite != null) {
						//invoke is really a schedule site
						
						//create a new schedule site in the analysis
						Multiplicity multiplicity = thisNodeInsideLoop || bbInsideLoop ? Multiplicity.multipleUnordered : Multiplicity.single;
						WalaScheduleSite site = new WalaScheduleSite(thisMethod, taskCreationSite.getNewSite());
						site.setSSAInvokeInstruction(invoke);
						
						ScheduleSite<CGNode, WalaScheduleSite> scheduleSite = thisTask.addScheduleSite(site, multiplicity);
						
						//add possible targets
						for(CGNode target : cg.getPossibleTargets(thisNode, callSite)) {						
							AnalysisTask<CGNode, WalaScheduleSite> targetTask = analysis.taskForID(target);
							scheduleSite.addPossibleTaskTarget(targetTask);
						}
					} else if(callSite.getDeclaredTarget().getSignature().equals(HappensBeforeSignature)) {
						happensBeforeCalls.add(invoke);
					}
				}
			}
		}
		
		//now iterate over all the happens-before calls such as t1.hb(t2); that we have collected above
		for(SSAInvokeInstruction invoke : happensBeforeCalls) {			
			int lhsValue = invoke.getUse(0);
			int rhsValue = invoke.getUse(1);
			TaskVariable<?> lhs = findTaskVariable(thisMethod, lhsValue, thisTask, thisSymTab, thisDefUse);
			TaskVariable<?> rhs = findTaskVariable(thisMethod, rhsValue, thisTask, thisSymTab, thisDefUse);
				
			//XXX TODO check for some simple patterns; don't just believe the hb call in the program!
			if(lhs != null && rhs != null)
				lhs.happensBefore(rhs);			
		}
		
		
		//now handle the param flow between schedule sites
		for(ScheduleSite<CGNode, WalaScheduleSite> scheduleSite : thisTask.scheduleSites()) {
			WalaScheduleSite walaSite = scheduleSite.id;
			SSAInvokeInstruction invoke = walaSite.ssaInvokeInstruction();
			MethodReference methodRef = invoke.getDeclaredTarget();
				
			for(int i = 0; i < invoke.getNumberOfUses(); i++) {
				if(methodRef.getParameterType(i).equals(TaskType)) {
					int value = invoke.getUse(i);
					TaskVariable<?> paramValue = findTaskVariable(thisMethod, value, thisTask, thisSymTab, thisDefUse);
					if(paramValue != null)
						scheduleSite.addActualParameter(i, paramValue);					
				}
			}
		}
		
	}
		
	//returns null if we can't find a local task variable (e.g., if the task was read from a field or comes from a phi node)
	private TaskVariable<?> findTaskVariable(IMethod method, int ssaValue, AnalysisTask<CGNode, WalaScheduleSite> task, SymbolTable symTab, DefUse defUse) {
		
		if(symTab.isParameter(ssaValue)) {
			//note: this line relies on the fact that for parameters, the ssa value and the param position are equal!
			//we assert a "similar" thing here, even though we should assert that the first occurrence of ssaValue is equal to the ssa value
			assert symTab.getParameterValueNumbers()[ssaValue] == ssaValue;
			return task.formalParameter(ssaValue);
		}
		
		SSAInstruction def = defUse.getDef(ssaValue);
		if(def instanceof SSANewInstruction) {			
			NewSiteReference taskCreationSite = ((SSANewInstruction)def).getNewSite();
			WalaScheduleSite key = new WalaScheduleSite(method, taskCreationSite);
			return task.scheduleSite(key);
		} else {
			return null;
		}
		
	}
	
}