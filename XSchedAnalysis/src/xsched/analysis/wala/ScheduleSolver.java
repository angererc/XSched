package xsched.analysis.wala;

import java.util.Iterator;

import com.ibm.wala.dataflow.graph.AbstractMeetOperator;
import com.ibm.wala.dataflow.graph.BasicFramework;
import com.ibm.wala.dataflow.graph.DataflowSolver;
import com.ibm.wala.dataflow.graph.IKilldallFramework;
import com.ibm.wala.dataflow.graph.ITransferFunctionProvider;
import com.ibm.wala.fixedpoint.impl.AbstractStatement;
import com.ibm.wala.fixedpoint.impl.AbstractVariable;
import com.ibm.wala.fixedpoint.impl.UnaryOperator;
import com.ibm.wala.fixpoint.IVariable;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSAArrayLengthInstruction;
import com.ibm.wala.ssa.SSAArrayLoadInstruction;
import com.ibm.wala.ssa.SSAArrayStoreInstruction;
import com.ibm.wala.ssa.SSABinaryOpInstruction;
import com.ibm.wala.ssa.SSACFG;
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
import com.ibm.wala.ssa.SSAInstruction.IVisitor;
import com.ibm.wala.ssa.SSAInstruction.Visitor;
import com.ibm.wala.util.CancelException;

public class ScheduleSolver {

	/*
	 * *******************************************
	 * *******************************************
	 */
	private static class ScheduleData extends AbstractVariable<ScheduleData> {
		
		public ScheduleData() {
			
		}

		@Override
		public void copyState(ScheduleData v) {
			
		}
		
		public ScheduleData duplicate() {
			ScheduleData result = new ScheduleData();
			result.copyState(this);
			return result;
		}
		
		public boolean stateEquals(ScheduleData other) {
			return true;
		}
	}

	/*
	 * *******************************************
	 * *******************************************
	 */
	private class SolverFramework extends BasicFramework<ISSABasicBlock, ScheduleData> {

		public SolverFramework() {

			super(cfg, new ITransferFunctionProvider<ISSABasicBlock, ScheduleData>() {

				private final AbstractMeetOperator<ScheduleData> meetOperator = new AbstractMeetOperator<ScheduleData>() {

					@Override
					public boolean isUnaryNoOp() {
						return false;
					}

					@SuppressWarnings("unchecked")
					@Override
					public byte evaluate(ScheduleData lhs, IVariable[] rhs) {
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

				@Override
				public AbstractMeetOperator<ScheduleData> getMeetOperator() {
					return meetOperator;
				}

				@Override
				public UnaryOperator<ScheduleData> getEdgeTransferFunction(final ISSABasicBlock src, final ISSABasicBlock dst) {
					return new UnaryOperator<ScheduleData>() {
						@Override
						public byte evaluate(ScheduleData lhs, ScheduleData rhs) {
							return CHANGED;
						}

						@Override
						public String toString() {
							return "EDGE-FLOW";
						}

						@Override
						public int hashCode() {
							return 9973 * (src.hashCode() ^ dst.hashCode());
						}

						@Override
						public boolean equals(Object o) {
							return this == o;
						}

					};
				}

				@Override
				public UnaryOperator<ScheduleData> getNodeTransferFunction(final ISSABasicBlock node) {
					return new UnaryOperator<ScheduleData>()  {
						@Override
						public byte evaluate(ScheduleData lhs, ScheduleData rhs) {
							
							ScheduleData data = lhs.duplicate();
							
							for(SSAInstruction instruction : node) {
								instruction.visit(new NodeVisitor(data));
							}
							
							if(data.stateEquals(rhs)) {
								return NOT_CHANGED;
							} else {
								rhs.copyState(data);
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
				public boolean hasEdgeTransferFunctions() { 
					return true;
				}

				@Override
				public boolean hasNodeTransferFunctions() {
					return true;
				}


			});
		}

	}

	/*
	 * *******************************************
	 * *******************************************
	 */
	private static class NodeVisitor extends Visitor {

		private final ScheduleData data;
		
		public NodeVisitor(ScheduleData data) {
			this.data = data;
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
			// TODO Auto-generated method stub
			
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
			// TODO Auto-generated method stub
			
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
			// TODO Auto-generated method stub
			
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
	
	/*
	 * *******************************************
	 * *******************************************
	 */
	private class ScheduleDataFlowSolver extends DataflowSolver<ISSABasicBlock, ScheduleData> {

		private ScheduleData entry;

		public ScheduleDataFlowSolver(IKilldallFramework<ISSABasicBlock, ScheduleData> problem) {
			super(problem);
		}

		@Override
		protected ScheduleData makeEdgeVariable(ISSABasicBlock src, ISSABasicBlock dst) {
			assert src != null;
			assert dst != null;
			return new ScheduleData();
		}

		@Override
		protected ScheduleData makeNodeVariable(ISSABasicBlock n, boolean IN) {
			assert n != null;
			ScheduleData result = new ScheduleData(); 
			if (IN && n.equals(cfg.entry())) {
				entry = result;
			}
			return result;
		}

		@Override
		protected void initializeWorkList() {
			super.buildEquations(false, false);
			/*
			 * Add only the entry variable to the work list.
			 */
			
			for (Iterator<?> it = getFixedPointSystem().getStatementsThatUse(entry); it.hasNext();) {
				AbstractStatement<?, ?> s = (AbstractStatement<?, ?>) it.next();
				addToWorkList(s);
			}
		}

		@Override
		protected void initializeVariables() {
			super.initializeVariables();	      
		}
	}

	/*
	 * *******************************************
	 * *******************************************
	 */
	/*
	 * the implementation of this class
	 */
	private final SSACFG cfg;
	private final ScheduleDataFlowSolver solver;

	public ScheduleSolver(SSACFG cfg) {
		this.cfg = cfg;
		SolverFramework problem = new SolverFramework();
		solver = new ScheduleDataFlowSolver(problem);
	}

	public boolean solve() {
		try {
			return solver.solve(null);
		} catch (CancelException e) {			
			//
		}
		return false;
	};


}
