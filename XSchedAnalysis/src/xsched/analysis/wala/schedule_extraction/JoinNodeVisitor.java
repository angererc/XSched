/**
 * 
 */
package xsched.analysis.wala.schedule_extraction;

import java.util.Set;

import com.ibm.wala.ssa.SSAPhiInstruction;
import com.ibm.wala.ssa.SSAReturnInstruction;

class JoinNodeVisitor extends NormalNodeVisitor {

	private final JoinNodeFlowData data;
	
	public JoinNodeVisitor(JoinNodeFlowData data) {	
		super(data);
		this.data = data;
	}

	private boolean isTaskPhi(SSAPhiInstruction instruction) {
		
		int numUses = instruction.getNumberOfUses();
		for(int i = 0; i < numUses; i++) {
			int use = instruction.getUse(i);
			if(data.isTask(use))
				return true;			
		}
		return false;
	}
	
	@Override
	public void visitPhi(SSAPhiInstruction instruction) {
		
		if(isTaskPhi(instruction)) {
			
			int phi = instruction.getDef();
			
			for(int i = 0; i < instruction.getNumberOfUses(); i++) {				
				EdgeFlowData edge = data.incomingEdgeAtPosition(i);
				if(edge.data() == null)
					continue;
				
				NormalNodeFlowData incomingData = edge.data();
				
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
						data.addPhiVariable(new PhiVariable(nextContext, phi), task);
						data.addLoopContext(nextContext);
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
