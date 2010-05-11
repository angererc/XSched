package xsched.analysis;

import org.junit.Test;

import xsched.tests._1.SimpleTriangle;

public class TestAnalyze_1 {

	@Test
	public void testAnalyze_1() {
		XSchedAnalyzer analyzer = new XSchedAnalyzer();
		analyzer.analyzeMainActivation("<" + SimpleTriangle.class.getName() + ": void main()>");
	}
}
