package xsched.analysis.core;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//TV: task variable type
//SS: schedule site type
public class AnalysisTask<Instance, TV, SM  extends TaskScheduleManager<TV>> {
		
	public final Instance id;
	private final TaskSchedule<TV, SM> taskSchedule;
	private final AnalysisSession<Instance, TV, SM> analysis;
	
	//map from task varible int to a collection of possible targets
	private HashMap<Integer, Collection<AnalysisTask<Instance, TV, SM>>> possibleTargetTasksCache;
	private HashMap<FormalParameterConstraints, AnalysisStepResult<Instance, TV, SM>> resultsCache = new HashMap<FormalParameterConstraints, AnalysisStepResult<Instance, TV, SM>>();
	 
	private HashSet<AnalysisTask<Instance, TV, SM>> childrenCache;
		
	AnalysisTask(AnalysisSession<Instance, TV, SM> analysis, Instance id, TaskSchedule<TV, SM> taskSchedule) {
		this.analysis = analysis;
		this.id = id;
		this.taskSchedule = taskSchedule;
	}
	
	public TaskSchedule<TV, SM> taskSchedule() {
		return taskSchedule;
	}
	
	//call this method and it will compute the parallelTasks set for all tasks that are directly or indirectly scheduled when
	//this is a root task
	public AnalysisResult<Instance> solveAsRoot(AnalysisTaskResolver<Instance, TV, SM> resolver) {
		AnalysisStepResult<Instance, TV, SM> result = analyze(resolver, new FormalParameterConstraints());
		//a main task only has the now param	
		assert(result.formalParameterResult.numTaskParameters() == 1);
		return new AnalysisResult<Instance>(result.parallelTasksResult);
	}
	
	private void populatePossibleTaskTargetsCache(AnalysisTaskResolver<Instance, TV, SM> resolver) {
		//first, compute for all schedule sites the possible targets because we need that for all of them anyway
		possibleTargetTasksCache = new HashMap<Integer, Collection<AnalysisTask<Instance, TV, SM>>>(taskSchedule.numberOfNonParameterTaskVariables());
		Iterator<Integer> scheduleSites = taskSchedule.iterateNonParameterTaskVariables();
		while(scheduleSites.hasNext()) {
			int scheduleSite = scheduleSites.next();
			Collection<Instance> taskInstanceIDs = resolver.possibleTargetTasksForSite(this, taskSchedule.nodeForTaskVariable(scheduleSite));
			Collection<AnalysisTask<Instance, TV, SM>> possibleTargetTasks = analysis.tasksWithIDs(taskInstanceIDs);
			this.possibleTargetTasksCache.put(scheduleSite, possibleTargetTasks);
		}
	}
	
	private AnalysisStepResult<Instance, TV, SM> analyze(AnalysisTaskResolver<Instance, TV, SM> resolver, FormalParameterConstraints myParamConstraints) {
		AnalysisStepResult<Instance, TV, SM> myResult = resultsCache.get(myParamConstraints);
		if(myResult != null)
			return myResult;
		
		this.populatePossibleTaskTargetsCache(resolver);
		
		//add the result to the cache immediately to avoid infinite recursion
		myResult = new AnalysisStepResult<Instance, TV, SM>(new ParallelTasksResult<Instance, TV, SM>(), new FormalParameterResult<Instance, TV, SM>(taskSchedule.numberOfFormalParameterTaskVariables()));
		resultsCache.put(myParamConstraints, myResult);
		
		//analyze each schedule site and then compare it with each schedule site and each formal param O(n^2)
		Iterator<Integer> scheduleSites = taskSchedule.iterateNonParameterTaskVariables();
		while(scheduleSites.hasNext()) {
			int scheduleSite = scheduleSites.next();
			
			FormalParameterConstraints scheduleSiteConstraints = new FormalParameterConstraints(taskSchedule, scheduleSite);
		
			Collection<AnalysisTask<Instance, TV, SM>> possibleTargetTasks = possibleTargetTasksCache.get(scheduleSite);			
			//for each possible target task of the schedule site, get the analysis result and aggregate all of them			
			FormalParameterResult<Instance, TV, SM> aggregate = new FormalParameterResult<Instance, TV, SM>(scheduleSiteConstraints.numActualParameters());
			for(AnalysisTask<Instance, TV, SM> siteTask : possibleTargetTasks) {
				AnalysisStepResult<Instance, TV, SM> siteResult = siteTask.analyze(resolver, scheduleSiteConstraints);
				myResult.parallelTasksResult.mergeWith(siteResult.parallelTasksResult);				
				aggregate.mergeWith(siteResult.formalParameterResult);				
			}
			
			//check relation of each schedule site with each incoming parameter
			Iterator<Integer> formalParameters = taskSchedule.iterateFormalParameterTaskVariables();
			while(formalParameters.hasNext()) {
				int myFormalParam = formalParameters.next();
				
				TaskSchedule.Relation relation = taskSchedule.relationForTaskVariables(scheduleSite, myFormalParam);
				switch(relation) {
				case singleton:
					throw new RuntimeException("that can't be... a schedule site and a formal param can't be a singleton");
				case happensBefore:				
					myResult.formalParameterResult.setTasksNotOrderedAfter(myFormalParam, possibleTargetTasks);
					break;
				case happensAfter:
					myResult.formalParameterResult.setTasksNotOrderedBefore(myFormalParam, possibleTargetTasks);
					break;
				case ordered:
					//?!? nothing to do here?
					break;
				case unordered:
					myResult.formalParameterResult.setTasksNotOrderedBefore(myFormalParam, possibleTargetTasks);
					myResult.formalParameterResult.setTasksNotOrderedAfter(myFormalParam, possibleTargetTasks);
					break;
				}
								
				//"fold" the parameters and compute the effect for the formal parameter
				//pessimistically assume that the parameter is parallel to all of the schedule site's children
				UnorderedTasksSets<Instance, TV, SM> unorderedChildren = foldParameters(resolver, scheduleSite, aggregate, myFormalParam);
				myResult.formalParameterResult.setTasksNotOrderedBefore(myFormalParam, unorderedChildren.tasksNotOrderedBefore);
				myResult.formalParameterResult.setTasksNotOrderedAfter(myFormalParam, unorderedChildren.tasksNotOrderedAfter);
			}
			
			//check relation of the schedule site with each other schedule site
			Iterator<Integer> otherScheduleSites = taskSchedule.iterateNonParameterTaskVariables();		
			while(otherScheduleSites.hasNext()) {
				int otherScheduleSite = otherScheduleSites.next();
				Collection<AnalysisTask<Instance, TV, SM>> otherPossibleTargetTasks = possibleTargetTasksCache.get(otherScheduleSite);
													
				TaskSchedule.Relation relation = taskSchedule.relationForTaskVariables(scheduleSite, otherScheduleSite);
				
				//if schedule sites are unordered, their immediate target tasks are unordered
				switch(relation) {
				case singleton:
					//the targets are unordered
					myResult.parallelTasksResult.setParallel(possibleTargetTasks, otherPossibleTargetTasks);
					break;
				default:
					//that's ok
					break;
				}
				
				//relate the children of this schedule site to the other's possible targets
				UnorderedTasksSets<Instance, TV, SM> unorderedChildren = foldParameters(resolver, scheduleSite, aggregate, otherScheduleSite);
				//intersect tasks that are not ordered before and not ordered after
				HashSet<AnalysisTask<Instance, TV, SM>> unordered = new HashSet<AnalysisTask<Instance, TV, SM>>(unorderedChildren.tasksNotOrderedBefore);
				unordered.retainAll(unorderedChildren.tasksNotOrderedAfter);
				myResult.parallelTasksResult.setParallel(otherPossibleTargetTasks, unordered);	
				
			}
		}
		

		return myResult;
	}
	
