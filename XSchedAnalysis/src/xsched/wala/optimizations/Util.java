package xsched.wala.optimizations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import xsched.analysis.core.AnalysisResult;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.HeapModel;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;

public class Util {

	public static Set<InstanceKey> computeInstanceKeysForVariableAtNode(PointerAnalysis pa, CGNode node, Variable variable) {
		HeapModel heap = pa.getHeapModel();
		Set<InstanceKey> instances = new HashSet<InstanceKey>();
		
		if(variable.isClassInStaticMethod()) {
			instances.add(heap.getInstanceKeyForClassObject(variable.method.getDeclaringClass().getReference()));
		} else {
			PointerKey pointer = heap.getPointerKeyForLocal(node, variable.ssaVariable);
			for(InstanceKey instance : pa.getPointsToSet(pointer)) {
				instances.add(instance);
			}
		}
		
		return instances;
	}

	public static Set<InstanceKey> collectInstanceKeysOfTaskAlsoUsedInParallel(AnalysisResult<CGNode> schedule, CGNode task, Set<InstanceKey> taskInstances, Map<CGNode, Set<InstanceKey>> otherTasks) {
		Set<InstanceKey> parallelInstances = new HashSet<InstanceKey>();
		
		for(CGNode parallel : schedule.parallelTasksFor(task)) {
			Set<InstanceKey> otherInstances = otherTasks.get(parallel);
			parallelInstances.addAll(otherInstances);
		}
		
		parallelInstances.retainAll(taskInstances);
		return parallelInstances;
	}
	
	public static Map<CGNode, Set<InstanceKey>> collectInstanceKeysUsedInParallel(AnalysisResult<CGNode> schedule, Map<CGNode, Set<InstanceKey>> tasks) {
		//
		Map<CGNode, Set<InstanceKey>> usedInParallelByTask = new HashMap<CGNode, Set<InstanceKey>>();
		for(Entry<CGNode, Set<InstanceKey>> entry : tasks.entrySet()) {
			CGNode task = entry.getKey();
			Set<InstanceKey> taskInstances = entry.getValue();
			
			Set<InstanceKey> usedInParallel = collectInstanceKeysOfTaskAlsoUsedInParallel(schedule, task, taskInstances, tasks);
			usedInParallelByTask.put(task, usedInParallel);
		}
		
		return usedInParallelByTask;
	}
	
	public static Map<CGNode, Set<InstanceKey>> mapToInstanceKeys(Map<CGNode, LocalPointsToInfo> info, Set<InstanceKey> filter) {
		 Map<CGNode, Set<InstanceKey>> result = new HashMap<CGNode, Set<InstanceKey>>();
		 for(Entry<CGNode, LocalPointsToInfo> entry : info.entrySet()) {
			 result.put(entry.getKey(), entry.getValue().allInstanceKeys(filter));
		 }
		 return result;
	}
	
	public static <T> boolean containsAny(Set<T> one, Set<T> other) {
		for(T a : one) {
			if(other.contains(a))
				return true;
		}
		return false;
	}
}
