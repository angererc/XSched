package xsched.analysis.wala.schedule_extraction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public final class NodeFlowData extends FlowData {

	//null for fresh sets; in meet operations, we will call mergeWithForward edge etc multiple times which will instantiate those
	//in node analysis operations we call duplicate which instantiates those
	//in the initial setup code we call initEmpty()

	final boolean isLoopHead;
	
	private Set<LoopContext> loopContexts;
	private Set<TaskVariable> scheduledTasks;
	private Set<HappensBeforeEdge> happensBeforeEdges;
	private HashMap<PhiVariable, TaskVariable> phiMappings;
	
	NodeFlowData(boolean isLoopHead) {
		this.isLoopHead = isLoopHead;
	}
	
	Set<LoopContext> loopContexts() {
		return loopContexts;
	}
	
	NodeFlowData duplicate() {
		NodeFlowData data = new NodeFlowData(isLoopHead);
		this.copyState(data);
		return data;
	}
	
	TaskVariable taskVariableForSSAVariable(LoopContext ctxt, int ssaVariable) {
		
		TaskVariable scheduledTask;
		
		if(phiMappings != null) {
			scheduledTask = phiMappings.get(new PhiVariable(ctxt, ssaVariable));
			if(scheduledTask != null)
				return scheduledTask;
		}
		
		//ssaVariable is not a phi node, then it must be a scheduled task
		scheduledTask = new TaskVariable(ctxt, ssaVariable);
		assert scheduledTasks.contains(scheduledTask);
		return scheduledTask;
	}
	
	void initEmpty() {
		this.scheduledTasks = new HashSet<TaskVariable>();
		this.happensBeforeEdges = new HashSet<HappensBeforeEdge>();
	}
	
	void addTaskScheduleSite(TaskVariable variable) {		
		scheduledTasks.add(variable);
	}
	
	void addHappensBeforeEdge(HappensBeforeEdge edge) {
		this.happensBeforeEdges.add(edge);
	}

	@Override
	boolean stateEquals(FlowData otherData) {
		assert otherData instanceof NodeFlowData;
		NodeFlowData other = (NodeFlowData)otherData;
		
		return other.loopContexts.equals(loopContexts) && other.scheduledTasks.equals(scheduledTasks) && other.happensBeforeEdges.equals(happensBeforeEdges);
	}

	@Override
	public void copyState(FlowData v) {
		assert(v instanceof NodeFlowData);
		NodeFlowData other = (NodeFlowData)v;
		this.loopContexts = new HashSet<LoopContext>(other.loopContexts);
		this.scheduledTasks = new HashSet<TaskVariable>(other.scheduledTasks);
		this.happensBeforeEdges = new HashSet<HappensBeforeEdge>(other.happensBeforeEdges);		
	}
	
	@Override
	public String toString() {
		return "Node Flow Data" + (isLoopHead ? " (loop head)" : "");
	}
}
