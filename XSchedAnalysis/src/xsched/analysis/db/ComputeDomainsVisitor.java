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
import com.ibm.wala.types.MethodReference;

public class ComputeDomainsVisitor extends Visitor {
	private final ExtensionalDatabase database;
	private final MethodReference methodRef;
	private final HashMap<Integer, Variable> variables = new HashMap<Integer, Variable>();

	public ComputeDomainsVisitor(ExtensionalDatabase database, MethodReference methodRef) {
		this.database = database;
		this.methodRef = methodRef;
	}

	public HashMap<Integer, Variable> getVariables() {
		return variables;
	}
	/* *******************
	 * helpers that register the values with the domains
	 * always wrap values in such methods
	 * 
	 * *******************/
	private void bytecode(SSAInstruction instruction) {
		database.bytecode.getOrAdd(instruction);
	}

	private void variable(int idx) {
		Variable v = variables.get(idx);
		if(v == null) {
			v = new Variable(methodRef, idx);
			variables.put(idx, v);
			database.variable.add(v);
		}
	}

	private void heapObject(NewSiteReference newSiteRef) {
		database.heap.getOrAdd(newSiteRef);
	}

	private void field(FieldReference fieldRef) {
		database.field.getOrAdd(fieldRef);
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
		bytecode(instruction);
		variable(instruction.getDef());
		variable(instruction.getRef());
		field(instruction.getDeclaredField());		
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
		bytecode(instruction);
		heapObject(instruction.getNewSite());
		variable(instruction.getDef());
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
		bytecode(instruction);
		variable(instruction.getDef());
		variable(instruction.getRef());
		field(instruction.getDeclaredField());		
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