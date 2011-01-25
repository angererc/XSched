package xsched.analysis.wala.schedule_extraction;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import xsched.analysis.core.TaskSchedule;
import xsched.analysis.wala.util.SimpleGraph;

import com.ibm.wala.ssa.ISSABasicBlock;

public class NormalNodeFlowData extends FlowData {

	private static boolean DEBUG = false;
	
	//null for fresh sets; in meet operations, we will call mergeWithForward edge etc multiple times which will instantiate those
	//in node analysis operations we call duplicate which instantiates those
	//in the initial setup code we call initEmpty()	
	protected Set<LoopContext> loopContexts;
	//a phi node in a certain context can point to multiple task variables from different contexts
	protected HashMap<PhiVariable, Set<TaskVariable>> phiMappings;
	protected SimpleGraph<TaskVariable> schedule; 
	
	protected NormalNodeVisitor visitor;
	
	//just to improve debugging
	final ISSABasicBlock basicBlock;
	
	NormalNodeFlowData(ISSABasicBlock basicBlock) {
		this.basicBlock = basicBlock;
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
		Iterator<TaskVariable> nodes = this.schedule.iterator();
		while(nodes.hasNext()) {
			TaskVariable node = nodes.next();
			if(node.ssaVariable == ssaVariable)
				return true;
		}
		
		return false;
	}
	
	Set<LoopContext> loopContexts() {
		return loopContexts;
	}
	
	void initEmpty() {
		assert this.isInitial();
		this.loopContexts = new HashSet<LoopContext>();
		this.schedule = new SimpleGraph<TaskVariable>();	
	}
	
	NormalNodeFlowData duplicate(ISSABasicBlock forBasicBlock) {
		NormalNodeFlowData data = new NormalNodeFlowData(forBasicBlock);
		data.copyState(this);
		return data;
	}
	
	//never returns null
	Set<TaskVariable> taskVariableForSSAVariable(LoopContext ctxt, int ssaVariable) {
		
		if(phiMappings != null) {
			Set<TaskVariable> tasks = phiMappings.get(new PhiVariable(ctxt, ssaVariable));
			if(tasks != null)
				return tasks;
		}
		
		//ssaVariable is not a phi node, then it must be a scheduled task
		TaskVariable scheduledTask = new TaskVariable(ctxt, ssaVariable);
		if(schedule.containsNode(scheduledTask)) {
			return Collections.singleton(scheduledTask);
		} else {
			return Collections.emptySet();
		}
	}
	
	void addLoopContext(LoopContext lc) {
		if(DEBUG)
			System.out.println("NormalNodeFlowData: node of block " + basicBlock.getGraphNodeId() + " adding loop context: " + lc);
		this.loopContexts.add(lc);
	}
	
	void addTaskScheduleSite(TaskVariable variable) {
		if(DEBUG)
			System.out.println("NormalNodeFlowData: node of block " + basicBlock.getGraphNodeId() + " adding task variable: " + variable);
		schedule.addNode(variable);
	}
	
	void addHappensBeforeEdge(TaskVariable src, TaskVariable trgt) {
		if(DEBUG)
			System.out.println("NormalNodeFlowData: node of block " + basicBlock.getGraphNodeId() + " adding hb edge: " + src + "->" + trgt);		
		this.schedule.addEdge(src, trgt);
	}
	
	boolean isInitial() {
		assert  (loopContexts != null && schedule != null) 
			|| (loopContexts == null && phiMappings == null && schedule == null);
		return loopContexts == null;
	}
	
	void killHappensBeforeRelationshipsContaining(TaskVariable task) {
		this.schedule.removeAllIncidentEdges(task);		
	}

	@Override
	boolean stateEquals(FlowData otherData) {
		assert otherData instanceof NormalNodeFlowData;
		NormalNodeFlowData other = (NormalNodeFlowData)otherData;
		assert ! other.isInitial();
				
		return ! isInitial() && other.loopContexts.equals(loopContexts) 			
			&& other.schedule.stateEquals(schedule) && other.phiMappings.equals(phiMappings);
	}
	
	protected void addAllPhiVariables(PhiVariable phi, Collection<TaskVariable> toAdd) {
		if (phiMappings == null)
			phiMappings = new HashMap<PhiVariable, Set<TaskVariable>>();
		
		Set<TaskVariable> tasks = phiMappings.get(phi);
		if(tasks == null) {
			tasks = new HashSet<TaskVariable>();
			phiMappings.put(phi, tasks);			
		}
		tasks.addAll(toAdd);
	}

	@Override
	public void copyState(FlowData v) {
		assert(v instanceof NormalNodeFlowData);
		assert v != null;
		NormalNodeFlowData other = (NormalNodeFlowData)v;
		assert ! other.isInitial();
		//when duplicating, the basic blocks can be different
		//assert this.isInitial() || other.basicBlock.equals(basicBlock);
		
		this.loopContexts = new HashSet<LoopContext>(other.loopContexts);
		this.schedule = new SimpleGraph<TaskVariable>(other.schedule);	
		
		if (other.phiMappings != null) {
			this.phiMappings = new HashMap<PhiVariable, Set<TaskVariable>>();
			for(Entry<PhiVariable, Set<TaskVariable>> entry : other.phiMappings.entrySet()) {
				this.addAllPhiVariables(entry.getKey(), entry.getValue());
			}
		}
	}
	
	public TaskSchedule<Integer> makeTaskSchedule() {
		TaskSchedule<Integer> result = new TaskSchedule<Integer>();
		
		
		
		return result;
	}
	
	@Override
	public String toString() {
		return "Node Flow Data " + basicBlock.getGraphNodeId();
	}
	
	public void print(PrintStream out) {
		out.println("Loop Contexts: " + loopContexts);
		out.println("Scheduled Tasks: " + this.schedule.nodesToString());
		out.println("Phi Mappings: " + phiMappings);
		out.println("HB Edges: " + this.schedule.edgesToString());
	}
	
}
