package xsched.analysis.wala;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import xsched.analysis.core.TaskSchedule;
import xsched.analysis.core.TaskScheduleManager;
import xsched.analysis.wala.schedule_extraction.NormalNodeFlowData;
import xsched.analysis.wala.schedule_extraction.TaskVariable;
import xsched.analysis.wala.util.SimpleGraph;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSACache;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.graph.traverse.FloydWarshall;

public class WalaTaskScheduleManager implements TaskScheduleManager<Integer> {

	public static WalaTaskScheduleManager make(SSACache cache, IR ir, NormalNodeFlowData flowData) {
		assert WalaConstants.isTaskMethod(ir.getMethod().getReference());
		
		WalaTaskScheduleManager result = new WalaTaskScheduleManager(ir, flowData.partialSchedule());
		
		DefUse defUse = cache.findOrCreateDU(ir, Everywhere.EVERYWHERE);
		
		//this is mildly annoying because this code has to agree with the init code in TaskScheduleSolver
		//but it's not 100% obvious how to do it much better;
		//we could iterate through all the nodes in the partial schedule to see what ssa variables are parameters etc but then we lose the ordering
		//of the param array which is important; also there can be several occurrences of the same ssa variable in the partial schedule with different loop contexts
		//we could also compute the "occurrences" map here already which gives us all the task ssa variables but then we still would have to find the formal
		//parameter ssa variables in there and find their place... so i just do it the 'less robust' way
		for(int i = 0; i < ir.getNumberOfParameters(); i++) {
			TypeReference paramType = ir.getParameterType(i);
			if(WalaConstants.isTaskType(paramType)) {
				int ssaVariable = ir.getParameter(i);
				result.formalParameters.add(ssaVariable);
			}
		}
		
		Iterator<SSAInstruction> instructions = ir.iterateNormalInstructions();
		while(instructions.hasNext()) {
			SSAInstruction instruction = instructions.next();
			if(instruction instanceof SSAInvokeInstruction) {
				SSAInvokeInstruction invoke = (SSAInvokeInstruction)instruction;
				if(WalaConstants.isTaskMethod(invoke.getDeclaredTarget())) {
					//the task as an ssa variable
					int ssaTaskVariable = invoke.isStatic() ? invoke.getUse(0) : invoke.getUse(1);
					
					//the corresponding new site; we just trust the compiler that there is one
					SSANewInstruction newTaskInstruction = (SSANewInstruction)defUse.getDef(ssaTaskVariable);
					result.newSitesBySSAVariable.put(ssaTaskVariable, newTaskInstruction);
					result.callSitesBySSAVariable.put(ssaTaskVariable, invoke);
				
				}
			}
		}
				
		return result;
	}
	
	private final HashMap<Integer, SSANewInstruction> newSitesBySSAVariable = new HashMap<Integer, SSANewInstruction>();
	private final HashMap<Integer, SSAInvokeInstruction> callSitesBySSAVariable = new HashMap<Integer, SSAInvokeInstruction>();
	//the list of formal parameters that are actually task variables; other parameters are left out
	//e.g., task method is foo(Task t, int x, Task a) then the formal parameters array is [t, a]
	private final List<Integer> formalParameters = new ArrayList<Integer>();
	
	//those fields are only used until initializeFullSchedule() is called; they will be cleared then
	private SimpleGraph<TaskVariable> partialSchedule;
	private IR ir;
	
	private WalaTaskScheduleManager(IR ir, SimpleGraph<TaskVariable> partialSchedule) {
		this.ir = ir;
		this.partialSchedule = partialSchedule;
	}
	
	public SSANewInstruction newSiteForNode(Integer ssaVariable) {
		return newSitesBySSAVariable.get(ssaVariable);
	}
	
	public SSAInvokeInstruction scheduleSiteForNode(Integer ssaVariable) {
		return callSitesBySSAVariable.get(ssaVariable);
	}
			
	@Override
	public ArrayList<Integer> actualParametersForNode(Integer node) {
		SSAInvokeInstruction invoke = callSitesBySSAVariable.get(node);
		ArrayList<Integer> actuals = new ArrayList<Integer>();
		
		MethodReference target = invoke.getDeclaredTarget();
		for(int i = 1; i < invoke.getNumberOfParameters(); i++) {
			//MethodReference.getParameterType() does not include "this" but that's OK because "this" can never be a task anyways
			if(WalaConstants.isTaskType(target.getParameterType(i-1))) {
				actuals.add(invoke.getUse(i));
			}
		}
		
		return actuals;
	}

	@Override
	public List<Integer> formalTaskParameterNodes() {
		return formalParameters;
	}

	@Override
	public Set<Integer> scheduleSiteNodes() {
		assert callSitesBySSAVariable.keySet().equals(newSitesBySSAVariable.keySet());
		return callSitesBySSAVariable.keySet();
	}

