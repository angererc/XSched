package xsched.analysis.wala;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import xsched.analysis.core.AnalysisResult;
import xsched.analysis.core.AnalysisSession;
import xsched.analysis.core.AnalysisTask;
import xsched.analysis.core.AnalysisTaskResolver;
import xsched.analysis.core.ParallelTasksResult;
import xsched.analysis.core.TaskSchedule;
import xsched.analysis.wala.schedule_extraction.NormalNodeFlowData;
import xsched.analysis.wala.schedule_extraction.TaskScheduleSolver;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.ref.ReferenceCleanser;

public class WalaScheduleAnalysisDriver {

	//private static boolean DEBUG = true;

	private static final ClassLoader MY_CLASSLOADER = WalaScheduleAnalysisDriver.class.getClassLoader();
	
	private final AnalysisProperties properties;
	
	private AnalysisCache cache;
	private AnalysisScope scope;
	private ClassHierarchy classHierarchy;
	private Iterable<Entrypoint> entrypoints;
	private AnalysisOptions options;
	private CallGraph cg;
	private HashSet<IMethod> taskMethods;
	//all main tasks; those are included in the taskMethods, too!
	private HashSet<IMethod> mainTaskMethods;
	private AnalysisSession<CGNode, Integer, WalaTaskScheduleManager> analysis;
	
	public WalaScheduleAnalysisDriver(AnalysisProperties properties) {
		this.properties = properties;	
	}
	
	public Set<IMethod> taskMethods() {
		return taskMethods;
	}
	
	public Set<IMethod> mainTaskMethods() {
		return mainTaskMethods;
	}
	
	public void _1_setUp() throws IOException, ClassHierarchyException {
		assert cache == null;
		cache = new AnalysisCache();
		ReferenceCleanser.registerCache(cache);
		//
		scope = AnalysisScopeReader.readJavaScope(properties.standardScopeFile, properties.openExclusionsFile(), MY_CLASSLOADER);
		for(String s : properties.applicationFiles) {
			AnalysisScope appScope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(s, properties.openExclusionsFile());
			scope.addToScope(appScope);
		}
		//
		classHierarchy = ClassHierarchy.make(scope);
		ReferenceCleanser.registerClassHierarchy(classHierarchy);
		//
		this.entrypoints = new AllApplicationEntrypoints(scope, classHierarchy);
		this.options = new AnalysisOptions(scope, entrypoints);
		
		//
		analysis = new AnalysisSession<CGNode, Integer, WalaTaskScheduleManager>();
	}

	public void _2_findTaskMethods() {
		this.taskMethods = new HashSet<IMethod>();
		this.mainTaskMethods = new HashSet<IMethod>();
		Iterator<IClass> classes = classHierarchy.iterator();
		while(classes.hasNext()) {
			IClass clazz = classes.next();
			//we don't have to look in the standard library because they don't have any task methods
			if( ! clazz.getClassLoader().getReference().equals(ClassLoaderReference.Primordial)) {
				for(IMethod method : clazz.getDeclaredMethods()) {
					if(WalaConstants.isTaskMethod(method.getReference())) {
						taskMethods.add(method);
						if(WalaConstants.isMainTaskMethod(method.getReference())) {
							mainTaskMethods.add(method);
						}
					}
				}
			}
		}
	}
	
	public void _3_makeCallGraph() throws IllegalArgumentException, CallGraphBuilderCancelException {
		cg = properties.createCallGraphBuilder(options, cache, scope, classHierarchy).makeCallGraph(options, null);
	}
	
	public NormalNodeFlowData _n_computeNodeFlowData(IR ir) {
		NormalNodeFlowData flowData = TaskScheduleSolver.solve(ir);
		return flowData;
	}
	
	//this method will call computeNodeFlowData() so you con't have to do that if you don't need the flow data
	public TaskSchedule<Integer, WalaTaskScheduleManager> _n_computeTaskSchedule(IR ir) {
		NormalNodeFlowData flowData = _n_computeNodeFlowData(ir);
		WalaTaskScheduleManager manager = WalaTaskScheduleManager.make(cache.getSSACache(), ir, flowData);
		TaskSchedule<Integer, WalaTaskScheduleManager> taskSchedule = flowData.makeTaskSchedule(manager);
		return taskSchedule;
	}
	
	public AnalysisTaskResolver<CGNode, Integer, WalaTaskScheduleManager> createResolver() {
		return new AnalysisTaskResolver<CGNode, Integer,WalaTaskScheduleManager>() {
			@Override
			public Collection<CGNode> possibleTargetTasksForSite(
					AnalysisTask<CGNode, Integer, WalaTaskScheduleManager> task,
					Integer scheduleNode) {
				WalaTaskScheduleManager manager = task.taskSchedule().taskScheduleManager();
				SSAInvokeInstruction invoke = manager.scheduleSiteForNode(scheduleNode);
				return cg.getNodes(invoke.getDeclaredTarget());					
			}
		};
	}
	
	public IR irForMethod(IMethod method) {
		return cache.getIR(method);
	}
	
	public void _4_createAnalysisTasks() {
		for(IMethod taskMethod : taskMethods) {
			TaskSchedule<Integer, WalaTaskScheduleManager> taskSchedule = _n_computeTaskSchedule(irForMethod(taskMethod));
			
			for(CGNode node : cg.getNodes(taskMethod.getReference())) {
				analysis.createTask(node, taskSchedule);
			}	
		}
	}
	
	public ParallelTasksResult<CGNode, Integer, WalaTaskScheduleManager> _5_runAnalysisOnMainTaskMethods() {
		AnalysisTaskResolver<CGNode, Integer, WalaTaskScheduleManager> resolver = createResolver();
		
		ParallelTasksResult<CGNode, Integer, WalaTaskScheduleManager> result = new ParallelTasksResult<CGNode, Integer, WalaTaskScheduleManager>();
		//now solve the analysis for each main task
		for(IMethod mainTaskMethod : mainTaskMethods) {
			for(CGNode node : cg.getNodes(mainTaskMethod.getReference())) {
				AnalysisTask<CGNode, Integer, WalaTaskScheduleManager> task = analysis.taskForID(node);
				
				AnalysisResult<CGNode, Integer, WalaTaskScheduleManager> analysisResult = task.solveAsRoot(resolver);
				assert analysisResult.formalParameterResult.numTaskParameters() == 0;
				result.mergeWith(analysisResult.parallelTasksResult);
			}
		}
		return result;
	}
		
	public ParallelTasksResult<CGNode, Integer, WalaTaskScheduleManager> runScheduleAnalysis() throws IOException, ClassHierarchyException, IllegalArgumentException, CallGraphBuilderCancelException {
		
		this._1_setUp();
		this._2_findTaskMethods();
		this._3_makeCallGraph();
		this._4_createAnalysisTasks();
		return this._5_runAnalysisOnMainTaskMethods();
		
		//XXX note: in FakeRootClass, Wala imitates schedules of all task methods, it seems; is that a problem?	
	}

}
