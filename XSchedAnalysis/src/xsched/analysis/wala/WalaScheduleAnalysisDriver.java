package xsched.analysis.wala;

import java.io.IOException;
import java.util.HashSet;
import xsched.analysis.core.AnalysisSchedule;
import xsched.analysis.wala.schedule_extraction.TaskScheduleSolver;

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
		
		for(CGNode node : taskMethodNodes) {
			TaskScheduleSolver.solve(node.getIR().getControlFlowGraph());
			
		}
		
		AnalysisSchedule<CGNode, WalaScheduleSite> analysis = ScheduleInference.populateScheduleAnalysis(cg, taskMethodNodes);		
		
		//do it like this:
		//use a TaskForrestCallGraph to cut the call graph into disjoint forests with the task methods at their root
		//then use the loop finder to find loops in the call graph (which are then only loops within a task, excluding recursive task activation)
		//then for each task collect all reachable nodes which are then all methods that this task may directly or indirectly execute
		//and then i don't know...
		
		/*
		 * or think about that:
		 * for an arrow, we require that at least one side is a local schedule statement (coupled)
		 * the other side can be a) local, b) a task parameter c) a method parameter d) a phi node or e) something else
		 * for e) we just ignore the hb edge
		 * for a) we just record it (maybe upgrade a "multiple unordered" to a "multiple ordered"?)
		 * for b) we record it, too
		 * for c) we are inside a non-task method; try to find where the parameter comes from and if we find a good unambiguous one, use that
		 * for d) dunno; check whether all elements of the phi node come from direct schedule statements; then we can record a hb between all of those
		 * because they are exclusive; well, as long as we are not inside a loop....
		 */
		
		/*
		 * the mathematical property is:
		 * there is a hb edge if there is one in the code AND
		 * it is guaranteed that hb is executed for ALL lhs and ALL rhs
		 * 
		 * maybe I should try to classify each schedule site in a separate pass?
		 * a) singleton or multiple
		 * b) ssite1 exclusive with ssite2
		 * 
		 *  then if lhs reaches hb and all ssites(lhs) are exclusive I can record an edge (if there's no loop)
		 *  
		 *  if lhs reaches hb and one ssite in lhs dominates hb and one (the rest) are dominated by hb then we are in a loop and the second things are multiple-ordered
		 */
		
		/*
		 * coupled: 
		 * at least one side of the hb statement is a direct schedule statement and
		 * hb post-dominates this schedule statement
		 * => whatever comes in of the other side is guaranteed to be ordered with the schedule statement
		 * 
		 * now if other side is a direct schedule statement we can record the arrow (hb does not need to post-dominate the other statement)
		 * 
		 * maybe i should first classify all task variables as: direct schedule statement, loop variable, other
		 * 
		 *  and then "loop transitivity": an edge that connects a loop variable accross iterations
		 *  
		 *  a loop transitive hb statement is a statement where
		 *  one side s1 is a direct statement, hb post-dominates s1, and the other side s2 is a phi that contains s1
		 */
		
		/*
		 * or maybe like this:
		 * just record all schedule sites and all hb relationships and flag
		 * schedule sites as "multiple" if inside a loop
		 * then check for each edge to or from a schedule site whether the edge is guaranteed for all cases
		 * cases: 
		 * both sides are singleton: edge post=dominates at least one side (?)
		 * etc... hm...
		 * 
		 * more like this:
		 * record all schedule sites and mark them as multiple if in loop
		 * iterate over all the edges. try to proof that an edge always orders all instances in both sides
		 * if yes, upgrade nodes to "multiple ordered" and/or add edges between them
		 * 
		 * proof obligation: if program reaches lhs AND program reaches RHS then it must reach lhs->rhs
		 * but that's not yet enough... 
		 */
		
		/*
		 * probably: instead of those simple stupid Multiplicity flags I should have a better data structure that can answer happens-before questions
		 * this data structure should support nested loops etc as I described in the onward paper
		 */
		
		//XXX missing:
		//check what happens with schedule sites outside a task method
		//check what arrows I can rely on
		
		//System.out.println(cg);
	}

}
