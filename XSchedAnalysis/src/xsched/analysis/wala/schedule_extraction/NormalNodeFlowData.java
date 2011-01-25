package xsched.analysis.wala.schedule_extraction;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import xsched.analysis.core.TaskSchedule;
import xsched.analysis.wala.WalaScheduleSitesInformation;
import xsched.analysis.wala.util.SimpleGraph;

import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.util.collections.Pair;
import com.ibm.wala.util.graph.traverse.FloydWarshall;

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
		this.schedule = new SimpleGraph<TaskVariable>();
		this.schedule.addAllNodesAndEdges(other.schedule);
		
		if (other.phiMappings != null) {
			this.phiMappings = new HashMap<PhiVariable, Set<TaskVariable>>();
			for(Entry<PhiVariable, Set<TaskVariable>> entry : other.phiMappings.entrySet()) {
				this.addAllPhiVariables(entry.getKey(), entry.getValue());
			}
		}
	}
	
	//a map from ssa variable to node in the schedule graph
	private HashMap<Integer, Set<Integer>> computeOccurrences() {
		HashMap<Integer, Set<Integer>> occurrences = new HashMap<Integer, Set<Integer>>();
		Iterator<TaskVariable> taskVariables = schedule.iterator();
		while(taskVariables.hasNext()) {
			TaskVariable task = taskVariables.next();
			Set<Integer> occs = occurrences.get(task.ssaVariable);
			if(occs == null) {
				occs = new HashSet<Integer>();
				occurrences.put(task.ssaVariable, occs);
			}
			occs.add(schedule.getNumber(task));
		}
		return occurrences;
	}
	
	/**
	 * compress the node flow data into a task schedule where each task ssa variable is related with each other
	 * @return
	 */
	public TaskSchedule<Integer, Pair<SSANewInstruction, SSAInvokeInstruction>> makeTaskSchedule(WalaScheduleSitesInformation info) {
		
		final HashMap<Integer, Set<Integer>> occurrences = computeOccurrences();
		
		
		TaskSchedule<Integer, Pair<SSANewInstruction, SSAInvokeInstruction>> result = new TaskSchedule<Integer, Pair<SSANewInstruction, SSAInvokeInstruction>>(info, occurrences.keySet()) {
			
			private boolean isOutsideLoop(int occurrence) {
				TaskVariable variable = schedule.getNode(occurrence);
				return variable.loopContext.isEmpty();
			}
			
			private Relation computeRelation(int[][] paths, int lhs, int rhs) {
				Relation result = null;
				
				if(lhs == rhs) {
					Set<Integer> occs = occurrences.get(lhs);
					if(occs.size() == 1) {
						assert isOutsideLoop(occs.iterator().next()) : "there can't be a node inside a loop without having at least one duplicate";
						//a single node that is not in a loop
						return Relation.singleton;
					} else {
						//more than one occurrence of the same task 
						//the task can be at most ordered
						result = Relation.ordered;
						for(int lhsNode : occurrences.get(lhs)) {
							for(int rhsNode : occurrences.get(rhs)) {
								if(lhsNode == rhsNode && isOutsideLoop(lhsNode)) {
									//that's OK; it's the first iteration of the loop
								} else if(lhsNode == rhsNode && ! isOutsideLoop(lhsNode)) {
									//make sure that the node is ordered with itself
									if(paths[lhsNode][lhsNode] == Integer.MAX_VALUE)
										result = Relation.unordered;
								}
							}
						}
						return result;
					}
				}
				
				for(int lhsNode : occurrences.get(lhs)) {
					for(int rhsNode : occurrences.get(rhs)) {
						assert lhsNode != rhsNode; //lhs != rhs and therefore their occurrences must be different, too
						
						if(paths[lhsNode][rhsNode] == Integer.MAX_VALUE) {
							//no lhs->rhs path
							if(paths[rhsNode][lhsNode] == Integer.MAX_VALUE) {
								//no lhs->rhs and no rhs->lhs 
								result = Relation.unordered;						
							} else {
								//no lhs-> but lhs<-rhs
								if(result == null) {
									result = Relation.happensAfter;
								} else {
									switch(result) {
									case happensBefore: result = Relation.ordered; break;
									//we don't change ordered or unordered
									}
								}
							}
						} else {
							//lhs -> rhs
							if(paths[rhsNode][lhsNode] == Integer.MAX_VALUE) {
								//lhs->rhs but not rhs->lhs 
								if(result == null) {
									result = Relation.happensBefore;
								} else {
									switch(result) {
									case happensAfter: result = Relation.ordered; break;
									//we don't change ordered or unordered
									}
								}
							} else {
								//lhs<->rhs
								if(result == null) {
									 result = Relation.ordered;
								} else {
									switch(result) {
									case happensBefore: result = Relation.ordered; break;
									case happensAfter: result = Relation.ordered; break;
									//we don't change ordered or unordered
									}
								}
							}
						}
					}
				}
				
				return result;
			}
			
			@Override
			protected void computeFullSchedule() {
				//for transitive information
				int[][] paths = FloydWarshall.shortestPathLengths(schedule);
								
				//force the occurrences (ssa variables) into a nice array so that we can iterate in a diagonal matrix style
				ArrayList<Integer> tasks = new ArrayList<Integer>(occurrences.keySet());
				int numTasks = tasks.size();
				for(int lhsIndex = 0; lhsIndex < numTasks; lhsIndex++) {
					for(int rhsIndex = lhsIndex; rhsIndex < numTasks; rhsIndex++) {
						int lhs = tasks.get(lhsIndex);
						int rhs = tasks.get(rhsIndex);
						Relation relation = computeRelation(paths, lhs, rhs);
						//the addRelation automatically adds the inverse, too
						this.addRelation(lhs, relation, rhs);
					}
				}
				
			}
			
		};
		
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
