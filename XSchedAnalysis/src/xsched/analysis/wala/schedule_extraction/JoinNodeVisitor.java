/**
 * 
 */
package xsched.analysis.wala.schedule_extraction;

import java.util.Set;

import com.ibm.wala.ssa.SSAPhiInstruction;
import com.ibm.wala.ssa.SSAReturnInstruction;

class JoinNodeVisitor extends NormalNodeVisitor {

	public JoinNodeVisitor(JoinNodeFlowData data) {	
		super(data);
	}

	private boolean isTaskPhi(SSAPhiInstruction instruction) {
		
		int numUses = instruction.getNumberOfUses();
		for(int i = 0; i < numUses; i++) {
			int use = instruction.getUse(i);
			if(((JoinNodeFlowData)data).isTask(use))
				return true;			
		}
		return false;
	}
	
	@Override
	public void visitPhi(SSAPhiInstruction instruction) {
		
		if(isTaskPhi(instruction)) {
			
			int phi = instruction.getDef();
			
			for(int i = 0; i < instruction.getNumberOfUses(); i++) {				
				EdgeFlowData edge = ((JoinNodeFlowData)data).incomingEdgeAtPosition(i);
				if(edge.isInitial())
					continue;
				
				NormalNodeFlowData incomingData = edge.getData();
				
				int use = instruction.getUse(i);
				
				//for back edges:
				//for each context of the used data, find the value of the phi use
				//then add the edge to the loop context and add a phi mapping
				// "find value(s) before edge has been taken and record it for after edge has been taken
				
				for(LoopContext lc : incomingData.loopContexts()) {
					LoopContext nextContext;
					if(edge instanceof BackEdgeFlowData) {
						nextContext = lc.contextByAddingLoop((BackEdgeFlowData)edge);
					} else {
						nextContext = lc;
					}
					
					Set<TaskVariable> tasks = incomingData.taskVariableForSSAVariable(lc, use);
					for(TaskVariable task : tasks) {						
						((JoinNodeFlowData)data).addPhiVariable(new PhiVariable(nextContext, phi), task);						
					}
				}
								
			}
		}
	}

	@Override
	public void visitReturn(SSAReturnInstruction instruction) {
		// TODO Auto-generated method stub
		
	}
	
}
