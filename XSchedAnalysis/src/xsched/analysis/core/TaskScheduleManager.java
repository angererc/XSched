package xsched.analysis.core;

import java.util.List;
import java.util.Set;

/**
 * in TaskSchedule, we call everything "task variable"; in here, we call everything "node"
 * the task schedule uses an ordering to identify nodes that needs to be mapped to whatever data structure
 * originally represents this task variable.
 * 
 * so for example, assume that the frontend uses a class SSAVariable to represent SSAVariables (funky integers)
 * then we might have SSAVariable(8) and SSAVariable(15) which are called "nodes" and the TV generic is a SSAVariable
 * 
 * the TaskSchedule uses an ordering on those nodes, e.g., [SSAVariable(15), SSAVariable(8)] and uses the indexes of those
 * for the rest; so task variable 0 is then node SSAVariable(15)
 */
public interface TaskScheduleManager<TV> {

	void initializeFullSchedule(TaskSchedule<TV,?> schedule);
	
	List<TV> formalTaskParameterNodes();
	Set<TV> scheduleSiteNodes();
	
	List<TV> actualParametersForNode(TV node);
	
}
