package xsched.analysis.wala.schedule_extraction;

import com.ibm.wala.cfg.ControlFlowGraph;
import com.ibm.wala.cfg.IBasicBlock;
import com.ibm.wala.ipa.cfg.EdgeFilter;
import com.ibm.wala.ipa.cfg.PrunedCFG;

public class AutomaticExceptionPrunedCFG {

	private static class ExceptionEdgePruner<I, T extends IBasicBlock<I>> implements EdgeFilter<T>{
		private final ControlFlowGraph<I, T> cfg;

		ExceptionEdgePruner(ControlFlowGraph<I, T> cfg) {
			this.cfg = cfg;
		}

		public boolean hasNormalEdge(T src, T dst) {
			return cfg.getNormalSuccessors(src).contains(dst);
		}

		public boolean hasExceptionalEdge(T src, T dst) {
			//remove all exceptional edges to the exit block
			//those have been inserted automatically by wala but our semantics is that
			//when an unhandled exception occurs all scheduled child tasks are aborted
			return !cfg.exit().equals(dst) && cfg.getExceptionalSuccessors(src).contains(dst);			
		}
	};

	public static <I, T extends IBasicBlock<I>> PrunedCFG<I, T> make(ControlFlowGraph<I, T> cfg) {
		return PrunedCFG.make(cfg, new ExceptionEdgePruner<I, T>(cfg));
	}
}