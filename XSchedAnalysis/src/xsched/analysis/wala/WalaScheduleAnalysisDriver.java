package xsched.analysis.wala;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.JarFile;

import xsched.analysis.core.AnalysisTask;
import xsched.analysis.core.ScheduleAnalysis;
import xsched.analysis.core.ScheduleSite;

import com.ibm.wala.analysis.reflection.ReflectionContextInterpreter;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.ShrikeCTMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.callgraph.Context;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.callgraph.impl.DefaultContextSelector;
import com.ibm.wala.ipa.callgraph.impl.DelegatingContextSelector;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.cfa.DelegatingSSAContextInterpreter;
import com.ibm.wala.ipa.callgraph.propagation.cfa.nCFAContextSelector;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.shrikeBT.analysis.Analyzer.FailureException;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.ssa.IR;
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
	    
	    ScheduleAnalysis<CGNode, CallSiteReference> analysis = new ScheduleAnalysis<CGNode, CallSiteReference>();
	    TaskStringContextSelector customSelector = new TaskStringContextSelector(nCFAContextSelector, analysis);

		PropagationCallGraphBuilder cgBuilder = Util.makeZeroCFABuilder(options, cache, classHierarchy, scope, customSelector, null);
		
		//create call graph and perform points-to analysis
		CallGraph cg = cgBuilder.makeCallGraph(options, null);

		//extract schedules from all task methods 
		for(AnalysisTask<CGNode, CallSiteReference> task : analysis.tasks()) {
			
			for(ScheduleSite<CGNode, CallSiteReference> scheduleSite : task.scheduleSites()) {
				Set<CGNode> targets = cg.getPossibleTargets(task.id, scheduleSite.id);
				for(CGNode target : targets) {
					scheduleSite.addPossibleTaskTarget(analysis.taskForID(target));
				}
			}
			
		}
		
		//missing: check what multiplicity each schedule site has
		//check what happens with schedule sites outside a task method
		//check what arrows I can rely on
		
		//System.out.println(cg);
	}

}
