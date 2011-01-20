/**
 * 
 */
package xsched.analysis.wala.schedule_extraction;

import java.util.Set;

import xsched.analysis.wala.WalaConstants;

import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.ssa.SSAPhiInstruction;
import com.ibm.wala.ssa.SSAReturnInstruction;
import com.ibm.wala.ssa.SSAInstruction.Visitor;
import com.ibm.wala.util.debug.Assertions;

class NormalNodeVisitor extends Visitor {

	private final NormalNodeFlowData data;
	
	public NormalNodeVisitor(NormalNodeFlowData data) {
		this.data = data;
	}

	@Override
	public void visitInvoke(SSAInvokeInstruction instruction) {
		if(instruction.getDeclaredTarget().equals(WalaConstants.HappensBeforeMethod)) {
			int lhs = instruction.getReceiver();
			int rhs = instruction.getUse(1);
			
			for(LoopContext loopContext : data.loopContexts()) {
				//resolve the ssa variables
				Set<TaskVariable> lhsVariables = data.taskVariableForSSAVariable(loopContext, lhs);
				Set<TaskVariable> rhsVariables = data.taskVariableForSSAVariable(loopContext, rhs);
				for(TaskVariable lhsVariable : lhsVariables) {
					for(TaskVariable rhsVariable : rhsVariables) {
						data.addHappensBeforeEdge(new HappensBeforeEdge(lhsVariable, rhsVariable));
					}
				}				
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
		Assertions.UNREACHABLE();
	}

	@Override
	public void visitReturn(SSAReturnInstruction instruction) {
		// TODO Auto-generated method stub
		
	}
	
}