	//returns the children that may be created as a result of a schedule site, but does NOT include the tasks that may be created at the schedule site itself!
	public Set<AnalysisTask<Instance, TV, SM>> childrenForScheduleSite(int scheduleSite) {
		HashSet<AnalysisTask<Instance, TV, SM>> result = new HashSet<AnalysisTask<Instance, TV, SM>>();
		
		Collection<AnalysisTask<Instance, TV, SM>> possibleTargetTasks = this.possibleTargetTasksCache.get(scheduleSite);
		for(AnalysisTask<Instance, TV, SM> child : possibleTargetTasks) {
			result.addAll(child.children());
		}
		
		return result;
	}
	
	//given a schedule site, the result for the formal parameters and one schedule site (possibly an actual parameter),
	//walk over the list of actuals of the schedule site and find its relation with the task variable 
	private UnorderedTasksSets<Instance, TV, SM> foldParameters(
				AnalysisTaskResolver<Instance, TV, SM> resolver,
				int scheduleSite, 
				FormalParameterResult<Instance, TV, SM> aggregate, 
				int taskVariable) {
		//pessimistically assume that the parameter is parallel to all of the schedule site's children
		Set<AnalysisTask<Instance, TV, SM>> children = this.childrenForScheduleSite(scheduleSite);
		
		UnorderedTasksSets<Instance, TV, SM> unorderedChildren 
					= new UnorderedTasksSets<Instance, TV, SM>(new HashSet<AnalysisTask<Instance, TV, SM>>(children), new HashSet<AnalysisTask<Instance, TV, SM>>(children));
		
		int[] actuals = taskSchedule.actualsForTaskVariable(scheduleSite);
		for(int i = 0; i < actuals.length; i++) {
			int actual = actuals[i];
			
			TaskSchedule.Relation relation = taskSchedule.relationForTaskVariables(taskVariable, actual);
			switch(relation) {
			case singleton:
				unorderedChildren.tasksNotOrderedBefore.retainAll(aggregate.tasksNotOrderedBefore(i));
				unorderedChildren.tasksNotOrderedAfter.retainAll(aggregate.tasksNotOrderedAfter(i));
				break;
			case happensBefore:
				unorderedChildren.tasksNotOrderedAfter.retainAll(aggregate.tasksNotOrderedAfter(i));
				break;
			case happensAfter:
				unorderedChildren.tasksNotOrderedBefore.retainAll(aggregate.tasksNotOrderedBefore(i));
				break;
			case ordered:
				//do nothing?!?
				break;
			default:
				//do nothing
			}
			
		}
		
		return unorderedChildren;
	}
	
	public Set<AnalysisTask<Instance, TV, SM>> children() {
		if(childrenCache != null)
			return childrenCache;
		
		//init first, avoids infinite recursion
		childrenCache = new HashSet<AnalysisTask<Instance, TV, SM>>();
		
		//analyze each schedule site and then compare it with each schedule site and each formal param O(n^2)
		Iterator<Integer> scheduleSites = taskSchedule.iterateNonParameterTaskVariables();
		while(scheduleSites.hasNext()) {
			int scheduleSite = scheduleSites.next();
		
			Collection<AnalysisTask<Instance, TV, SM>> possibleTargetTasks = this.possibleTargetTasksCache.get(scheduleSite);
			childrenCache.addAll(possibleTargetTasks);
			for(AnalysisTask<Instance, TV, SM> possibleTask : possibleTargetTasks) {
				childrenCache.addAll(possibleTask.children());
			}
		}
		
		return childrenCache;
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
