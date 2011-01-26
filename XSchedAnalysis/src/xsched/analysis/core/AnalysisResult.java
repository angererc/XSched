package xsched.analysis.core;

/**
 * AnalysisResult is just a tuple to return the results for parallel tasks together with the results for formal parameters
 * @author angererc
 *
 * @param <Instance>
 * @param <TV>
 * @param <SM>
 */
public class AnalysisResult<Instance, TV, SM extends TaskScheduleManager<TV>> {

	public final ParallelTasksResult<Instance, TV, SM> parallelTasksResult;
	public final FormalParameterResult<Instance, TV, SM> formalParameterResult;
	
	public AnalysisResult(ParallelTasksResult<Instance, TV, SM> a, FormalParameterResult<Instance, TV, SM> b) {
		this.parallelTasksResult = a;
		this.formalParameterResult = b;
	}

}
