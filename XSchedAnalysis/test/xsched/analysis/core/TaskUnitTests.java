package xsched.analysis.core;
import static org.junit.Assert.*;

import org.junit.Test;

import xsched.analysis.core.AnalysisResult;
import xsched.analysis.core.FormalTaskParameter;
import xsched.analysis.core.ParallelTasksResult;
import xsched.analysis.core.ScheduleAnalysis;
import xsched.analysis.core.ScheduleSite;
import xsched.analysis.core.Task;



public class TaskUnitTests {
	@Test
	public void twoUnorderedTasksNoParams() {
		ScheduleAnalysis<String, String> sa = new ScheduleAnalysis<String, String>();
		Task<String, String> X = sa.taskForID("X");
		Task<String, String> A = sa.taskForID("A");
		Task<String, String> B = sa.taskForID("B");
		
		ScheduleSite<String, String> a =  X.addScheduleSite("a", ScheduleSite.Multiplicity.single);
		a.addPossibleTaskTarget(A);
		
		ScheduleSite<String, String> b = X.addScheduleSite("b", ScheduleSite.Multiplicity.single);
		b.addPossibleTaskTarget(B);
		
		AnalysisResult<String, String> analysisResult = X.solveAsRoot();
		ParallelTasksResult<String, String> result = analysisResult.parallelTasksResult;
		
		assertTrue(result.isOrdered(X, X));
		assertTrue(result.isOrdered(X, A));
		assertTrue(result.isOrdered(X, B));
		
		assertTrue(result.isOrdered(A, A));
		assertTrue(result.isParallel(A, B));
		assertTrue(result.isOrdered(B, B));
					
	}
	
	@Test
	public void simpleNowHappensBeforeLaterPatternNoRecursion() {
		ScheduleAnalysis<String, String> sa = new ScheduleAnalysis<String, String>();
				
		Task<String, String> X = sa.taskForID("X");
		Task<String, String> Later = sa.taskForID("Later");
		
		Task<String, String> A = sa.taskForID("A");
		Task<String, String> Worker = sa.taskForID("Work");
		
		Task<String, String> Parallel = sa.taskForID("Parallel");
		
		//task X: a(later)->later
		ScheduleSite<String, String> later = X.addScheduleSite("later", ScheduleSite.Multiplicity.single);
		later.addPossibleTaskTarget(Later);
		
		ScheduleSite<String, String> a =  X.addScheduleSite("a", ScheduleSite.Multiplicity.single);
		a.addPossibleTaskTarget(A);
		
		a.happensBefore(later);
		a.addActualParameter(later);
		
		//task A: work->#1
		ScheduleSite<String, String> worker = A.addScheduleSite("worker", ScheduleSite.Multiplicity.single);
		worker.addPossibleTaskTarget(Worker);
		
		ScheduleSite<String, String> parallel = A.addScheduleSite("parallel", ScheduleSite.Multiplicity.single);
		parallel.addPossibleTaskTarget(Parallel);
		
		FormalTaskParameter param1 = A.addFormalParameter();
		
		worker.happensBefore(param1);
		
		AnalysisResult<String, String> analysisResult = X.solveAsRoot();
		ParallelTasksResult<String, String> result = analysisResult.parallelTasksResult;
		
		assertTrue(result.isOrdered(X, X));
		assertTrue(result.isOrdered(X, A));
		assertTrue(result.isOrdered(X, Later));
		assertTrue(result.isOrdered(X, Worker));
		assertTrue(result.isOrdered(X, Parallel));
		
		assertTrue(result.isOrdered(A, A));
		assertTrue(result.isOrdered(A, Later));
		assertTrue(result.isOrdered(A, Worker));
		assertTrue(result.isOrdered(A, Parallel));
		
		assertTrue(result.isOrdered(Worker, Later));		
		assertTrue(result.isParallel(Worker, Parallel));
		assertTrue(result.isParallel(Later, Parallel));
	}
}
