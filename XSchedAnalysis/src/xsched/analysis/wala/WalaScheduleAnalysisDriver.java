package xsched.analysis.wala;

import java.io.IOException;
import java.util.HashSet;
import xsched.analysis.core.ScheduleAnalysis;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.callgraph.impl.DefaultContextSelector;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.cfa.nCFAContextSelector;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.shrikeBT.analysis.Analyzer.FailureException;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.ref.ReferenceCleanser;

public class WalaScheduleAnalysisDriver {

	private static boolean DEBUG = true;

	private static final ClassLoader MY_CLASSLOADER = WalaScheduleAnalysisDriver.class.getClassLoader();
	
	

	public void runScheduleAnalysis(AnalysisProperties properties) throws IOException, ClassHierarchyException, IllegalArgumentException, CallGraphBuilderCancelException, IllegalStateException, InvalidClassFileException, FailureException {

		AnalysisCache cache = new AnalysisCache();
		AnalysisScope scope = AnalysisScopeReader.readJavaScope(properties.standardScopeFile, properties.openExclusionsFile(), MY_CLASSLOADER);
		for(String s : properties.applicationFiles) {
			AnalysisScope appScope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(s, properties.openExclusionsFile());
			scope.addToScope(appScope);
		}
		
		ClassHierarchy classHierarchy = ClassHierarchy.make(scope);

		ReferenceCleanser.registerClassHierarchy(classHierarchy);
		ReferenceCleanser.registerCache(cache);

		Iterable<Entrypoint> entrypoints = new AllApplicationEntrypoints(scope, classHierarchy);
		AnalysisOptions options = new AnalysisOptions(scope, entrypoints);

		ContextSelector def = new DefaultContextSelector(options);
	    ContextSelector nCFAContextSelector = new nCFAContextSelector(1, def);
	    
	    TaskStringContextSelector customSelector = new TaskStringContextSelector(nCFAContextSelector);

		PropagationCallGraphBuilder cgBuilder = Util.makeZeroCFABuilder(options, cache, classHierarchy, scope, customSelector, null);
		
		//create call graph and perform points-to analysis
		CallGraph cg = cgBuilder.makeCallGraph(options, null);

		HashSet<CGNode> taskMethodNodes = new HashSet<CGNode>();
		for(CGNode node : cg) {
			if(ScheduleInference.isTaskMethod(node.getMethod())) {
				taskMethodNodes.add(node);
			}
		}
		ScheduleAnalysis<CGNode, WalaScheduleSite> analysis = ScheduleInference.populateScheduleAnalysis(cg, taskMethodNodes);		
		
		//do it like this:
		//use a TaskForrestCallGraph to cut the call graph into disjoint forests with the task methods at their root
		//then use the loop finder to find loops in the call graph (which are then only loops within a task, excluding recursive task activation)
		//then for each task collect all reachable nodes which are then all methods that this task may directly or indirectly execute
		//and then i don't know....
		
		//XXX missing:
		//check what happens with schedule sites outside a task method
		//check what arrows I can rely on
		
		//System.out.println(cg);
	}

}
