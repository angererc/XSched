package xsched.analysis.core;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class AnalysisTask<T, SS> {
		
	public final T id;
	
	private ArrayList<FormalTaskParameter> formalParameters = new ArrayList<FormalTaskParameter>();
	private HashMap<SS, ScheduleSite<T, SS>> scheduleSites = new HashMap<SS, ScheduleSite<T, SS>>();
	
	private HashMap<FormalParameterConstraints, AnalysisResult<T, SS>> resultsCache = new HashMap<FormalParameterConstraints, AnalysisResult<T, SS>>();
	 
	private HashSet<AnalysisTask<T, SS>> childrenCache;
		
	AnalysisTask(T id) {
		this.id = id;
	}
	
	//call this method and it will compute the parallelTasks set for all tasks that are directly or indirectly scheduled when
	//this is a root task
	public AnalysisResult<T, SS> solveAsRoot() {
		return analyze(new FormalParameterConstraints());
	}
	
	private AnalysisResult<T, SS> analyze(FormalParameterConstraints myParamConstraints) {
		AnalysisResult<T, SS> myResult = resultsCache.get(myParamConstraints);
		if(myResult != null)
			return myResult;
		
		//add the result to the cache immediately to avoid infinite recursion
		myResult = new AnalysisResult<T, SS>(new ParallelTasksResult<T, SS>(), new FormalParameterResult<T, SS>(formalParameters.size()));
		resultsCache.put(myParamConstraints, myResult);
		
		//analyze each schedule site and then compare it with each schedule site and each formal param O(n^2)
		for(ScheduleSite<T, SS> scheduleSite : scheduleSites.values()) {
			FormalParameterConstraints scheduleSiteConstraints = new FormalParameterConstraints(scheduleSite.actualParameters());
			
			//for each possible target task of the schedule site, get the analysis result and aggregate all of them			
			FormalParameterResult<T, SS> aggregate = new FormalParameterResult<T, SS>(scheduleSite.numActualParameters());
			for(AnalysisTask<T, SS> siteTask : scheduleSite.possibleTargetTasks()) {
				AnalysisResult<T, SS> siteResult = siteTask.analyze(scheduleSiteConstraints);
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
				UnorderedTasksSets<T, SS> unorderedChildren = foldParameters(scheduleSite, aggregate, myFormalParam);
				myResult.formalParameterResult.setTasksNotOrderedBefore(myFormalParam.id, unorderedChildren.tasksNotOrderedBefore);
				myResult.formalParameterResult.setTasksNotOrderedAfter(myFormalParam.id, unorderedChildren.tasksNotOrderedAfter);
				
			}
			
			for(ScheduleSite<T, SS> otherScheduleSite : scheduleSites.values()) {
				if(! scheduleSite.equals(otherScheduleSite) || scheduleSite.multiplicity == ScheduleSite.Multiplicity.multipleUnordered) {
					//if schedule sites are unordered, their tasks are unordered
					if(! otherScheduleSite.isOrderedWith(scheduleSite)) {
						myResult.parallelTasksResult.setParallel(otherScheduleSite.possibleTargetTasks(), scheduleSite.possibleTargetTasks());
					}
				
					UnorderedTasksSets<T, SS> unorderedChildren = foldParameters(scheduleSite, aggregate, otherScheduleSite);
					//intersect tasks that are not ordered before and not ordered after
					HashSet<AnalysisTask<T, SS>> unordered = new HashSet<AnalysisTask<T, SS>>(unorderedChildren.tasksNotOrderedBefore);
					unordered.retainAll(unorderedChildren.tasksNotOrderedAfter);
					myResult.parallelTasksResult.setParallel(otherScheduleSite.possibleTargetTasks(), unordered);					
				}
			}
			
		}
		
		return myResult;
	}
	
	private UnorderedTasksSets<T, SS> foldParameters(ScheduleSite<T, SS> scheduleSite, FormalParameterResult<T, SS> aggregate, TaskVariable<?> taskVariable) {
		//pessimistically assume that the parameter is parallel to all of the schedule site's children
		UnorderedTasksSets<T, SS> unorderedChildren = new UnorderedTasksSets<T, SS>(new HashSet<AnalysisTask<T, SS>>(scheduleSite.children()), new HashSet<AnalysisTask<T, SS>>(scheduleSite.children()));
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
	
	public Set<AnalysisTask<T, SS>> children() {
		if(childrenCache != null)
			return childrenCache;
		
		childrenCache = new HashSet<AnalysisTask<T, SS>>();
		
		for(ScheduleSite<T, SS> site : scheduleSites.values()) {
			Set<AnalysisTask<T, SS>> possibleTasks = site.possibleTargetTasks();
			childrenCache.addAll(possibleTasks);
			for(AnalysisTask<T, SS> possibleTask : possibleTasks) {
				childrenCache.addAll(possibleTask.children());
			}
		}
		
		return childrenCache;
	}
	
	public Collection<ScheduleSite<T,SS>> scheduleSites() {
		return scheduleSites.values();
	}
	
	public ScheduleSite<T, SS> scheduleSite(SS variableID) {
		return scheduleSites.get(variableID);
	}
	
	//isMultiple: can this schedule site be executed multiple times? (in a loop/through recursion)
	public ScheduleSite<T, SS> addScheduleSite(SS variableID, ScheduleSite.Multiplicity multiplicity) {
		assert ! scheduleSites.containsKey(variableID);
		ScheduleSite<T, SS> site = new ScheduleSite<T, SS>(variableID, multiplicity);
		scheduleSites.put(variableID, site);
		return site;
	}
	
	public FormalTaskParameter formalParameter(int i) {
		return formalParameters.get(i);
	}
	
	public int numFormalParameters() {
		return formalParameters.size();
	}
	
	public FormalTaskParameter addFormalParameter() {
		//formal parameter ID's depend on their location in the formal parameter array
		FormalTaskParameter param = new FormalTaskParameter(formalParameters.size());
		assert ! formalParameters.contains(param);
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
