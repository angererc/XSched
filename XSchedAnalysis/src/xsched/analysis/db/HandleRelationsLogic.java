package xsched.analysis.db;

import java.util.HashMap;
import java.util.Set;

import xsched.analysis.utils.DefUseUtils;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAArrayLoadInstruction;
import com.ibm.wala.ssa.SSAArrayStoreInstruction;
import com.ibm.wala.ssa.SSACheckCastInstruction;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSALoadMetadataInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.ssa.SSAPhiInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.ssa.SSAReturnInstruction;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;

/**
 * instead of making subclasses for each relation that contains the code to fill it
 * we put the code in here. Since our system is not really intended to be super extensible
 * I think it is easier to have all this logic combined in this one file as opposed to introducing
 * 20 new files with 4 lines handling-code.
 * 
 * If the system should become more flexible later, we can move the static methods from in here into
 * specialized classes for each relation
 *
 */
class HandleRelationsLogic {

	private final AnalysisCache cache;
	private final AnalysisOptions options;
	final ExtensionalDatabase database;
	private final ClassHierarchy classHierarchy;
	
	private HashMap<Integer, Variable> variables;
	private IMethod method;
	private IR ir;
	private DefUse defUse;
	
	HandleRelationsLogic(FillExtensionalDatabase parent) {
		this.database = parent.database;
		this.classHierarchy = parent.classHierarchy;
		this.cache = parent.cache;
		this.options = parent.options;
	}
	
	private void addToVariableType(Variable variable) {
		Set<TypeReference> types = DefUseUtils.definedReferenceTypes(ir, defUse, variable.ssaID);
		for(TypeReference type : types) {
			database.variableType.add(variable, type.getName());
		}
	}

	void addToAssignableRel(IClass klass) {
		//**************
		// Assignable relation
		IClass superKlass = klass;
		while(superKlass != null) {
			database.assignable.add(superKlass.getReference().getName(), klass.getReference().getName());
			superKlass = superKlass.getSuperclass();
		}
		
		for(IClass interf : klass.getAllImplementedInterfaces()) {
			database.assignable.add(interf.getReference().getName(), klass.getReference().getName());
		}
	}
	
	void addToAssignsRel(SSACheckCastInstruction instruction) {
		if(DefUseUtils.definesReferenceType(ir, defUse, instruction)) {
			Variable lhs = variable(instruction.getResult());
			Variable rhs = variable(instruction.getVal());
			database.assigns0.add(lhs, rhs);
			addToVariableType(lhs);
		}
	}
	
	void addToAssignsRel(SSAPhiInstruction instruction) {
		if(DefUseUtils.definesReferenceType(ir, defUse, instruction)) {
			Variable lhs = variable(instruction.getDef());
			for(int i = 0; i < instruction.getNumberOfUses(); i++) {
				Variable rhs = variable(instruction.getUse(i));
				database.assigns0.add(lhs, rhs);
			}
			
			addToVariableType(lhs);
		}
	}
	
	void addToStoreRel(SSAArrayStoreInstruction instruction) {
		//base.field = source		
		Variable lhs = variable(instruction.getArrayRef());
		FieldReference field = database.arrayElementField;
		
		if(instruction.getElementType().isReferenceType()) {
			Variable rhs = variable(instruction.getValue());;
			database.store.add(instruction, lhs, field, rhs);
		} else {
			database.primStore.add(instruction, lhs, field);
		}
				
	}
	
	void addToStoreRel(SSAPutInstruction instruction) {
		//base.field = source
		Variable lhs;
		
		if(instruction.isStatic()) {
			lhs = database.theGlobalObjectRef;
		} else {
			lhs = variable(instruction.getRef());
		}
		
		FieldReference field = instruction.getDeclaredField();
		if(field.getFieldType().isReferenceType()) {
			Variable rhs = variable(instruction.getVal());;			
			database.store.add(instruction, lhs, field, rhs);
		} else {
			database.primStore.add(instruction, lhs, field);
		}
	}
	
	void addToLoadRel(SSAArrayLoadInstruction instruction) {
		Variable lhs = variable(instruction.getDef());
		Variable rhs = variable(instruction.getArrayRef());
		FieldReference field = database.arrayElementField;
		
		if(DefUseUtils.definesReferenceType(ir, defUse, lhs.ssaID)) {
			//***************
			// load relation
			database.load.add(instruction, rhs, field, lhs);
			addToVariableType(lhs);
		} else {
			database.primLoad.add(instruction, rhs, field);
		}
	}
	
	void addToLoadRel(SSAGetInstruction instruction) {
		Variable lhs = variable(instruction.getDef());
		FieldReference field = instruction.getDeclaredField();
		Variable rhs;
			if(instruction.isStatic()) {
				rhs = database.theGlobalObjectRef;
			} else {
				rhs = variable(instruction.getRef());
			}
		if(DefUseUtils.definesReferenceType(ir, defUse, lhs.ssaID)) {					
			//***************
			// load relation
			database.load.add(instruction, rhs, field, lhs);
			addToVariableType(lhs);
		} else {
			database.primLoad.add(instruction, rhs, field);
		}
	}
		
