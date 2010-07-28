package xsched.analysis.db;

import java.util.HashMap;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.ssa.SSAReturnInstruction;
import com.ibm.wala.types.FieldReference;
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

	private final ExtensionalDatabase database;
	private final ClassHierarchy classHierarchy;

	private HashMap<Integer, Variable> variables;
	private IMethod method;
	
	HandleRelationsLogic(FillExtensionalDatabase parent) {
		this.database = parent.database;
		this.classHierarchy = parent.classHierarchy;
	}
	
	private void addToVariableType(Variable variable, TypeReference type) {		
		database.variableType.add(variable, type.getName());
	}

	void addToAssignableRel(IClass klass) {
		//**************
		// Assignable relation
		IClass superKlass = klass;
		while(superKlass != null) {
			database.assignable.add(superKlass.getReference().getName(), klass.getReference().getName());
			superKlass = superKlass.getSuperclass();
		}
	}
		
	void addToStoreRel(SSAPutInstruction instruction) {
		//base.field = source
		Variable lhs = variable(instruction.getRef());
		FieldReference field = instruction.getDeclaredField();
		Variable rhs = variable(instruction.getVal());;		
		database.store.add(instruction, lhs, field, rhs);		
	}
	
	void addToLoadRel(SSAGetInstruction instruction) {
		Variable lhs = variable(instruction.getDef());
		Variable rhs = variable(instruction.getRef());
		FieldReference field = instruction.getDeclaredField();
		//***************
		// load relation
		database.load.add(instruction, lhs, field, rhs);
		addToVariableType(lhs, instruction.getDeclaredFieldType());
	}
	
	void addToNewStatementRel(SSANewInstruction instruction) {
		NewSiteReference object = instruction.getNewSite();
		Variable lhs = variable(instruction.getDef());
		database.newStatement.add(lhs, object);
		
		TypeReference objectType = object.getDeclaredType();
		database.objectType.add(object, object.getDeclaredType().getName());
		
		addToVariableType(lhs, objectType);
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
	
	void addToFormalRel(IR ir) {
		//TODO we only handle formals for java methods here, not formals for native methods!
		int[] params = ir.getParameterValueNumbers();
		for(int i = 0; i < params.length; i++) {
			Variable param = variable(params[i]);
			database.formals.add(ir.getMethod(), i, param);
			addToVariableType(param, ir.getParameterType(i));			
		}		
	}
	
	void addToMethodReturnRel(SSAReturnInstruction instruction) {
		if(! instruction.returnsVoid() && ! instruction.returnsPrimitiveType()) {		
			Variable result = variable(instruction.getResult());
			database.methodReturns.add(method, result);
		}
	}

	void addToActualsRel(SSAInvokeInstruction instruction) {
		for(int i = 0; i < instruction.getNumberOfParameters(); i++) {
			Variable param = variable(instruction.getUse(i));
			database.actuals.add(instruction, i, param);
		}
	}
	
	void addToCallSiteReturnsRel(SSAInvokeInstruction instruction) {		
		Variable exception = variable(instruction.getException()); 
		database.callSiteReturns.add(instruction, exception);
		if(instruction.getExceptionTypes().size() == 1) {
			addToVariableType(exception, instruction.getExceptionTypes().iterator().next());
		} else {
			addToVariableType(exception, TypeReference.JavaLangError);
		}
		
		for(int i = 0; i < instruction.getNumberOfReturnValues(); i++) {
			Variable ret = variable(instruction.getReturnValue(i));
			database.callSiteReturns.add(instruction, ret);
			addToVariableType(ret, instruction.getDeclaredResultType());
		}
	}
	
	void addToInvokeRel(SSAInvokeInstruction instruction) {
		if(instruction.getCallSite().isFixed()) {
			IMethod method = classHierarchy.resolveMethod(instruction.getDeclaredTarget());
			database.staticInvokes.add(instruction, method);
		} else {
			database.methodInvokes.add(instruction, instruction.getDeclaredTarget().getSelector());
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
	
	void beginMethod(IMethod method) {
		variables = new HashMap<Integer, Variable>();
		this.method = method;
	}
}
