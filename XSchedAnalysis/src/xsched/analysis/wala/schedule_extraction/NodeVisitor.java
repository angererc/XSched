/**
 * 
 */
package xsched.analysis.wala.schedule_extraction;

import xsched.analysis.wala.WalaConstants;

import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.ssa.SSAPhiInstruction;
import com.ibm.wala.ssa.SSAReturnInstruction;
import com.ibm.wala.ssa.SSAInstruction.Visitor;

class NodeVisitor extends Visitor {

	private final NodeFlowData data;
	
	public NodeVisitor(NodeFlowData data) {
		this.data = data;
	}

	@Override
	public void visitInvoke(SSAInvokeInstruction instruction) {
		if(instruction.getDeclaredTarget().equals(WalaConstants.HappensBeforeMethod)) {
			int lhs = instruction.getReceiver();
			int rhs = instruction.getUse(1);
			
			for(LoopContext loopContext : data.loopContexts()) {
				//resolve the ssa variables
				TaskVariable lhsVariable = data.taskVariableForSSAVariable(loopContext, lhs);
				TaskVariable rhsVariable = data.taskVariableForSSAVariable(loopContext, rhs);
				if(lhsVariable != null && rhsVariable != null)
					data.addHappensBeforeEdge(new HappensBeforeEdge(lhsVariable, rhsVariable));
				
			}			
		}		
	}

	@Override
	public void visitNew(SSANewInstruction instruction) {
		if(instruction.getConcreteType().equals(WalaConstants.TaskType)) {
			for(LoopContext loopContext : data.loopContexts()) {
				data.addTaskScheduleSite(new TaskVariable(loopContext, instruction.getDef()));
			}			
		}
	}

	@Override
	public void visitPhi(SSAPhiInstruction instruction) {
		for(int i = 0; i < instruction.getNumberOfUses(); i++) {
			int use = instruction.getUse(i);
			Task 
		}
	}

	@Override
	public void visitReturn(SSAReturnInstruction instruction) {
		// TODO Auto-generated method stub
		
	}
	
}