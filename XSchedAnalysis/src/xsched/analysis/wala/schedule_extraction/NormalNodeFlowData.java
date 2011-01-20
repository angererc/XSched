package xsched.analysis.wala.schedule_extraction;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class NormalNodeFlowData extends FlowData {

	//null for fresh sets; in meet operations, we will call mergeWithForward edge etc multiple times which will instantiate those
	//in node analysis operations we call duplicate which instantiates those
	//in the initial setup code we call initEmpty()	
	protected Set<LoopContext> loopContexts;
	protected Set<TaskVariable> scheduledTasks;
	protected Set<HappensBeforeEdge> happensBeforeEdges;
	
	protected NormalNodeVisitor visitor;
	
	NormalNodeFlowData() {
	}
	
	public NormalNodeVisitor createNodeVisitor() {
		return new NormalNodeVisitor(this);
	}
	
	public NormalNodeVisitor nodeVisitor() {
		if(visitor == null)
			visitor = this.createNodeVisitor();
		
		return visitor;
	}
	
	boolean isTask(int ssaVariable) {
		for(TaskVariable task : scheduledTasks) {
			if(task.ssaVariable == ssaVariable)
				return true;
		}
		return false;
	}
	
	Set<LoopContext> loopContexts() {
		return loopContexts;
	}
	
	NormalNodeFlowData duplicate() {
		NormalNodeFlowData data = new NormalNodeFlowData();
		data.copyState(this);
		return data;
	}
	
	//never returns null
	Set<TaskVariable> taskVariableForSSAVariable(LoopContext ctxt, int ssaVariable) {
			
		//ssaVariable is not a phi node, then it must be a scheduled task
		TaskVariable scheduledTask = new TaskVariable(ctxt, ssaVariable);
		if(scheduledTask != null)
			return Collections.singleton(scheduledTask);
		else
			return Collections.emptySet();
	}
	
	void initEmpty() {
		this.loopContexts = new HashSet<LoopContext>();
		this.scheduledTasks = new HashSet<TaskVariable>();
		this.happensBeforeEdges = new HashSet<HappensBeforeEdge>();
	}
	
	void addLoopContext(LoopContext lc) {
		this.loopContexts.add(lc);
	}
	
	void addTaskScheduleSite(TaskVariable variable) {		
		scheduledTasks.add(variable);
	}
	
	void addHappensBeforeEdge(HappensBeforeEdge edge) {
		this.happensBeforeEdges.add(edge);
	}

	@Override
	boolean stateEquals(FlowData otherData) {
		assert otherData instanceof NormalNodeFlowData;
		NormalNodeFlowData other = (NormalNodeFlowData)otherData;
		
		return other.loopContexts.equals(loopContexts) 
			&& other.scheduledTasks.equals(scheduledTasks) 
			&& other.happensBeforeEdges.equals(happensBeforeEdges);
	}

	@Override
	public void copyState(FlowData v) {
		assert(v instanceof NormalNodeFlowData);
		assert v != null;
		NormalNodeFlowData other = (NormalNodeFlowData)v;
		assert other.loopContexts != null;
		this.loopContexts = new HashSet<LoopContext>(other.loopContexts);
		this.scheduledTasks = new HashSet<TaskVariable>(other.scheduledTasks);
		this.happensBeforeEdges = new HashSet<HappensBeforeEdge>(other.happensBeforeEdges);		
	}
	
	@Override
	public String toString() {
		return "Node Flow Data";
	}
}
