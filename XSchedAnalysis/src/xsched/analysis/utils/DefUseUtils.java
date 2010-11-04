package xsched.analysis.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAArrayLoadInstruction;
import com.ibm.wala.ssa.SSACheckCastInstruction;
import com.ibm.wala.ssa.SSAGetCaughtExceptionInstruction;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSALoadMetadataInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.ssa.SSAPhiInstruction;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.types.TypeReference;

public class DefUseUtils {

	public static Set<TypeReference> definedReferenceTypes(IR ir, DefUse defUse, int variable) {
		SymbolTable symTab = ir.getSymbolTable();
		
		if(symTab.isParameter(variable)) {
			int paramPosition = parameterPosition(ir.getParameterValueNumbers(), variable);
			TypeReference type = ir.getParameterType(paramPosition);
			if(type.isReferenceType()) {
				return Collections.singleton(type);
			} else {
				return Collections.emptySet();
			}
		}
		if(! definesReferenceType(ir, defUse, variable)) {
			return Collections.emptySet();
		}
		
		if(symTab.isStringConstant(variable)) {
			return Collections.singleton(TypeReference.JavaLangString);
		} else if (symTab.isNullConstant(variable)) {
			return Collections.singleton(TypeReference.Null);
		}
		
		SSAInstruction instruction = defUse.getDef(variable);
		
		//instructions that always define a reference type
		if(instruction instanceof SSAArrayLoadInstruction) {			
			return Collections.singleton(((SSAArrayLoadInstruction)instruction).getElementType());
			
		} else if (instruction instanceof SSACheckCastInstruction) {
			HashSet<TypeReference> result = new HashSet<TypeReference>();
			
			for(TypeReference ref : ((SSACheckCastInstruction)instruction).getDeclaredResultTypes()) {
				if(ref.isReferenceType())
					result.add(ref);
			}
			return result;
			
		} else if (instruction instanceof SSAGetCaughtExceptionInstruction) {
			HashSet<TypeReference> result = new HashSet<TypeReference>();
			
			Iterator <TypeReference> it = ir.getBasicBlockForCatch((SSAGetCaughtExceptionInstruction)instruction).getCaughtExceptionTypes();
			while(it.hasNext()) {
				TypeReference ref = it.next();
				result.add(ref);
			}
			return result;
		} else if (instruction instanceof SSANewInstruction) {
			return Collections.singleton(((SSANewInstruction)instruction).getConcreteType());
			
		//get defines object if it reads an object field
		} else if (instruction instanceof SSAGetInstruction) {
			return Collections.singleton(((SSAGetInstruction)instruction).getDeclaredFieldType());
			
		//invoke defines an exception object and an object if it returns one
		} else if (instruction instanceof SSAInvokeInstruction) {
			HashSet<TypeReference> result = new HashSet<TypeReference>(((SSAInvokeInstruction)instruction).getExceptionTypes());
			result.add(((SSAInvokeInstruction)instruction).getDeclaredResultType());
			return result;
			
		//load metadata usually returns an object
		} else if (instruction instanceof SSALoadMetadataInstruction) {
			return Collections.singleton(((SSALoadMetadataInstruction)instruction).getType());
			
		//phi defines an object if at least one of its elements defines one
		} else if (instruction instanceof SSAPhiInstruction) {
			HashSet<TypeReference> result = new HashSet<TypeReference>();
			
			SSAPhiInstruction phi = (SSAPhiInstruction)instruction;
			for(int i = 0; i < phi.getNumberOfUses(); i++) {
				Set<TypeReference> paramTypes = definedReferenceTypes(ir, defUse, phi.getUse(i));
				result.addAll(paramTypes);				
			}
			return result;
			
		//everything else should have been caught by the definesReferenceType method
		} else {
			throw new RuntimeException("shouldn't be reachable...");
		}
	}
	
	public static int parameterPosition(int[] paramValues, int paramVariable) {		
		int paramPosition = 0;
		while(paramValues[paramPosition] != paramVariable) {
			paramPosition++;
		}
		return paramPosition;
	}
	
	public static boolean definesReferenceType(IR ir, DefUse defUse, int variable) {
		SymbolTable symTab = ir.getSymbolTable();
		
		if(symTab.isParameter(variable)) {
			int paramPosition = parameterPosition(ir.getParameterValueNumbers(), variable);			
			return ir.getParameterType(paramPosition).isReferenceType();			
		}
		
		if(symTab.isStringConstant(variable) || symTab.isNullConstant(variable)) {
			return true;
		}
		
		SSAInstruction instruction = defUse.getDef(variable);
		if(instruction == null) {
			//TODO System.err.println("No definition of " + variable + " found in " + ir.getMethod() + ". Not sure if that's OK...");
			return false;
		}
		
		if(instruction instanceof SSAInvokeInstruction) {
			return
				((SSAInvokeInstruction)instruction).getException() == variable || 
				((SSAInvokeInstruction)instruction).getDeclaredResultType().isReferenceType();
		} else {
			if(instruction instanceof SSAPhiInstruction) {
				//it happens that one 17=phi(23, 16) and another 23=phi(17, 3). This results in an infinite loop. To prevent this, we return false here.
				System.err.println("Warning: in DefUseUtils, ignoring phi instruction " + instruction + " to prevent infinite loop. Not sure if that's correct");
				return false;
			} else {
				return definesReferenceType(ir, defUse, instruction);
			}
		}
	}
	
	public static boolean definesReferenceType(IR ir, DefUse defUse, SSAInstruction instruction) {
		
		//instructions that always define a reference type
		if(				
				instruction instanceof SSAGetCaughtExceptionInstruction ||
				instruction instanceof SSANewInstruction
		) {
			return true;
			
		} else if (instruction instanceof SSACheckCastInstruction) {
			SSACheckCastInstruction cast = (SSACheckCastInstruction)instruction;
			TypeReference[] types = cast.getDeclaredResultTypes();
			for(TypeReference type : types) {
				if(type.isReferenceType())
					return true;
			}
			return false;
			
		} else if (instruction instanceof SSAArrayLoadInstruction) {
			return ((SSAArrayLoadInstruction)instruction).getElementType().isReferenceType();
			
		//get defines object if it reads an object field
		} else if (instruction instanceof SSAGetInstruction) {
			return ((SSAGetInstruction)instruction).getDeclaredFieldType().isReferenceType();
			
		//invoke defines an exception object and an object if it returns one
		} else if (instruction instanceof SSAInvokeInstruction) {
			//always returns an exception object, as far as I can tell
			return true;				
			
		//load metadata usually returns an object
		} else if (instruction instanceof SSALoadMetadataInstruction) {
			return ((SSALoadMetadataInstruction)instruction).getType().isReferenceType();
			
		//phi defines an object if at least one of its elements defines one
		} else if (instruction instanceof SSAPhiInstruction) {
			SSAPhiInstruction phi = (SSAPhiInstruction)instruction;			
			for(int i = 0; i < phi.getNumberOfUses(); i++) {
				if(definesReferenceType(ir, defUse, phi.getUse(i))) {
					return true;
				}
			}
			return false;
			
		//everything else defines a primitive or nothing at all
		} else {
			return false;
		}
	}
}