	void addToAssignObjectRel(SSANewInstruction instruction) {		
		Variable lhs = variable(instruction.getDef());
		ObjectCreationSite creationSite = new ObjectCreationSite.SSANewInstructionCreationSite(instruction);
		database.assignObject.add(lhs, creationSite);		
		database.objectType.add(creationSite, instruction.getNewSite().getDeclaredType().getName());
		
		addToVariableType(lhs);
	}
	
	void addToAssignObjectRel(int variable, String constant) {		
		Variable lhs = variable(variable);		
		database.assignObject.add(lhs, database.theImmutableStringObject);		
		
		addToVariableType(lhs);
	}
	
	public void addToAssignObjectRel(SSALoadMetadataInstruction instruction) {
		Variable lhs = variable(instruction.getDef());
		Object token = instruction.getToken();
		Object value;
		if(token instanceof TypeReference) {
			value = ((TypeReference)token).getName();
		} else {
			throw new RuntimeException("unknown metadata token: " + token);
		}
		ObjectCreationSite creationSite = new ObjectCreationSite.SpecialCreationSite(value);
		database.assignObject.add(lhs, creationSite);
		database.objectType.add(creationSite, instruction.getType().getName());
		addToVariableType(lhs);
	}
	
	void addToMethodImplementationRel(IClass klass) {
		TypeReference type = klass.getReference();		
		for(IMethod method : klass.getAllMethods()) {
			Selector selector = method.getSelector();			
			if( ! method.isAbstract()) {
				database.methodImplementation.add(type.getName(), selector, method);
			}
		}
	}
	
	void addToFormalRel() {
		//TODO we only handle formals for java methods here, not formals for native methods!
		int[] params = ir.getParameterValueNumbers();
		for(int i = 0; i < params.length; i++) {
			if(ir.getParameterType(i).isReferenceType()) {
				Variable param = variable(params[i]);
				database.formals.add(ir.getMethod(), i, param);
				addToVariableType(param);
			}
		}		
	}
	
	void addToMethodReturnRel(SSAReturnInstruction instruction) {
		if(! instruction.returnsVoid() && ! instruction.returnsPrimitiveType()) {		
			Variable result = variable(instruction.getResult());
			database.methodReturns.add(method, result);
		}
	}

	void addToActualsRel(SSAInvokeInstruction instruction) {
		final MethodReference target = instruction.getCallSite().getDeclaredTarget();
		
		if(instruction.getCallSite().isStatic()) {
			for(int i = 0; i < instruction.getNumberOfParameters(); i++) {
				if(target.getParameterType(i).isReferenceType()) {
					Variable param = variable(instruction.getUse(i));
					database.actuals.add(instruction, i, param);
				}
			}
		} else {
			//a virtual call, so 0 is the this pointer
			Variable param = variable(instruction.getUse(0));
			database.actuals.add(instruction, 0, param);
			for(int i = 1; i < instruction.getNumberOfParameters(); i++) {
				if(target.getParameterType(i-1).isReferenceType()) { //the method.getParameterType() doesn't contain "this"
					param = variable(instruction.getUse(i));
					database.actuals.add(instruction, i, param);
				}
			}
		}		
	}
	
	void addToCallSiteReturnsRel(SSAInvokeInstruction instruction) {		
		Variable exception = variable(instruction.getException()); 
		database.callSiteReturns.add(instruction, exception);
		addToVariableType(exception);
		
		for(int i = 0; i < instruction.getNumberOfReturnValues(); i++) {
			if(instruction.getDeclaredResultType().isReferenceType()) {
				Variable ret = variable(instruction.getReturnValue(i));
				database.callSiteReturns.add(instruction, ret);
				addToVariableType(ret);
			}
		}
	}
	
	void addToInvokeRel(SSAInvokeInstruction instruction) {
		if(instruction.getCallSite().isFixed()) {
			IMethod method = classHierarchy.resolveMethod(instruction.getDeclaredTarget());
			if(method == null) {
				System.err.println("Warning: didn't find method " + instruction.getDeclaredTarget() + ". Probably the Cheater ignores the receiver class. Ignoring invoke statement!");
			} else {
				database.staticInvokes.add(instruction, method);
			}
		} else {
			Selector sel = instruction.getDeclaredTarget().getSelector();
			database.methodInvokes.add(instruction, sel);			
		}
	}
	
	/* *******************
	 * helpers that register the values with the domains
	 * always wrap values in such methods
	 * 
	 * *******************/
	private Variable variable(int idx) {
		Variable v = variables.get(idx);
		if(v == null) {
			v = new Variable(method, idx);
			variables.put(idx, v);
		}
		return v;
	}	
	
	void beginMethod(IMethod method, IR ssa) {
		ir = ssa;
		defUse = cache.getSSACache().findOrCreateDU(method, Everywhere.EVERYWHERE, options.getSSAOptions());
		variables = new HashMap<Integer, Variable>();
		this.method = method;
	}
	
}
