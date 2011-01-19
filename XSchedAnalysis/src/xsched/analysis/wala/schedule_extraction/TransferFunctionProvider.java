/**
 * 
 */
package xsched.analysis.wala.schedule_extraction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.ibm.wala.dataflow.graph.AbstractMeetOperator;
import com.ibm.wala.dataflow.graph.ITransferFunctionProvider;
import com.ibm.wala.fixedpoint.impl.UnaryOperator;
import com.ibm.wala.fixpoint.IVariable;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSAInstruction;

final class TransferFunctionProvider implements ITransferFunctionProvider<ISSABasicBlock, FlowData> {
	
	
	private final AbstractMeetOperator<FlowData> meetOperator = new AbstractMeetOperator<FlowData>() {
				
		@Override
		public boolean isUnaryNoOp() {
			return true;
		}
		
		//the forward flow data is the stuff that comes in from before a possible loop
		ArrayList<EdgeFlowData> forwardFlowData;
		ArrayList<EdgeFlowData> backwardFlowData;
		@SuppressWarnings("unchecked")
		private void sortEdgeFlowData(IVariable[] rhs) {
			// sort the backward and forward flow edges into 
			int len = rhs.length;
			forwardFlowData = new ArrayList<EdgeFlowData>(len);
			backwardFlowData = new ArrayList<EdgeFlowData>(len);
			
			for(int i = 0; i < len; i++) {
				EdgeFlowData dat = (EdgeFlowData)rhs[i];
				if(dat.isBackEdge) 
					backwardFlowData.add(dat);
				else
					forwardFlowData.add(dat);
			}
		}
		
		@SuppressWarnings("unchecked")
		private boolean assertVariablesAreOK(IVariable[] rhs) {
			for(int i=0; i < rhs.length; i++) {
				EdgeFlowData dat = (EdgeFlowData)rhs[i];
				if( ! (forwardFlowData.contains(dat) || backwardFlowData.contains(dat)))
						return false;
			}
			
			if( ! (forwardFlowData.size() + backwardFlowData.size() == rhs.length))
				return false;
			
			return true;
		}

		@SuppressWarnings("unchecked")
		@Override
		public byte evaluate(FlowData lhs, IVariable[] rhs) {
			
			if(forwardFlowData == null)
				sortEdgeFlowData(rhs);
			
			//i assume that the rhs array never changes, but I am not 100% sure
			assert assertVariablesAreOK(rhs);
			
			NodeFlowData result = new NodeFlowData(((NodeFlowData)lhs).isLoopHead);
			
			for(EdgeFlowData edgeData : forwardFlowData) {
				result.mergeWithForwardEdge(edgeData);
			}
			
			for(EdgeFlowData edgeData : backwardFlowData) {
				//first, see what tasks are actually scheduled inside the loop
				Set<TaskVariable> tasksInsideLoop = new HashSet<TaskVariable>(edgeData.data.taskScheduleSites());
				tasksInsideLoop.removeAll(result.taskScheduleSites());
				
				//we need to give new versions to them
				for(TaskVariable taskInsideLoop : tasksInsideLoop) {
					result.addTaskScheduleSite(taskInsideLoop.versionForEdge(edgeData));
				}
			}
			
			return CHANGED;

		}

		@Override
		public int hashCode() {
			return System.identityHashCode(this);
		}

		@Override
		public boolean equals(Object o) {
			return o == this;						
		}

		@Override
		public String toString() {
			return "Meet Operator";
		}

	};

	private final UnaryOperator<FlowData> edgeTransferFunction =  new UnaryOperator<FlowData>() {

		@Override
		public byte evaluate(FlowData lhs, FlowData rhs) {
			assert lhs instanceof EdgeFlowData;
			assert rhs instanceof NodeFlowData;
			
			//the edge flow data will just take the node flow data as its new state if copied
			if(lhs.stateEquals(rhs)) {
				return NOT_CHANGED;
			} else {
				lhs.copyState(rhs);
				return CHANGED;
			}
					
		}

		@Override
		public String toString() {
			return "EDGE-FLOW";
		}

		@Override
		public int hashCode() {
			return System.identityHashCode(this);
		}

		@Override
		public boolean equals(Object o) {
			return this == o;
		}
		
	};
	
	@Override
	public UnaryOperator<FlowData> getNodeTransferFunction(final ISSABasicBlock node) {
		return new UnaryOperator<FlowData>()  {
			@Override
			public byte evaluate(FlowData lhs, FlowData rhs) {
				assert lhs instanceof NodeFlowData;
				assert rhs instanceof NodeFlowData;
				
				NodeFlowData data = ((NodeFlowData)rhs).duplicate();
				
				for(SSAInstruction instruction : node) {
					instruction.visit(new NodeVisitor(data));
				}
				
				if(data.stateEquals(lhs)) {
					return NOT_CHANGED;
				} else {
					lhs.copyState(data);
					return CHANGED;
				}
			}

			@Override
			public String toString() {
				return "NODE-FLOW";
			}

			@Override
			public int hashCode() {
				return 9973 * node.hashCode();
			}

			@Override
			public boolean equals(Object o) {
				return this == o;
			}
		};
	}
	
	@Override
	public AbstractMeetOperator<FlowData> getMeetOperator() {
		return meetOperator;
	}

	@Override
	public UnaryOperator<FlowData> getEdgeTransferFunction(final ISSABasicBlock src, final ISSABasicBlock dst) {
		return edgeTransferFunction;
	}
	
	@Override
	public boolean hasEdgeTransferFunctions() { 
		return true;
	}

	@Override
	public boolean hasNodeTransferFunctions() {
		return true;
	}
}