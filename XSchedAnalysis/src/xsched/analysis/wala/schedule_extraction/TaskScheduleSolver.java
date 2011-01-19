/**
 * 
 */
package xsched.analysis.wala.schedule_extraction;

import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.wala.dataflow.graph.BasicFramework;
import com.ibm.wala.dataflow.graph.DataflowSolver;
import com.ibm.wala.fixedpoint.impl.AbstractStatement;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.graph.Acyclic;
import com.ibm.wala.util.intset.IBinaryNaturalRelation;
import com.ibm.wala.util.intset.IntPair;

public class TaskScheduleSolver extends DataflowSolver<ISSABasicBlock, FlowData> {

	public static boolean solve(SSACFG cfg) {
		try {
			TaskScheduleSolver solver = new TaskScheduleSolver(cfg);
			return solver.solve((IProgressMonitor)null);			
		} catch (CancelException e) {			
			//
		}
		return false;
	};
	
	/**
	 * 
	 */
	final SSACFG cfg;
	final IBinaryNaturalRelation backEdges;
	private FlowData entry;
	
	public TaskScheduleSolver(SSACFG cfg) {
		super(new BasicFramework<ISSABasicBlock, FlowData>(cfg, new TransferFunctionProvider()));
		this.cfg = cfg;
		this.backEdges = Acyclic.computeBackEdges(cfg, cfg.entry());
	}

	@Override
	protected FlowData makeEdgeVariable(ISSABasicBlock src, ISSABasicBlock dst) {
		assert src != null;
		assert dst != null;
		
		return backEdges.contains(src.getGraphNodeId(), dst.getGraphNodeId()) ?
				new BackEdgeFlowData(src, dst) : new EdgeFlowData();	
	}

	@Override
	protected FlowData makeNodeVariable(ISSABasicBlock n, boolean IN) {
		assert n != null;
		
		boolean isLoopHead = false;
		for(IntPair rel : backEdges) {
			if(rel.getY() == n.getGraphNodeId()) {
				isLoopHead = true;
				break;
			}
		}
		
		NodeFlowData result = new NodeFlowData(isLoopHead);
		result.initEmpty();
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