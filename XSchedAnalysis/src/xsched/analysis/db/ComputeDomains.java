package xsched.analysis.db;

import java.util.Iterator;

import xsched.analysis.utils.DefUseUtils;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.ssa.SSAPhiInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;

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
		
		parent.cheater.cheatBeforeDomainComputation();
		
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
	
	private void addDefaultElements() {
		database.fields.add(database.arrayElementField);
		database.objects.add(database.theImmutableStringObject);
	}
	
	private void processClass(IClass klass) {
		
		//********
		// Types domain
		database.types.add(klass.getReference().getName());
		
		//********
		// Fields domain
		for(IField field : klass.getDeclaredInstanceFields()) {
			//we keep even primitive fields because we might use them in datalog files for datarace checks etc
			database.fields.add(field.getReference());
		}
		for(IField field : klass.getDeclaredStaticFields()) {
			//we keep even primitive fields because we might use them in datalog files for datarace checks etc
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

	private boolean includeBytecode(SSAInstruction inst) {
		return 
			(inst instanceof SSAGetInstruction) || 
			(inst instanceof SSAPutInstruction) || 
			(inst instanceof SSAAbstractInvokeInstruction) ||
			(inst instanceof SSAPhiInstruction);
	}
	
	private void processCallableMethod(IMethod method) {
		//************
		// Methods domain
		database.methods.add(method);
		
		if(method.isNative()) {
			return;
		}
		
		//Method has a body
		IR ir = cache.getSSACache().findOrCreateIR(method, Everywhere.EVERYWHERE, options.getSSAOptions());
		
		DefUse defUse = cache.getSSACache().findOrCreateDU(ir, Everywhere.EVERYWHERE);
		
		for(int variable = 1; variable <= ir.getSymbolTable().getMaxValueNumber(); variable++) {
			//************
			//Variables domain
			if(DefUseUtils.definesReferenceType(ir, defUse, variable)) {
				database.variables.add(new Variable(method, variable));
			}
		}
		
		//domains extracted from the body
		Iterator<SSAInstruction> it = ir.iterateAllInstructions();
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
				database.objects.add((SSANewInstruction)instruction);
			}
			
			//make sure we saw all the selectors
			//shouldn't matter if we con't cheat and ignore classes, but if we do then we have to do that
			if(instruction instanceof SSAInvokeInstruction) {
				//***********
				//selectors domain
				database.selectors.add(((SSAInvokeInstruction)instruction).getDeclaredTarget().getSelector());
			}
		}

	}
	
}