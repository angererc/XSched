package xsched.analysis.core;

import java.util.Collection;

public interface AnalysisTaskResolver<Instance, TV, SM extends TaskScheduleManager<TV>> {
	Collection<Instance> possibleTargetTasksForSite(AnalysisTask<Instance, TV, SM> task, TV scheduleNode); 
}
