package xsched.analysis.wala.schedule_extraction;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import com.ibm.wala.util.debug.Assertions;

public final class JoinNodeFlowData extends NormalNodeFlowData {

	//a phi node in a certain context can point to multiple task variables from different contexts
	private HashMap<PhiVariable, Set<TaskVariable>> phiMappings;
	private final EdgeFlowData[] incoming;
	
	JoinNodeFlowData(int numIncomingEdges) {
		//this constructor is called when the initial flow data instances are created; so elements in incoming[] can be null before the meet operation ran
		incoming = new EdgeFlowData[numIncomingEdges];
	}
	
	JoinNodeFlowData(EdgeFlowData[] incoming) {
		this.incoming = incoming;
		
		for(int i = 0; i < incoming.length; i++) {
			EdgeFlowData edge = incoming[i];
			assert edge != null; //should not happen after we ran the meet operator
			NormalNodeFlowData dat = edge.data();
			if(dat != null) {
				this.mergeState(dat);
			}
		}
		Assertions.UNREACHABLE("todo: merge");
	}
	
	@Override
	void initEmpty() {
		super.initEmpty();
	}
	
	@Override
	public NormalNodeVisitor createNodeVisitor() {
		return new JoinNodeVisitor(this);
	}
	
	EdgeFlowData incomingEdgeAtPosition(int pos) {
		return incoming[pos];
	}
	
	NormalNodeFlowData incomingDataAtPosition(int pos) {
		EdgeFlowData edge = incoming[pos];
		if(edge != null) {
			return edge.data();
		} else {
			return null;
		}
	}
	
	void addAllPhiVariables(PhiVariable phi, Collection<TaskVariable> toAdd) {
		if (phiMappings == null)
			phiMappings = new HashMap<PhiVariable, Set<TaskVariable>>();
		
		Set<TaskVariable> tasks = phiMappings.get(phi);
		if(tasks == null) {
			tasks = new HashSet<TaskVariable>();
			phiMappings.put(phi, tasks);			
		}
		tasks.addAll(toAdd);
	}
	
	void addPhiVariable(PhiVariable phi, TaskVariable task) {
		if (phiMappings == null)
			phiMappings = new HashMap<PhiVariable, Set<TaskVariable>>();
		
		Set<TaskVariable> tasks = phiMappings.get(phi);
		if(tasks == null) {
			tasks = new HashSet<TaskVariable>();
			phiMappings.put(phi, tasks);
			
		}
		tasks.add(task);		
	}
	
	
	@Override
	Set<TaskVariable> taskVariableForSSAVariable(LoopContext ctxt, int ssaVariable) {
		
		if(phiMappings != null) {
			Set<TaskVariable> tasks = phiMappings.get(new PhiVariable(ctxt, ssaVariable));
			if(tasks != null)
				return tasks;
		}
		return super.taskVariableForSSAVariable(ctxt, ssaVariable);
	}
	
	@Override
	boolean stateEquals(FlowData otherData) {
		return super.stateEquals(otherData);
	}
	
	protected void mergeState(NormalNodeFlowData other) {
		this.happensBeforeEdges.addAll(other.happensBeforeEdges);
		this.loopContexts.addAll(other.loopContexts);
		this.scheduledTasks.addAll(other.scheduledTasks);
		//TODO a normal node needs phi mappings, too, i guess!!!! so it's wrong that they are only in here
	}

	@Override
	public void copyState(FlowData v) {
		super.copyState(v);
		assert(v instanceof JoinNodeFlowData);
		JoinNodeFlowData other = (JoinNodeFlowData)v;
		assert other.incoming.length == incoming.length;
		
		for(int i = 0; i < incoming.length; i++) {
			incoming[i] = other.incoming[i];
		}
		
		if (other.phiMappings != null) {
			for(Entry<PhiVariable, Set<TaskVariable>> entry : other.phiMappings.entrySet()) {
				this.addAllPhiVariables(entry.getKey(), entry.getValue());
			}
		}
	}
	
	@Override
	public String toString() {
		return "Join Node Flow Data";
	}
}
