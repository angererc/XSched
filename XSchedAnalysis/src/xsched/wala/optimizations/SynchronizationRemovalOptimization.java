package xsched.wala.optimizations;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAMonitorInstruction;

import xsched.analysis.core.AnalysisResult;
import xsched.analysis.wala.WalaScheduleAnalysisDriver;

public class SynchronizationRemovalOptimization {

	private final AnalysisResult<CGNode> schedule;
	private final WalaScheduleAnalysisDriver driver;
	
	public SynchronizationRemovalOptimization(WalaScheduleAnalysisDriver driver) {
		this.driver = driver;
		this.schedule = driver.scheduleAnalysisResult();
	}
	
	private void computeRequiredVariablesForNode(GlobalPointsToInfo globalInfo, CGNode node, Set<InstanceKey> usedInParallel, Map<CGNode, Set<Variable>> out) {
		Set<Variable> variables = out.get(node);
		if(variables == null) {
			variables = new HashSet<Variable>();
			out.put(node, variables);
		}
		
		LocalPointsToInfo localInfo = globalInfo.pointsToSet(node);
		for(Entry<Variable, Set<InstanceKey>> entry : localInfo.info().entrySet()) {
			if(Util.containsAny(usedInParallel, entry.getValue())) {
				variables.add(entry.getKey());
			}
		}
	}
	public Map<CGNode, Set<Variable>> computeRequiredSyncPointsByCGNode(Reachability<CGNode, CGNode> reachability) {
						
		//step 1: find all variables that are used for syncing
		Set<Variable> syncVariables = collectSynchronizationVariables();
		//step 2: compute where those variables may point to (context dependent)
		GlobalPointsToInfo globalPointsToInfo = GlobalPointsToInfo.make(driver.pointerAnalysis(), driver.callGraph(), syncVariables);
		//step 3: for each task node, collect all the reachable non task nodes and collect their variable points to sets; so kinda "flatten" the call graph into the task nodes
		Map<CGNode, LocalPointsToInfo> syncedVariablesByTask = globalPointsToInfo.collectLocalPointsToSetsForTasks(reachability, schedule.tasks());
		//we don't want the whole variable points to set info but are happy with just flattening everything into a set of instance keys per task
		//at the same time, filter out all non escaping instances
		Map<CGNode, Set<InstanceKey>> syncedInstancesByTask = Util.mapToInstanceKeys(syncedVariablesByTask, driver.escapeAnalysisResult());
		//per task, find what instance keys are also used by other parallel tasks
		Map<CGNode, Set<InstanceKey>> syncedInParallelByTask = Util.collectInstanceKeysUsedInParallel(schedule, syncedInstancesByTask);
		
		//now we want to "undo" all the folding from above and find for each CGNode reachable by a task what variables it has to keep
		//this could also be mapped from the CG node to the method to get a more "global" but less precise view
		Map<CGNode, Set<Variable>> requiredSyncsByCGNode = new HashMap<CGNode, Set<Variable>>();
		
		for(Entry<CGNode, Set<InstanceKey>> entry : syncedInParallelByTask.entrySet()) {
			CGNode task = entry.getKey();
			Set<InstanceKey> usedInParallel = entry.getValue();
			
			computeRequiredVariablesForNode(globalPointsToInfo, task, usedInParallel, requiredSyncsByCGNode);
			
			//then do the same for other nodes
			for(CGNode nonTaskNode : reachability.nonTaskNodesReachableByTask(task)) {
				computeRequiredVariablesForNode(globalPointsToInfo, nonTaskNode, usedInParallel, requiredSyncsByCGNode);
			}
		}
		
		return requiredSyncsByCGNode;
		
	}
	
	
	
	private Set<Variable> collectSynchronizationVariables() {
		ClassHierarchy classHierarchy = driver.classHierarchy();
		
		Set<Variable> syncPoints = new HashSet<Variable>();
		//step 1: iterate all classes and find synchronization points in their methods
		for(IClass cls : classHierarchy) {
			Collection<IMethod> methods = cls.getDeclaredMethods();
			for(IMethod method : methods) {
				collectSynchronizationSSAVariables(syncPoints, method, driver.irForMethod(method));
			}
		}
		return syncPoints;
	}
		
	private void collectSynchronizationSSAVariables(Set<Variable> syncPoints, IMethod method, IR ir) {
			
		if(method.isSynchronized()) {
			if(method.isStatic()) {
				syncPoints.add(new Variable(method, Variable.CLASS));				
			} else {
				syncPoints.add(new Variable(method, Variable.THIS));
			}
		}
		
		for(SSAInstruction instruction : ir.getInstructions()) {
			if(instruction instanceof SSAMonitorInstruction) {
				SSAMonitorInstruction monitor = (SSAMonitorInstruction)instruction;
				if(monitor.isMonitorEnter()) {
					syncPoints.add(new Variable(method, monitor.getRef()));
				}
			}
		}
		
	}
	
}
