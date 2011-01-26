package xsched.analysis.wala;

import org.junit.Test;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;

import static org.junit.Assert.*;

import xsched.analysis.core.AnalysisResult;
import xsched.analysis.wala.AnalysisProperties;


public class ScheduleAnalysisTest {

	@Test
	public void analyzeTestclasses() throws Exception {
		
		AnalysisProperties properties = new AnalysisProperties(				
				"xsched/analysis/wala/Exclusions.txt",
				"bin/testclasses/");
		
		WalaScheduleAnalysisDriver driver = new WalaScheduleAnalysisDriver(properties);
		
		AnalysisResult<CGNode> result = driver.runScheduleAnalysis();
		AnalysisResult<IMethod> resultByMethod = result.collapse(new AnalysisResult.MappingOperation<CGNode, IMethod>() {
			@Override
			public IMethod map(CGNode i) {
				return i.getMethod();
			} }
		);
		assertEquals(3, driver.mainTaskMethods().size());
		System.out.println(resultByMethod);
	}
}
