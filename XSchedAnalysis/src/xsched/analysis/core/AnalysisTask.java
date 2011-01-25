package xsched.analysis.core;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

//TV: task variable type
//SS: schedule site type
public class AnalysisTask<Instance, TV, SS> {
		
	public final Instance id;
	public final TaskSchedule<TV, SS> taskSchedule;
	
	private ArrayList<FormalTaskParameter> formalParameters = new ArrayList<FormalTaskParameter>();
	private HashMap<TV, ScheduleSite<Instance, TV, SS>> scheduleSites = new HashMap<TV, ScheduleSite<Instance, TV, SS>>();
	
	private HashMap<FormalParameterConstraints, AnalysisResult<Instance, TV, SS>> resultsCache = new HashMap<FormalParameterConstraints, AnalysisResult<Instance, TV, SS>>();
	 
	private HashSet<AnalysisTask<Instance, TV, SS>> childrenCache;
		
	AnalysisTask(Instance id, TaskSchedule<TV, SS> taskSchedule) {
		this.id = id;
		this.taskSchedule = taskSchedule;
	}
	
	//call this method and it will compute the parallelTasks set for all tasks that are directly or indirectly scheduled when
	//this is a root task
	public AnalysisResult<Instance, TV, SS> solveAsRoot() {
		return analyze(new FormalParameterConstraints());
	}
	
	private AnalysisResult<Instance, TV, SS> analyze(FormalParameterConstraints myParamConstraints) {
		AnalysisResult<Instance, TV, SS> myResult = resultsCache.get(myParamConstraints);
		if(myResult != null)
			return myResult;
		
		//add the result to the cache immediately to avoid infinite recursion
		myResult = new AnalysisResult<Instance, TV, SS>(new ParallelTasksResult<Instance, TV, SS>(), new FormalParameterResult<Instance, TV, SS>(formalParameters.size()));
		resultsCache.put(myParamConstraints, myResult);
		
		//analyze each schedule site and then compare it with each schedule site and each formal param O(n^2)
		for(ScheduleSite<Instance, TV, SS> scheduleSite : scheduleSites.values()) {
			FormalParameterConstraints scheduleSiteConstraints = new FormalParameterConstraints(scheduleSite.actualParameters());
			
			//for each possible target task of the schedule site, get the analysis result and aggregate all of them			
			FormalParameterResult<Instance, TV, SS> aggregate = new FormalParameterResult<Instance, TV, SS>(scheduleSite.numActualParameters());
			for(AnalysisTask<Instance, TV, SS> siteTask : scheduleSite.possibleTargetTasks()) {
				AnalysisResult<Instance, TV, SS> siteResult = siteTask.analyze(scheduleSiteConstraints);
				myResult.parallelTasksResult.mergeWith(siteResult.parallelTasksResult);				
				aggregate.mergeWith(siteResult.formalParameterResult);				
			}
			
			for(FormalTaskParameter myFormalParam : formalParameters) {
				if(! scheduleSite.doesHappenBefore(myFormalParam)) {
					myResult.formalParameterResult.setTasksNotOrderedBefore(myFormalParam.id, scheduleSite.possibleTargetTasks());
				}
				
				if(! scheduleSite.doesHappenAfter(myFormalParam)) {
					myResult.formalParameterResult.setTasksNotOrderedAfter(myFormalParam.id, scheduleSite.possibleTargetTasks());
				}
				
				//"fold" the parameters and compute the effect for the formal parameter
				//pessimistically assume that the parameter is parallel to all of the schedule site's children
				UnorderedTasksSets<Instance, TV, SS> unorderedChildren = foldParameters(scheduleSite, aggregate, myFormalParam);
				myResult.formalParameterResult.setTasksNotOrderedBefore(myFormalParam.id, unorderedChildren.tasksNotOrderedBefore);
				myResult.formalParameterResult.setTasksNotOrderedAfter(myFormalParam.id, unorderedChildren.tasksNotOrderedAfter);
				
			}
			
			for(ScheduleSite<Instance, TV, SS> otherScheduleSite : scheduleSites.values()) {
				if(! scheduleSite.equals(otherScheduleSite) || scheduleSite.multiplicity == ScheduleSite.Multiplicity.multipleUnordered) {
					//if schedule sites are unordered, their tasks are unordered
					if(! otherScheduleSite.isOrderedWith(scheduleSite)) {
						myResult.parallelTasksResult.setParallel(otherScheduleSite.possibleTargetTasks(), scheduleSite.possibleTargetTasks());
					}
				
					UnorderedTasksSets<Instance, TV, SS> unorderedChildren = foldParameters(scheduleSite, aggregate, otherScheduleSite);
					//intersect tasks that are not ordered before and not ordered after
					HashSet<AnalysisTask<Instance, TV, SS>> unordered = new HashSet<AnalysisTask<Instance, TV, SS>>(unorderedChildren.tasksNotOrderedBefore);
					unordered.retainAll(unorderedChildren.tasksNotOrderedAfter);
					myResult.parallelTasksResult.setParallel(otherScheduleSite.possibleTargetTasks(), unordered);					
				}
			}
			
		}
		
		return myResult;
	}
	