	@Override
	public void initializeFullSchedule(TaskSchedule<Integer, ?> schedule) {
		
		//for transitive information
		int[][] paths = FloydWarshall.shortestPathLengths(partialSchedule);
		
		//for each ssaVariable, find the nodes in the partial schedule that are occurrences of it
		HashMap<Integer, Set<Integer>> occurrences = this.computeOccurrences();
		assert schedule.numberOfAllTaskVariables() == occurrences.keySet().size();
		
		//force the occurrences (ssa variables) into a nice array so that we can iterate in a diagonal matrix style
		//this breaks encapsulation because we assume that the schedule stores task variables in an array and they are actually indexes
		int numTasks = schedule.numberOfAllTaskVariables();
		for(int lhsTaskVariable = 0; lhsTaskVariable < numTasks; lhsTaskVariable++) {
			for(int rhsTaskVariable = lhsTaskVariable; rhsTaskVariable < numTasks; rhsTaskVariable++) {
				Integer lhsSSA = schedule.nodeForTaskVariable(lhsTaskVariable);
				Integer rhsSSA = schedule.nodeForTaskVariable(rhsTaskVariable);
				
				TaskSchedule.Relation relation = computeRelation(occurrences, paths, lhsSSA, rhsSSA);
				//the addRelation automatically adds the inverse, too
				schedule.addRelationForTaskVariables(lhsTaskVariable, relation, rhsTaskVariable);
			} 
		}
		
		this.partialSchedule = null;
		this.ir = null;
	}
	
	private boolean isOutsideLoop(int occurrence) {
		TaskVariable variable = partialSchedule.getNode(occurrence);
		return variable.loopContext().isEmpty();
	}
	
	//a map from ssa variable to node in the schedule graph
	private HashMap<Integer, Set<Integer>> computeOccurrences() {
		HashMap<Integer, Set<Integer>> occurrences = new HashMap<Integer, Set<Integer>>();
		Iterator<TaskVariable> taskVariables = partialSchedule.iterator();
		while(taskVariables.hasNext()) {
			TaskVariable task = taskVariables.next();
			Set<Integer> occs = occurrences.get(task.ssaVariable());
			if(occs == null) {
				occs = new HashSet<Integer>();
				occurrences.put(task.ssaVariable(), occs);
			}
			occs.add(partialSchedule.getNumber(task));
		}
		return occurrences;
	}
	
	private boolean isNow(int lhsSSA) {
		IMethod method = ir.getMethod();
		if(method.isStatic()) {
			return ir.getParameter(0) == lhsSSA;
		} else {
			return ir.getParameter(1) == lhsSSA;
		}
	
	}
	
	private TaskSchedule.Relation computeRelation(HashMap<Integer, Set<Integer>> occurrences, int[][] paths, int lhsSSA, int rhsSSA) {
		TaskSchedule.Relation result = null;
		
		if(this.isNow(lhsSSA)) {
			if(lhsSSA == rhsSSA) {
				return TaskSchedule.Relation.singleton;
			} else {
				return TaskSchedule.Relation.happensBefore;
			}
		} else if(this.isNow(rhsSSA)) {
			return TaskSchedule.Relation.happensAfter;
		}
		
		if(lhsSSA == rhsSSA) {
			Set<Integer> occs = occurrences.get(lhsSSA);
			if(occs.size() == 1) {
				assert isOutsideLoop(occs.iterator().next()) : "there can't be a node inside a loop without having at least one duplicate";
				//a single node that is not in a loop
				return TaskSchedule.Relation.singleton;
			} else {
				//more than one occurrence of the same task 
				//the task can be at most ordered
				result = TaskSchedule.Relation.ordered;
				for(int lhsNode : occurrences.get(lhsSSA)) {
					for(int rhsNode : occurrences.get(rhsSSA)) {
						if(lhsNode == rhsNode && isOutsideLoop(lhsNode)) {
							//that's OK; it's the first iteration of the loop
						} else if(lhsNode == rhsNode && ! isOutsideLoop(lhsNode)) {
							//make sure that the node is ordered with itself
							if(paths[lhsNode][lhsNode] == Integer.MAX_VALUE)
								result = TaskSchedule.Relation.unordered;
						}
					}
				}
				return result;
			}
		}
		
		for(int lhsNode : occurrences.get(lhsSSA)) {
			for(int rhsNode : occurrences.get(rhsSSA)) {
				assert lhsNode != rhsNode; //lhs != rhs and therefore their occurrences must be different, too
				
				if(paths[lhsNode][rhsNode] == Integer.MAX_VALUE) {
					//no lhs->rhs path
					if(paths[rhsNode][lhsNode] == Integer.MAX_VALUE) {
						//no lhs->rhs and no rhs->lhs 
						result = TaskSchedule.Relation.unordered;						
					} else {
						//no lhs-> but lhs<-rhs
						if(result == null) {
							result = TaskSchedule.Relation.happensAfter;
						} else {
							switch(result) {
							case happensBefore: result = TaskSchedule.Relation.ordered; break;
							//we don't change ordered or unordered
							}
						}
					}
				} else {
					//lhs -> rhs
					if(paths[rhsNode][lhsNode] == Integer.MAX_VALUE) {
						//lhs->rhs but not rhs->lhs 
						if(result == null) {
							result = TaskSchedule.Relation.happensBefore;
						} else {
							switch(result) {
							case happensAfter: result = TaskSchedule.Relation.ordered; break;
							//we don't change ordered or unordered
							}
						}
					} else {
						//lhs<->rhs
						if(result == null) {
							 result = TaskSchedule.Relation.ordered;
						} else {
							switch(result) {
							case happensBefore: result = TaskSchedule.Relation.ordered; break;
							case happensAfter: result = TaskSchedule.Relation.ordered; break;
							//we don't change ordered or unordered
							}
						}
					}
				}
			}
		}
		
		return result;
	}
}
