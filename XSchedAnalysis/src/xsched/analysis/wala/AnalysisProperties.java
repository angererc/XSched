package xsched.analysis.wala;

import java.io.File;
import java.io.IOException;

import com.ibm.wala.util.io.FileProvider;

public class AnalysisProperties {
	final String[] applicationFiles;
	final String standardScopeFile = "xsched/analysis/wala/StandardScope.txt";
	final String exclusionsFile;
		
	public AnalysisProperties(String exclusionsFile, String... applicationFiles) {
		this.applicationFiles = applicationFiles;
		this.exclusionsFile = exclusionsFile;
	}
	
	File openExclusionsFile() {		
		if(exclusionsFile == null)
			return null;

		File exclude;
		try {
			exclude = FileProvider.getFile(exclusionsFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return exclude;
	}
	
}
