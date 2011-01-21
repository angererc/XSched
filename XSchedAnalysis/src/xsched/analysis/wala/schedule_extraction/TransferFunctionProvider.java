/**
 * 
 */
package xsched.analysis.wala.schedule_extraction;

import com.ibm.wala.dataflow.graph.AbstractMeetOperator;
import com.ibm.wala.dataflow.graph.ITransferFunctionProvider;
import com.ibm.wala.fixedpoint.impl.UnaryOperator;
import com.ibm.wala.fixpoint.IVariable;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSAInstruction;

final class TransferFunctionProvider implements ITransferFunctionProvider<ISSABasicBlock, FlowData> {
		
	TransferFunctionProvider() {
	}
	
	private final AbstractMeetOperator<FlowData> meetOperator = new AbstractMeetOperator<FlowData>() {
		
		private final boolean DEBUG = false;
		//the meet operator will not be called if there is only one incoming edge; if there
		//are more than one incoming edges, we are a join node and use a different node visitor to handle
		//potential phi nodes
		@Override
		public boolean isUnaryNoOp() {
			return false;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public byte evaluate(FlowData lhs, IVariable[] rhs) {
			if(rhs.length == 1) {
				NormalNodeFlowData lhsData = (NormalNodeFlowData)lhs;
				EdgeFlowData rhsEdge = (EdgeFlowData)rhs[0];
				assert ! rhsEdge.isInitial();
				NormalNodeFlowData rhsData = rhsEdge.getData();
				if(lhsData.stateEquals(rhsData)) {
					if(DEBUG)
						System.out.println("TransferFunctionProvider: meet " + lhs + " value did NOT change");
					return NOT_CHANGED;
				} else {
					if(DEBUG)
						System.out.println("TransferFunctionProvider: meet " + lhs + " value did change");
					lhsData.copyState(rhsData);
					return CHANGED;
				}
			}
			
			assert rhs.length > 1;
			assert lhs instanceof JoinNodeFlowData;
			
			EdgeFlowData[] incoming = new EdgeFlowData[rhs.length];			
			for(int i = 0; i < rhs.length; i++) {
				EdgeFlowData edge = (EdgeFlowData)rhs[i];
				assert edge != null;
				incoming[i] = edge;
			}
						
			JoinNodeFlowData result = new JoinNodeFlowData(((JoinNodeFlowData)lhs).basicBlock, incoming);
			
			if(lhs.stateEquals(result)) {
				if(DEBUG)
					System.out.println("TransferFunctionProvider: meet " + lhs + " value did NOT change");
				return NOT_CHANGED;
			} else {
				if(DEBUG)
					System.out.println("TransferFunctionProvider: meet " + lhs + " value did change");
				lhs.copyState(result);
				return CHANGED;
			}			

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

		//private final boolean DEBUG = false;
		@Override
		public byte evaluate(FlowData lhs, FlowData rhs) {
			assert lhs instanceof EdgeFlowData;
			assert rhs instanceof NormalNodeFlowData;
			
			lhs.copyState(rhs);
			return CHANGED;
			//the edge flow data will just take the node flow data as its new state if copied
//			if(lhs.stateEquals(rhs)) {
//				if(DEBUG)
//					System.out.println("TransferFunctionProvider: edge transfer function " + lhs + " value did NOT change");
//				return NOT_CHANGED;
//			} else {
//				if(DEBUG)
//					System.out.println("TransferFunctionProvider: edge transfer function " + lhs + " value did change");
//				lhs.copyState(rhs);
//				return CHANGED;
//			}
					
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
			
			private final boolean DEBUG = false;
			@Override
			public byte evaluate(FlowData lhs, FlowData rhs) {
				//lhs can be a JoinNodeFlowData, too
				assert lhs instanceof NormalNodeFlowData;
				//Note: rhs can be a EdgeFlowData or a NormalNodeFlowData. I guess for only one incoming edge we don't get actual edges because
				//we said that meet for unary ops is a no-op
				
				NormalNodeFlowData rhsData = (rhs instanceof EdgeFlowData) ? ((EdgeFlowData)rhs).getData() : (NormalNodeFlowData)rhs;
				assert ! rhsData.isInitial();
				
				NormalNodeFlowData data = rhsData.duplicate(((NormalNodeFlowData)lhs).basicBlock);
				NormalNodeVisitor visitor = data.nodeVisitor();
				
				for(SSAInstruction instruction : node) {
					instruction.visit(visitor);
				}
				
				if(!node.isEntryBlock() && lhs.stateEquals(data)) {
					if(DEBUG)
						System.out.println("TransferFunctionProvider: node transfer function " + lhs + " value did NOT change");
					return NOT_CHANGED;
				} else {
					if(DEBUG)
						System.out.println("TransferFunctionProvider: node transfer function " + lhs + " value did change");
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