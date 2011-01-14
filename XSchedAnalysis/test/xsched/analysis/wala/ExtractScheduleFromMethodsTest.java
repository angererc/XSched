package xsched.analysis.wala;

import org.junit.Test;

import xsched.analysis.wala.AnalysisProperties;
import xsched.analysis.wala.WalaScheduleAnalysisDriver;


public class ExtractScheduleFromMethodsTest {

	@Test
	public void justStartWalaSomehow() throws Exception {
		WalaScheduleAnalysisDriver driver = new WalaScheduleAnalysisDriver();
		
		AnalysisProperties properties = new AnalysisProperties(				
				"xsched/analysis/wala/Exclusions.txt",
				"bin/testclasses/");
		driver.runScheduleAnalysis(properties);
	}
}
