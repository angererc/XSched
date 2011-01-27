package xsched.wala.optimizations;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;

public class GlobalPointsToInfo {

	private final Map<CGNode, LocalPointsToInfo> info = new HashMap<CGNode, LocalPointsToInfo>();

	public static GlobalPointsToInfo make(PointerAnalysis pa, CallGraph cg, Set<Variable> variables) {
		GlobalPointsToInfo result = new GlobalPointsToInfo();
		
		for(Variable variable : variables) {
			for(CGNode node : cg.getNodes(variable.method.getReference())) {
				//we somehow jump wildly through the callgraph because we iterate variables first, then nodes; therefore this is a little less elegant than it would be otherwise
				LocalPointsToInfo local = result.pointsToSet(node);
				if(local == null) {
					local = new LocalPointsToInfo();
					result.set(node, local);
				}
				local.addAll(variable, Util.computeInstanceKeysForVariableAtNode(pa, node, variable));				
			}
		}
		
		return result;
	}
	
	public GlobalPointsToInfo() {
		
	}
		
	public LocalPointsToInfo pointsToSet(CGNode node) {
		LocalPointsToInfo byVariable = info.get(node);
		if(byVariable == null) 
			return new LocalPointsToInfo();
		else
			return byVariable;
	}
	
	public Map<CGNode, LocalPointsToInfo> info() {
		return info;
	}
	
	public void set(CGNode node, LocalPointsToInfo local) {
		assert ! info.containsKey(node);
		info.put(node, local);
	}
	
	public Map<CGNode, Set<InstanceKey>> mapToInstanceKeys(Set<InstanceKey> filter) {
		 Map<CGNode, Set<InstanceKey>> result = new HashMap<CGNode, Set<InstanceKey>>();
		 for(Entry<CGNode, LocalPointsToInfo> entry : info.entrySet()) {
			 result.put(entry.getKey(), entry.getValue().allInstanceKeys(filter));
		 }
		 return result;
	}
	
	//collect all the local points to infos of all nodes reachable by task
	public LocalPointsToInfo collectLocalPointsToSetsReachableByTask(Reachability<CGNode, CGNode> reachability, CGNode task) {
		LocalPointsToInfo result = new LocalPointsToInfo();
		
		result.addAll(this.pointsToSet(task));
		
		for(CGNode nonTask : reachability.nonTaskNodesReachableByTask(task)) {
			result.addAll(this.pointsToSet(nonTask));
		}
		return result;
	}
	
	//filter can be null
	public Map<CGNode, LocalPointsToInfo> collectLocalPointsToSetsForTasks(Reachability<CGNode, CGNode> reachability, Collection<CGNode> tasks) {
		
		Map<CGNode, LocalPointsToInfo> result = new HashMap<CGNode, LocalPointsToInfo>();
		for(CGNode task : tasks) {
			LocalPointsToInfo local = collectLocalPointsToSetsReachableByTask(reachability, task);
			result.put(task, local);
		}
		return result;
	}
}
