/**
 * 
 */
package xsched.analysis.wala.schedule_extraction;

import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.wala.dataflow.graph.BasicFramework;
import com.ibm.wala.dataflow.graph.DataflowSolver;
import com.ibm.wala.fixedpoint.impl.AbstractStatement;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.graph.Acyclic;
import com.ibm.wala.util.intset.IBinaryNaturalRelation;

public class TaskScheduleSolver extends DataflowSolver<ISSABasicBlock, FlowData> {

	public static void solve(IR ir) {
		try {
			System.out.println("=================================================================");
			System.out.println("TaskScheduleSolver: solving method " + ir.getMethod());
			TaskScheduleSolver solver = new TaskScheduleSolver(ir);
			solver.solve((IProgressMonitor)null);			
			NormalNodeFlowData result = (NormalNodeFlowData)solver.getIn(ir.getControlFlowGraph().exit());
			System.out.println("+++ RESULT +++");
			result.print(System.out);
			System.out.println("=================================================================");
		} catch (CancelException e) {			
			//
		}
		
	};
	
	/**
	 * 
	 */
	final SSACFG cfg;
	final IBinaryNaturalRelation backEdges;
	private FlowData entry;
	
	public TaskScheduleSolver(IR ir) {
		super(new BasicFramework<ISSABasicBlock, FlowData>(ir.getControlFlowGraph(), new TransferFunctionProvider()));
		this.cfg = ir.getControlFlowGraph();
		this.backEdges = Acyclic.computeBackEdges(cfg, cfg.entry());
	}

	@Override
	protected FlowData makeEdgeVariable(ISSABasicBlock src, ISSABasicBlock dst) {
		assert src != null;
		assert dst != null;
		
		return backEdges.contains(src.getGraphNodeId(), dst.getGraphNodeId()) ?
				new BackEdgeFlowData(src, dst) : new EdgeFlowData(src, dst);	
	}

	@Override
	protected FlowData makeNodeVariable(ISSABasicBlock n, boolean IN) {
		assert n != null;
		
		NormalNodeFlowData result;
		int predNodeCount = cfg.getPredNodeCount(n);
		if(IN &&  predNodeCount > 1) {
			result = new JoinNodeFlowData(n, predNodeCount);
		} else {
			result = new NormalNodeFlowData(n);
		}
		
//		boolean isLoopHead = false;
//		for(IntPair rel : backEdges) {
//			if(rel.getY() == n.getGraphNodeId()) {
//				isLoopHead = true;
//				break;
//			}
//		}
				
		if (IN && n.equals(cfg.entry())) {
			entry = result;
			result.initEmpty();
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