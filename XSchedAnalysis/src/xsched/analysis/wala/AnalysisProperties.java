package xsched.analysis.wala;

import java.io.File;
import java.io.IOException;

import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.impl.DefaultContextSelector;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.cfa.nCFAContextSelector;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.util.io.FileProvider;

public class AnalysisProperties {
	public final String[] applicationFiles;
	public final String standardScopeFile = "xsched/analysis/wala/StandardScope.txt";
	public final String exclusionsFile;
		
	public AnalysisProperties(String exclusionsFile, String... applicationFiles) {
		this.applicationFiles = applicationFiles;
		this.exclusionsFile = exclusionsFile;
	}
	
	public PropagationCallGraphBuilder createCallGraphBuilder(AnalysisOptions options, AnalysisCache cache, AnalysisScope scope, ClassHierarchy classHierarchy) {
		ContextSelector def = new DefaultContextSelector(options);
	    ContextSelector nCFAContextSelector = new nCFAContextSelector(1, def);
	    
	    TaskStringContextSelector customSelector = new TaskStringContextSelector(nCFAContextSelector);

		return Util.makeZeroCFABuilder(options, cache, classHierarchy, scope, customSelector, null);
	}
	
	public File openExclusionsFile() {		
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
