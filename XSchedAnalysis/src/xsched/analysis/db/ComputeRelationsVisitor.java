package xsched.analysis.db;

import java.util.HashMap;

import com.ibm.wala.classLoader.NewSiteReference;
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
import com.ibm.wala.types.FieldReference;

public class ComputeRelationsVisitor extends Visitor {
	private final ExtensionalDatabase database;	
	private final HashMap<Integer, Variable> variables;

	public ComputeRelationsVisitor(ExtensionalDatabase database, HashMap<Integer, Variable> variables) {
		this.database = database;
		this.variables = variables;
	}

	/* *******************
	 * helpers that register the values with the domains
	 * always wrap values in such methods
	 * 
	 * *******************/
	private SSAInstruction bytecode(SSAInstruction instruction) {
		return database.bytecode.get(instruction);
	}

	private Variable variable(int idx) {
		Variable v = variables.get(idx);		
		return database.variable.get(v);
	}

	private NewSiteReference heapObject(NewSiteReference newSiteRef) {
		return database.heap.get(newSiteRef);
	}

	private FieldReference field(FieldReference fieldRef) {
		return database.field.get(fieldRef);
	}

	/* *******************
	 * the visitor implementation that
	 * actually fills the extensional database
	 * *******************/

	@Override
	public void visitArrayLength(SSAArrayLengthInstruction instruction) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitArrayLoad(SSAArrayLoadInstruction instruction) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitArrayStore(SSAArrayStoreInstruction instruction) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitBinaryOp(SSABinaryOpInstruction instruction) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitCheckCast(SSACheckCastInstruction instruction) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitComparison(SSAComparisonInstruction instruction) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitConditionalBranch(
			SSAConditionalBranchInstruction instruction) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitConversion(SSAConversionInstruction instruction) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitGet(SSAGetInstruction instruction) {
		SSAInstruction bytecode = bytecode(instruction);
		Variable lhs = variable(instruction.getDef());
		Variable rhs = variable(instruction.getRef());
		FieldReference field = field(instruction.getDeclaredField());
		database.load.add(bytecode, lhs, field, rhs);
	}

	@Override
	public void visitGetCaughtException(
			SSAGetCaughtExceptionInstruction instruction) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitGoto(SSAGotoInstruction instruction) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitInstanceof(SSAInstanceofInstruction instruction) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitInvoke(SSAInvokeInstruction instruction) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitLoadMetadata(SSALoadMetadataInstruction instruction) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitMonitor(SSAMonitorInstruction instruction) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitNew(SSANewInstruction instruction) {
		SSAInstruction bytecode = bytecode(instruction);
		NewSiteReference heapObject = heapObject(instruction.getNewSite());
		Variable lhsVariable = variable(instruction.getDef());

		database.newStatement.add(bytecode, lhsVariable, heapObject);

		//if the new statement is for an array, it would contain 'uses' that hold the dimensions of the array
		//but because we are ignorant of the exact contents of arrays, we don't do anything here...
	}

	@Override
	public void visitPhi(SSAPhiInstruction instruction) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitPi(SSAPiInstruction instruction) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitPut(SSAPutInstruction instruction) {
		SSAInstruction bytecode = bytecode(instruction);
		Variable lhs = variable(instruction.getDef());
		Variable rhs = variable(instruction.getRef());
		FieldReference field = field(instruction.getDeclaredField());
		database.store.add(bytecode, lhs, field, rhs);
	}

	@Override
	public void visitReturn(SSAReturnInstruction instruction) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitSwitch(SSASwitchInstruction instruction) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitThrow(SSAThrowInstruction instruction) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitUnaryOp(SSAUnaryOpInstruction instruction) {
		// TODO Auto-generated method stub

	}

}