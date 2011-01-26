package xsched.analysis.wala;

import org.junit.Test;

import xsched.analysis.wala.AnalysisProperties;


public class ExtractScheduleFromMethodsTest {

	@Test
	public void justStartWalaSomehow() throws Exception {
		
		AnalysisProperties properties = new AnalysisProperties(				
				"xsched/analysis/wala/Exclusions.txt",
				"bin/testclasses/");
		
		WalaScheduleAnalysisDriver driver = new WalaScheduleAnalysisDriver(properties);
		
		driver.runScheduleAnalysis();
		
	}
}
