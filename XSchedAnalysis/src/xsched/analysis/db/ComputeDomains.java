package xsched.analysis.db;

import java.util.Iterator;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;

class ComputeDomains {
	private final ExtensionalDatabase database;
	private final AnalysisCache cache;
	private final AnalysisOptions options;
	
	private int maxNumberOfParameters = 0;
	
	ComputeDomains(FillExtensionalDatabase parent) {
		this.database = parent.database;
		this.cache = parent.cache;
		this.options = parent.options;
		
		//add some default stuff to the domains
		addDefaultElements();
		
		addCheatingElements();
		
		//work on all the classes in the hierarchy
		for(IClass klass : parent.classHierarchy) {
			processClass(klass);
		}
		
		//***********
		// ParamPosition domain
		for(int i = 0; i < maxNumberOfParameters; i++) {
			database.paramPositions.add(i);
		}
	}
	
	private boolean includeBytecode(SSAInstruction inst) {
		return (inst instanceof SSAGetInstruction) || (inst instanceof SSAPutInstruction) || (inst instanceof SSAAbstractInvokeInstruction);
	}
	
	private void addCheatingElements() {
		System.err.println("!!!! Cheating while computing domains!!!");
		database.types.add(TypeReference.JavaLangStringBuilder.getName());
		database.types.add(TypeReference.JavaLangNullPointerException.getName());
		database.types.add(TypeReference.JavaLangString.getName());
		database.types.add(TypeReference.JavaLangClass.getName());
		database.types.add(TypeReference.JavaLangError.getName());
		database.types.add(TypeReference.JavaUtilIterator.getName());
		database.types.add(TypeReference.JavaUtilVector.getName());
		database.types.add(TypeName.findOrCreate("Z"));
		database.types.add(TypeName.findOrCreate("I"));
		database.types.add(TypeName.findOrCreate("J"));
		database.types.add(TypeReference.findOrCreateArrayOf(TypeReference.JavaLangString).getName());
		database.types.add(TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Ljava/lang/IllegalArgumentException").getName());
		//not sure if that's cheating or if it's correct to keep the Activation class out of this
		database.types.add(TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Lxsched/Activation").getName());
		
		//make sure we have at least that many params
		for(int i = 0; i < 5; i++) {
			database.paramPositions.add(i);
		}
	}
	
	private void addDefaultElements() {
		database.fields.add(database.arrayElementField);		
	}
	
	private void processClass(IClass klass) {
		
		//********
		// Types domain
		database.types.add(klass.getReference().getName());
		
		//********
		// Fields domain
		for(IField field : klass.getDeclaredInstanceFields()) {
			database.fields.add(field.getReference());
		}
		for(IField field : klass.getDeclaredStaticFields()) {
			database.fields.add(field.getReference());
		}
		
		// Methods and Method References
		for(IMethod method : klass.getDeclaredMethods()) {
			//for ParamPosition domain
			if(maxNumberOfParameters < method.getNumberOfParameters()) {
				maxNumberOfParameters = method.getNumberOfParameters();
			}
			
			//*******
			// MethodReference domain
			database.selectors.add(method.getSelector());
			
			//does method have a body?
			if(! method.isAbstract()) {
				processCallableMethod(method);
			}
		}
		
	}

	private void processCallableMethod(IMethod method) {
		//************
		// Methods domain
		database.methods.add(method);
		
		if(method.isNative()) {
			return;
		}
		
		//Method has a body
		IR ssa = cache.getSSACache().findOrCreateIR(method, Everywhere.EVERYWHERE, options.getSSAOptions());
		
		for(int variable = 0; variable <= ssa.getSymbolTable().getMaxValueNumber(); variable++) {
			//************
			//Variables domain
			database.variables.add(new Variable(method, variable));
		}
		
		//domains extracted from the body
		Iterator<SSAInstruction> it = ssa.iterateAllInstructions();
		while(it.hasNext()) {
			SSAInstruction instruction = it.next();
			
			//************
			//Bytecode domain
			if(includeBytecode(instruction)) {
				database.bytecodes.add(instruction);
			}
			
			if(instruction instanceof SSANewInstruction) {
				//***********
				//Objects domain
				database.objects.add(((SSANewInstruction)instruction).getNewSite());
			}
		}
/*
	    // construct an IR; it will be cached
	    IR ssa = cache.getSSACache().findOrCreateIR(method, Everywhere.EVERYWHERE, options.getSSAOptions());
	    
	    //we "fake" and additional visit method to handle stuff that is done for every instruction
	    ComputeDomains computeDomains = new ComputeDomains(database, method.getReference());
	    ssa.visitAllInstructions(computeDomains);
	    
	    database.domainsAreComplete();
	    
	    ComputeRelations computeRelations= new ComputeRelations(database, computeDomains.getVariables());
	    ssa.visitAllInstructions(computeRelations);
	    
	    ControlFlowGraph cfg = ssa.getControlFlowGraph();
	    ControlDependenceGraph cdg = new ControlDependenceGraph(cfg);
	    System.out.println(ssa);
	    System.out.println(cdg);
	 */
	}
	
}