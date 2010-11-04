package xsched.analysis.test_benchmarks;

import xsched.analysis.db.Cheater;
import xsched.analysis.db.ExtensionalDatabase;

import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;

public class TestCheater extends Cheater {
	@Override
	public String exclusionsFile() {
		return  "xsched/analysis/test_benchmarks/Exclusions.txt";
	}
	
	@Override
	public void cheatBeforeDomainComputation() {
		
	}
}
