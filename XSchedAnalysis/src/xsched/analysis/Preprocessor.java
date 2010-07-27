package xsched.analysis;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import xsched.analysis.db.ComputeDomainsVisitor;
import xsched.analysis.db.ComputeRelationsVisitor;
import xsched.analysis.db.ExtensionalDatabase;
import xsched.analysis.db.Variable;

import com.ibm.wala.cfg.ControlFlowGraph;
import com.ibm.wala.cfg.cdg.ControlDependenceGraph;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSAArrayLengthInstruction;
import com.ibm.wala.ssa.SSAArrayLoadInstruction;
import com.ibm.wala.ssa.SSAArrayStoreInstruction;
import com.ibm.wala.ssa.SSABinaryOpInstruction;
import com.ibm.wala.ssa.SSACheckCastInstruction;
import com.ibm.wala.ssa.SSAComparisonInstruction;
import com.ibm.wala.ssa.SSAConditionalBranchInstruction;
import com.ibm.wala.ssa.SSAConversionInstruction;
import com.ibm.wala.ssa.SSAGetCaughtExceptionInstruction;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAGotoInstruction;
import com.ibm.wala.ssa.SSAInstanceofInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSALoadMetadataInstruction;
import com.ibm.wala.ssa.SSAMonitorInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.ssa.SSAPhiInstruction;
import com.ibm.wala.ssa.SSAPiInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.ssa.SSAReturnInstruction;
import com.ibm.wala.ssa.SSASwitchInstruction;
import com.ibm.wala.ssa.SSAThrowInstruction;
import com.ibm.wala.ssa.SSAUnaryOpInstruction;
import com.ibm.wala.ssa.SSAInstruction.Visitor;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.ref.ReferenceCleanser;

public class Preprocessor {
	AnalysisOptions options = new AnalysisOptions();
	AnalysisCache cache = new AnalysisCache();
	ExtensionalDatabase database = new ExtensionalDatabase();
	
	public Preprocessor() {
		
	}
	
	private void processMethod(IMethod method) {

	    // construct an IR; it will be cached
	    IR ssa = cache.getSSACache().findOrCreateIR(method, Everywhere.EVERYWHERE, options.getSSAOptions());
	    
	    //we "fake" and additional visit method to handle stuff that is done for every instruction
	    ComputeDomainsVisitor computeDomains = new ComputeDomainsVisitor(database, method.getReference());
	    ssa.visitAllInstructions(computeDomains);
	    
	    database.domainsAreComplete();
	    
	    ComputeRelationsVisitor computeRelations= new ComputeRelationsVisitor(database, computeDomains.getVariables());
	    ssa.visitAllInstructions(computeRelations);
	    
	    ControlFlowGraph cfg = ssa.getControlFlowGraph();
	    ControlDependenceGraph cdg = new ControlDependenceGraph(cfg);
	    System.out.println(ssa);
	    System.out.println(cdg);
	}
	
	public void preprocess(String scopeString, File exclude) throws IOException, ClassHierarchyException {
		AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(scopeString, exclude);
		
		 // build a type hierarchy
	    System.out.print("building class hierarchy...");
	    ClassHierarchy cha = ClassHierarchy.make(scope);
	    System.out.println("done");
		    
	    ReferenceCleanser.registerClassHierarchy(cha);
	    ReferenceCleanser.registerCache(cache);
		 // Create a name representing the method whose IR we will visualize
	    IClass klass = cha.lookupClass(TypeReference.findOrCreate(ClassLoaderReference.Application, "LTestClass"));

	    IMethod method = klass.getMethod(Selector.make("foo([Ljava/lang/String;)V"));
	    processMethod(method);
	    	    
	    System.out.println("done");
	}
	
}
