package xsched.analysis.core;

public class AnalysisResult<Instance, TV, SS> {

	public final ParallelTasksResult<Instance, TV, SS> parallelTasksResult;
	public final FormalParameterResult<Instance, TV, SS> formalParameterResult;
	
	public AnalysisResult(ParallelTasksResult<Instance, TV, SS> a, FormalParameterResult<Instance, TV, SS> b) {
		this.parallelTasksResult = a;
		this.formalParameterResult = b;
	}

}
