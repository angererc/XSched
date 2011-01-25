package xsched.analysis.wala.schedule_extraction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import com.ibm.wala.ssa.ISSABasicBlock;

public final class JoinNodeFlowData extends NormalNodeFlowData {

	private static final boolean DEBUG = false;
	
	private final EdgeFlowData[] incoming;
	
	JoinNodeFlowData(ISSABasicBlock basicBlock, int numIncomingEdges) {
		super(basicBlock);
		//this constructor is called when the initial flow data instances are created; so elements in incoming[] can be null before the meet operation ran
		incoming = new EdgeFlowData[numIncomingEdges];
	}
	
	void initAndMergeFromIncoming(EdgeFlowData[] incoming) {
		this.initEmpty();
		
		assert this.incoming.length == incoming.length;
		
		for(int i = 0; i < incoming.length; i++) {
			this.incoming[i] = incoming[i];
		}
		
		//we do not create a new happensBeforeMap because we have to do the intersection of all
		//and for that we need the null as a flag of "nothing happened yet"		
		if(DEBUG)
			System.out.println("JoinNodeFlowData: joining " + basicBlock);
		for(int i = 0; i < incoming.length; i++) {
			EdgeFlowData edge = incoming[i];
			assert edge != null; //should not happen after we ran the meet operator
			this.mergeState(edge);			
		}
		
		//now we unioned all edges; check that for all edges if an incoming data "knows" about lhs and rhs, it also knows the edge;
		//otherwise they disagree and we can't keep the edge
		
		for(int i = 0; i < this.incoming.length; i++) {
			EdgeFlowData edge = this.incoming[i];			
			this.filterUnreliableEdges(edge);			
		}
	}
	
	@Override
	JoinNodeFlowData duplicate(ISSABasicBlock forBasicBlock) {
		JoinNodeFlowData data = new JoinNodeFlowData(forBasicBlock, incoming.length);
		data.copyState(this);
		return data;
	}
	
	@Override
	public NormalNodeVisitor createNodeVisitor() {
		return new JoinNodeVisitor(this);
	}
	
	EdgeFlowData incomingEdgeAtPosition(int pos) {
		return incoming[pos];
	}
	
	NormalNodeFlowData incomingDataAtPosition(int pos) {
		EdgeFlowData edge = incoming[pos];
		if(edge != null) {
			NormalNodeFlowData data = edge.getData();
			assert ! data.isInitial();
			return data;
		} else {
			return null;
		}
	}
	
	void addPhiVariable(PhiVariable phi, TaskVariable task) {
		if (phiMappings == null)
			phiMappings = new HashMap<PhiVariable, Set<TaskVariable>>();
		
		Set<TaskVariable> tasks = phiMappings.get(phi);
		if(tasks == null) {
			tasks = new HashSet<TaskVariable>();
			phiMappings.put(phi, tasks);
			
		}
		tasks.add(task);		
	}
		
	@Override
	boolean stateEquals(FlowData otherData) {
		return super.stateEquals(otherData);
	}
	
	protected void filterUnreliableEdges(EdgeFlowData edge) { //iterate a copy of my hb edges and check whether the incoming data agrees
		if(!edge.isInitial()) {
			NormalNodeFlowData other = edge.getData();
			
			Iterator<TaskVariable> lhsNodes = this.schedule.iterator();
			while(lhsNodes.hasNext()) {
				TaskVariable lhs = lhsNodes.next();
				
				Iterator<TaskVariable> rhsNodes = this.schedule.getSuccNodes(lhs);
				while(rhsNodes.hasNext()) {
					TaskVariable rhs = rhsNodes.next();
					
					//check for each edge whether the other guy agrees on a) the existence of the task variables and b) on the edge lhs->rhs
					if(other.schedule.containsNode(lhs) && other.schedule.containsNode(rhs)) {
						if(! other.schedule.hasEdge(lhs, rhs))
							//not sure if this throws an concurrent modification exception
							this.schedule.removeEdge(lhs, rhs);
					}
				}
			}			
		}
	}
	
	//called in the constructor
	private void mergeState(EdgeFlowData edge) {
		
		if(!edge.isInitial()) {
			NormalNodeFlowData other = edge.getData();
			assert ! other.isInitial();		
			
			this.loopContexts.addAll(other.loopContexts);
			this.schedule.addAllNodesAndEdges(other.schedule);
			
			if (other.phiMappings != null) {
				for(Entry<PhiVariable, Set<TaskVariable>> entry : other.phiMappings.entrySet()) {
					this.addAllPhiVariables(entry.getKey(), entry.getValue());
				}
			}
			
			//we saw this edge, so add it to the list of loop contexts
			if(edge instanceof BackEdgeFlowData) {
				BackEdgeFlowData backEdge = (BackEdgeFlowData)edge;
				for(LoopContext lc : this.loopContexts) {
					this.loopContexts.add(lc.contextByAddingLoop(backEdge));
				}
			}
		}
		
	}

	@Override
	public void copyState(FlowData v) {
		super.copyState(v);
		assert(v instanceof JoinNodeFlowData);
		JoinNodeFlowData other = (JoinNodeFlowData)v;
		assert other.incoming.length == incoming.length;
		
		for(int i = 0; i < incoming.length; i++) {
			incoming[i] = other.incoming[i];
		}
		
	}
	
	@Override
	public String toString() {
		return "Join-" + super.toString();
	}
}
