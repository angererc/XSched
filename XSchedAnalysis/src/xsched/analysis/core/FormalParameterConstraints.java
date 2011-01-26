package xsched.analysis.core;

/**
 * constraints relating each pair of actual parameters.
 * those constraints are assembled by the parent task before recursing analysis into the child
 * @author angererc
 *
 */
public class FormalParameterConstraints {
	
	private final TaskSchedule.Relation[][] relations;
	
	public FormalParameterConstraints() {
		this.relations = new TaskSchedule.Relation[0][0];
	}
	
	public FormalParameterConstraints(TaskSchedule<?,?> taskSchedule, int scheduleSite) {
		
		int[] actuals = taskSchedule.actualsForTaskVariable(scheduleSite);
		
		int size = actuals.length;
		this.relations = new TaskSchedule.Relation[size][size];
		//compare parameter 0 with 1, 2, 3... then 1 with 2, 3, ... etc
		for(int i = 0; i < size-1; i++) {
			for(int j = i+1; j < size; j++) {
				int lhs = actuals[i];
				int rhs = actuals[j];
				
				relations[i][j] = taskSchedule.relationForTaskVariables(lhs, rhs);
				relations[j][i] = taskSchedule.relationForTaskVariables(rhs, lhs);				
			}
		}
	}
	
	public int numActualParameters() {
		return relations.length;
	}
	
	public TaskSchedule.Relation relation(int lhs, int rhs) {
		return relations[lhs][rhs];
	}
	
}
