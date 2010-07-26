package xsched.analysis;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.ref.ReferenceCleanser;

public class ScheduleAnalysis {

	private static void extractFactsForBasicBlock(ISSABasicBlock bb) {
		for(SSAInstruction instruction : bb) {
			System.out.println("instruction: " + instruction);
		}
	}
	
	public static void main(String[] args) throws Exception {
		
		AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope("/Users/angererc/Projects/XSched/XSchedAnalysis/bin/TestClass.class", null);
		
	 // build a type hierarchy
	    System.out.print("building class hierarchy...");
	    ClassHierarchy cha = ClassHierarchy.make(scope);
	    System.out.println("done");
	    
	 // Create a name representing the method whose IR we will visualize
	      
	    IClass klass = cha.lookupClass(TypeReference.findOrCreate(ClassLoaderReference.Application, "LTestClass"));
	    
	    
	 // register class hierarchy and AnalysisCache with the reference cleanser, so that their soft references are appropriately wiped
	    
	    AnalysisCache cache = new AnalysisCache();
	    AnalysisOptions options = new AnalysisOptions();
	    
	    ReferenceCleanser.registerClassHierarchy(cha);
	    ReferenceCleanser.registerCache(cache);

	    IMethod method = klass.getMethod(Selector.make("foo([Ljava/lang/String;)V"));
	    
	    // construct an IR; it will be cached
	    IR ssa = cache.getSSACache().findOrCreateIR(method, Everywhere.EVERYWHERE, options.getSSAOptions());
	    
	    for(ISSABasicBlock bb : ssa.getControlFlowGraph()) {
	    	extractFactsForBasicBlock(bb);
	    }
	    	    
	    System.out.println("done");
	}
}
