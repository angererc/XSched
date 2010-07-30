package xsched.analysis.db;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAArrayLoadInstruction;
import com.ibm.wala.ssa.SSAArrayStoreInstruction;
import com.ibm.wala.ssa.SSACheckCastInstruction;
import com.ibm.wala.ssa.SSAGetCaughtExceptionInstruction;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSALoadMetadataInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.ssa.SSAPhiInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.ssa.SSAReturnInstruction;
import com.ibm.wala.ssa.SSAThrowInstruction;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.ssa.SSAInstruction.Visitor;
import com.ibm.wala.types.TypeReference;

class ComputeRelations {
	
	private final AnalysisCache cache;
	private final AnalysisOptions options; 
	
	private final HandleRelationsLogic handler;
	private final MethodVisitor visitor = new MethodVisitor();
	
	ComputeRelations(FillExtensionalDatabase parent) {		
		this.cache = parent.cache;
		this.options = parent.options;
		
		handler = new HandleRelationsLogic(parent);
		
		addDefaultRelations();
		
		//work on all the classes in the hierarchy
		for(IClass klass : parent.classHierarchy) {
			processClass(klass);
		}
	}
	
	private void addDefaultRelations() {		
		handler.database.objectType.add(handler.database.theGlobalObject, TypeReference.JavaLangClass.getName());
		handler.database.assignObject.add(handler.database.theGlobalObjectRef, handler.database.theGlobalObject);
		
		handler.database.objectType.add(handler.database.theImmutableStringObject, TypeReference.JavaLangString.getName());		
	}

	private void processClass(IClass klass) {
		
		handler.addToAssignableRel(klass);
		handler.database.objectType.add(new ObjectCreationSite.SpecialCreationSite(klass.getName()), klass.getName());
		handler.addToMethodImplementationRel(klass);
				
		for(IMethod method : klass.getDeclaredMethods()) {
			if( ! (method.isAbstract() || method.isNative())) {				
				processCallableMethod(method);
			}
		}
	}
	
	private void processCallableMethod(IMethod method) {
		IR ir = cache.getSSACache().findOrCreateIR(method, Everywhere.EVERYWHERE, options.getSSAOptions());
		
		handler.beginMethod(method, ir);
		
		SymbolTable symTab = ir.getSymbolTable();
		//TODO make sure that we include constant pointers (not sure if we have to handle NULL or just leave it away...)
		for(int variable = 1; variable <= symTab.getMaxValueNumber(); variable++) {
			//************
			//Variables domain
			if(symTab.isStringConstant(variable)) {
				handler.addToAssignObjectRel(variable, symTab.getStringValue(variable));
			}			
		}
		
		handler.addToFormalRel();
		ir.visitAllInstructions(visitor);
	}
	
	private class MethodVisitor extends Visitor {
		
		/* *******************
		 * the visitor implementation that
		 * actually fills the extensional database
		 * *******************/
		
		@Override
		public void visitArrayLoad(SSAArrayLoadInstruction instruction) {
			handler.addToLoadRel(instruction);
		}
	
		@Override
		public void visitArrayStore(SSAArrayStoreInstruction instruction) {
			handler.addToStoreRel(instruction);
		}
	
		@Override
		public void visitCheckCast(SSACheckCastInstruction instruction) {
			handler.addToAssignsRel(instruction);
		}
	
		@Override
		public void visitGet(SSAGetInstruction instruction) {	
			handler.addToLoadRel(instruction);		
		}
	
		@Override
		public void visitGetCaughtException(SSAGetCaughtExceptionInstruction instruction) {
			new RuntimeException("no idea how to handle that yet...").printStackTrace();
		}
			
		@Override
		public void visitInvoke(SSAInvokeInstruction instruction) {
			handler.addToInvokeRel(instruction);
			handler.addToActualsRel(instruction);
			handler.addToCallSiteReturnsRel(instruction);
		}
	
		@Override
		public void visitLoadMetadata(SSALoadMetadataInstruction instruction) {
			handler.addToAssignObjectRel(instruction);
		}
	
		@Override
		public void visitNew(SSANewInstruction instruction) {			
			handler.addToAssignObjectRel(instruction);
		}
	
		@Override
		public void visitPhi(SSAPhiInstruction instruction) {
			handler.addToAssignsRel(instruction);
		}
		
		@Override
		public void visitPut(SSAPutInstruction instruction) {
			handler.addToStoreRel(instruction);			
		}
	
		@Override
		public void visitReturn(SSAReturnInstruction instruction) {
			handler.addToMethodReturnRel(instruction);
		}
	
		@Override
		public void visitThrow(SSAThrowInstruction instruction) {
			new RuntimeException("no idea how to handle that yet...").printStackTrace();
		}
	}
}