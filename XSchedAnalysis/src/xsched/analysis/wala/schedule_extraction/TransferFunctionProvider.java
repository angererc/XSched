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
		
		//the meet operator will not be called if there is only one incoming edge; if there
		//are more than one incoming edges, we are a join node and use a different node visitor to handle
		//potential phi nodes
		@Override
		public boolean isUnaryNoOp() {
			return true;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public byte evaluate(FlowData lhs, IVariable[] rhs) {
			
			assert rhs.length > 1;
			assert lhs instanceof JoinNodeFlowData;
			
			EdgeFlowData[] incoming = new EdgeFlowData[rhs.length];			
			for(int i = 0; i < rhs.length; i++) {
				EdgeFlowData edge = (EdgeFlowData)rhs[i];
				assert edge != null;
				incoming[i] = edge;
			}
						
			JoinNodeFlowData result = new JoinNodeFlowData(((JoinNodeFlowData)lhs).basicBlock, incoming);
						
			//XXX somehow the new loop contexts must get into this
			//backward edge kills loop contexts that existed in incoming edges
			if(lhs.stateEquals(result)) {
				return NOT_CHANGED;
			} else {
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

		@Override
		public byte evaluate(FlowData lhs, FlowData rhs) {
			assert lhs instanceof EdgeFlowData;
			assert rhs instanceof NormalNodeFlowData;
			
			//the edge flow data will just take the node flow data as its new state if copied
			if(lhs.stateEquals(rhs)) {
				System.out.println("TransferFunctionProvider: edge transfer function " + lhs + " value did NOT change");
				return NOT_CHANGED;
			} else {
				System.out.println("TransferFunctionProvider: edge transfer function " + lhs + " value did change");
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
				//lhs can be a JoinNodeFlowData, too
				assert lhs instanceof NormalNodeFlowData;
				//Note: rhs can be a EdgeFlowData or a NormalNodeFlowData. I guess for only one incoming edge we don't get actual edges because
				//we said that meet for unary ops is a no-op
				
				NormalNodeFlowData rhsData = (rhs instanceof EdgeFlowData) ? ((EdgeFlowData)rhs).data() : (NormalNodeFlowData)rhs;
				assert rhsData != null;
				
				NormalNodeFlowData data = rhsData.duplicate();
				NormalNodeVisitor visitor = data.nodeVisitor();
				
				for(SSAInstruction instruction : node) {
					instruction.visit(visitor);
				}
				
				if(!node.isEntryBlock() && lhs.stateEquals(data)) {
					System.out.println("TransferFunctionProvider: node transfer function " + lhs + " value did NOT change");
					return NOT_CHANGED;
				} else {
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