package xsched.analysis.core;

public class AnalysisResult<T, SS> {

	public final ParallelTasksResult<T, SS> parallelTasksResult;
	public final FormalParameterResult<T, SS> formalParameterResult;
	
	public AnalysisResult(ParallelTasksResult<T, SS> a,
			FormalParameterResult<T, SS> b) {
		this.parallelTasksResult = a;
		this.formalParameterResult = b;
	}

}