	private UnorderedTasksSets<Instance, TV, SS> foldParameters(ScheduleSite<Instance, TV, SS> scheduleSite, FormalParameterResult<Instance, TV, SS> aggregate, TaskVariable<?> taskVariable) {
		//pessimistically assume that the parameter is parallel to all of the schedule site's children
		UnorderedTasksSets<Instance, TV, SS> unorderedChildren = new UnorderedTasksSets<Instance, TV, SS>(new HashSet<AnalysisTask<Instance, TV, SS>>(scheduleSite.children()), new HashSet<AnalysisTask<Instance, TV, SS>>(scheduleSite.children()));
		for(int i = 0; i < scheduleSite.numActualParameters(); i++) {
			TaskVariable<?> actual = scheduleSite.actualParameter(i);
			
			if(taskVariable.equals(actual)) {
				unorderedChildren.tasksNotOrderedBefore.retainAll(aggregate.tasksNotOrderedBefore(i));
				unorderedChildren.tasksNotOrderedAfter.retainAll(aggregate.tasksNotOrderedAfter(i));						
			} else if(taskVariable.doesHappenBefore(actual)) {
				unorderedChildren.tasksNotOrderedAfter.retainAll(aggregate.tasksNotOrderedAfter(i));						
			} else if(taskVariable.doesHappenAfter(actual)) {
				unorderedChildren.tasksNotOrderedBefore.retainAll(aggregate.tasksNotOrderedBefore(i));
			} else {
				//do nothing
			}
		}
		
		return unorderedChildren;
	}
	
	public Set<AnalysisTask<Instance, TV, SS>> children() {
		if(childrenCache != null)
			return childrenCache;
		
		childrenCache = new HashSet<AnalysisTask<Instance, TV, SS>>();
		
		for(ScheduleSite<Instance, TV, SS> site : scheduleSites.values()) {
			Set<AnalysisTask<Instance, TV, SS>> possibleTasks = site.possibleTargetTasks();
			childrenCache.addAll(possibleTasks);
			for(AnalysisTask<Instance, TV, SS> possibleTask : possibleTasks) {
				childrenCache.addAll(possibleTask.children());
			}
		}
		
		return childrenCache;
	}
	
	public Collection<ScheduleSite<Instance, TV, SS>> scheduleSites() {
		return scheduleSites.values();
	}
	
	public ScheduleSite<Instance, TV, SS> scheduleSite(SS variableID) {
		return scheduleSites.get(variableID);
	}
	
	//isMultiple: can this schedule site be executed multiple times? (in a loop/through recursion)
	public ScheduleSite<Instance, TV, SS> addScheduleSite(TV variableID, ScheduleSite.Multiplicity multiplicity) {
		assert ! scheduleSites.containsKey(variableID);
		ScheduleSite<Instance, TV, SS> site = new ScheduleSite<Instance, TV, SS>(variableID, multiplicity);
		scheduleSites.put(variableID, site);
		return site;
	}
	
	public FormalTaskParameter formalParameter(int i) {
		for(FormalTaskParameter param : formalParameters) {
			if(param.id.intValue() == i)
				return param;
		}
		return null;
	}
 
	public FormalTaskParameter addFormalParameter(int position) {
		FormalTaskParameter param = new FormalTaskParameter(position);
		assert formalParameter(position) == null;
		formalParameters.add(param);
		return param;
	}
		
	@Override
	public int hashCode() {
		return id.hashCode();
	}
	
	@Override
	public boolean equals(Object otherObj) {
		return otherObj == this;
	}

}
