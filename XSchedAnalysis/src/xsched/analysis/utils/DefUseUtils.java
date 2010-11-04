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
		if(symTab.isStringConstant(variable)) {
			return Collections.singleton(TypeReference.JavaLangString);
		} else if (symTab.isNullConstant(variable)) {
			return Collections.singleton(TypeReference.Null);
		}
		
		SSAInstruction instruction = defUse.getDef(variable);
		if(instruction == null) {
			assert(symTab.isConstant(variable)) : "variable " + variable + " must be a non-string constant";
			return Collections.emptySet();
		}
		
		//instructions that always define a reference type
		if(instruction instanceof SSAArrayLoadInstruction) {		
			TypeReference typeRef = ((SSAArrayLoadInstruction)instruction).getElementType();
			if(typeRef.isReferenceType()) {
				return Collections.singleton(typeRef);
			} else {
				return Collections.emptySet();
			}
			
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
			TypeReference typeRef = ((SSAGetInstruction)instruction).getDeclaredFieldType();
			if(typeRef.isPrimitiveType()) {
				return Collections.singleton(typeRef);
			} else {
				return Collections.emptySet();
			}
			
		//invoke defines an exception object and an object if it returns one
		} else if (instruction instanceof SSAInvokeInstruction) {
			HashSet<TypeReference> result = new HashSet<TypeReference>(((SSAInvokeInstruction)instruction).getExceptionTypes());
			TypeReference typeRef = ((SSAInvokeInstruction)instruction).getDeclaredResultType();
			if(typeRef.isReferenceType()) {
				result.add(typeRef);
			}
			return result;
			
		//load metadata usually returns an object
		} else if (instruction instanceof SSALoadMetadataInstruction) {
			TypeReference typeRef = ((SSALoadMetadataInstruction)instruction).getType();
			if(typeRef.isReferenceType()) {
				return Collections.singleton(typeRef);
			} else {
				return Collections.emptySet();
			}
			
		//phi defines an object if at least one of its elements defines one
		} else if (instruction instanceof SSAPhiInstruction) {
			//find default value of phi and return that type
			HashSet<TypeReference> typeRefs = new HashSet<TypeReference>();
			collectPhiDefs(ir, defUse, new HashSet<SSAPhiInstruction>(), (SSAPhiInstruction)instruction, typeRefs);			
			//System.out.println("defuse utils: found typeRefs " + typeRefs + " for phi " + instruction);
			return typeRefs;
		//everything else should have been caught by the definesReferenceType method
		} else {
			//System.out.println("defuse utils: instruction does not define a reference type " + instruction);
			return Collections.emptySet();
		}
	}
	
	//since it's kinda whacky to find out what the type of a phi node is in wala, I do this brute-forcy approach of collecting all defs
	//of the phi that reach it, taking care that (really existing) loops in phi nodes don't result in an infinite loop
	private static void collectPhiDefs(IR ir, DefUse defUse, HashSet<SSAPhiInstruction> visited, SSAPhiInstruction phi, HashSet<TypeReference> collected) {
		if(visited.contains(phi)) {
			return;
		}
		visited.add(phi);
		for(int i=0; i < phi.getNumberOfUses(); i++) {
			int variable = phi.getUse(i);
			SSAInstruction child = defUse.getDef(variable);
			if(child instanceof SSAPhiInstruction) {
				collectPhiDefs(ir, defUse, visited, phi, collected);
			} else {
				//child is NOT an SSAPhiInstruction and therefore the call to definedReferenceTypes will not recurse!
				collected.addAll(definedReferenceTypes(ir, defUse, variable));
			}
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
		Set<TypeReference> refs = definedReferenceTypes(ir, defUse, variable);
		return !refs.isEmpty();		
	}
	
	public static boolean definesReferenceType(IR ir, DefUse defUse, SSAInstruction instruction) {
		Set<TypeReference> refs = definedReferenceTypes(ir, defUse, instruction.getDef());
		return !refs.isEmpty();
	}
}
