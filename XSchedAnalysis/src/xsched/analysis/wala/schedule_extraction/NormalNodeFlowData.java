package xsched.analysis.wala.schedule_extraction;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import com.ibm.wala.ssa.ISSABasicBlock;

public class NormalNodeFlowData extends FlowData {

	private static boolean DEBUG = false;
	
	//null for fresh sets; in meet operations, we will call mergeWithForward edge etc multiple times which will instantiate those
	//in node analysis operations we call duplicate which instantiates those
	//in the initial setup code we call initEmpty()	
	protected Set<LoopContext> loopContexts;
	protected Set<TaskVariable> scheduledTasks;
	protected Set<HappensBeforeEdge> happensBeforeEdges;
	//a phi node in a certain context can point to multiple task variables from different contexts
	protected HashMap<PhiVariable, Set<TaskVariable>> phiMappings;
	
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
		for(TaskVariable task : scheduledTasks) {
			if(task.ssaVariable == ssaVariable)
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
		this.scheduledTasks = new HashSet<TaskVariable>();
		this.happensBeforeEdges = new HashSet<HappensBeforeEdge>();	
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
		if(scheduledTasks.contains(scheduledTask)) {
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
		scheduledTasks.add(variable);
	}
	
	void addHappensBeforeEdge(HappensBeforeEdge edge) {
		if(DEBUG)
			System.out.println("NormalNodeFlowData: node of block " + basicBlock.getGraphNodeId() + " adding hb edge: " + edge);
		this.happensBeforeEdges.add(edge);
	}
	
	boolean isInitial() {
		assert  (loopContexts != null && happensBeforeEdges != null && scheduledTasks != null) 
			|| (loopContexts == null && phiMappings == null && happensBeforeEdges == null && scheduledTasks == null);
		return loopContexts == null;
	}
	
	void killHappensBeforeRelationshipsContaining(TaskVariable task) {
		ArrayList<HappensBeforeEdge> toDelete = new ArrayList<HappensBeforeEdge>();
		
		for(HappensBeforeEdge edge : happensBeforeEdges) {
			if(edge.lhs.equals(task) || edge.rhs.equals(task))
				toDelete.add(edge);
		}
		
		happensBeforeEdges.removeAll(toDelete);
	}

	@Override
	boolean stateEquals(FlowData otherData) {
		assert otherData instanceof NormalNodeFlowData;
		NormalNodeFlowData other = (NormalNodeFlowData)otherData;
		assert ! other.isInitial();
				
		return ! isInitial() && other.loopContexts.equals(loopContexts) 
			&& other.scheduledTasks.equals(scheduledTasks) 
			&& other.happensBeforeEdges.equals(happensBeforeEdges) && other.phiMappings.equals(phiMappings);
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
		this.scheduledTasks = new HashSet<TaskVariable>(other.scheduledTasks);
		this.happensBeforeEdges = new HashSet<HappensBeforeEdge>(other.happensBeforeEdges);	
		
		if (other.phiMappings != null) {
			this.phiMappings = new HashMap<PhiVariable, Set<TaskVariable>>();
			for(Entry<PhiVariable, Set<TaskVariable>> entry : other.phiMappings.entrySet()) {
				this.addAllPhiVariables(entry.getKey(), entry.getValue());
			}
		}
	}
	
	@Override
	public String toString() {
		return "Node Flow Data " + basicBlock.getGraphNodeId();
	}
	
	public void print(PrintStream out) {
		out.println("Loop Contexts: " + loopContexts);
		out.println("Scheduled Tasks: " + scheduledTasks);
		out.println("Phi Mappings: " + phiMappings);
		out.println("HB Edges: " + happensBeforeEdges);
	}
}
