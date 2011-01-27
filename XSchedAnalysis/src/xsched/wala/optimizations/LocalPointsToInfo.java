package xsched.wala.optimizations;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;

public class LocalPointsToInfo {
	private Map<Variable, Set<InstanceKey>> info = new HashMap<Variable, Set<InstanceKey>>();
		
	public Set<InstanceKey> pointsToSet(CGNode node, Variable variable) {
		Set<InstanceKey> set = info.get(variable);
		if(set == null)
			return Collections.emptySet();
		else
			return set;
	}
	
	public Map<Variable, Set<InstanceKey>> info() {
		return info;
	}
	
	public Set<InstanceKey> allInstanceKeys(Set<InstanceKey> filter) {
		Set<InstanceKey> result = new HashSet<InstanceKey>();
		for(Set<InstanceKey> instances : info.values()) {
			result.addAll(instances);
		}
		if(filter != null)
			result.retainAll(filter);
		return result;
	}
	
	public void add(Variable variable, Set<InstanceKey> instances) {
		assert ! info.containsKey(variable);
		info.put(variable, instances);
	}
	
	public void addAll(Variable variable, Set<InstanceKey> instances) {
		Set<InstanceKey> myInstances = info.get(variable);
		if(myInstances == null) {
			myInstances = new HashSet<InstanceKey>();
			info.put(variable, myInstances);
		}
		myInstances.addAll(instances);
	}
	
	public void addAll(LocalPointsToInfo other) {
		for(Entry<Variable, Set<InstanceKey>> entry : other.info.entrySet()) {
			addAll(entry.getKey(), entry.getValue());
		}
	}
}
