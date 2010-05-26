package xsched.analysis.schedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Schedule<Context> {

	private HashMap<Context, ActivationNode<Context>> nodesByContext = new HashMap<Context, ActivationNode<Context>>();
	
	private HashMap<ScheduleNode<Context>,List<ScheduleNode<Context>>> outgoingEdges = new HashMap<ScheduleNode<Context>,List<ScheduleNode<Context>>>();
	private HashMap<ScheduleNode<Context>,List<ScheduleNode<Context>>> incomingEdges = new HashMap<ScheduleNode<Context>,List<ScheduleNode<Context>>>();
		
	public final ScheduleNode<Context> enterNode;
	public final ScheduleNode<Context> exitNode;
	
	public final Factory<Context> factory;
	
	public Schedule(Factory<Context> factory) {		        
		this.enterNode = new EnterNode<Context>();
		this.exitNode = new ExitNode<Context>();
		this.factory = factory;
		
		this.addHappensBefore(enterNode, exitNode);
		this.addToWorklist(enterNode);
	}
	
	public ActivationNode<Context> activationNodeForContext(Context context) {
		return nodesByContext.get(context);
	}
		
	public ActivationNode<Context> getOrCreateActivationNode(Context context, Task task) {
			
		ActivationNode<Context> node = nodesByContext.get(context);	
		if(node == null) {			
			node = new ActivationNode<Context>(context, task);
			nodesByContext.put(context, node);
		}
		//in case we had a node, assert that the task is still the same
		assert(node.task.equals(task));
		return node;		
	}
	
	public Heap<Context> resultHeapForNode(ScheduleNode<Context> node) {
		throw new RuntimeException("nyi");
	}
	
	public void setResultHeap(ScheduleNode<Context> node, Heap<Context> heap) {
		throw new RuntimeException("Not yet implemented");
	}
	
	private Set<ScheduleNode<Context>> workingSet = new TreeSet<ScheduleNode<Context>>();
	public void analyze() {
		while(! workingSet.isEmpty()) {
			ScheduleNode<Context> node = workingSet.iterator().next();
			workingSet.remove(node);
			node.analyze(this);
		}
	}
	
	public void nodeChanged(ScheduleNode<Context> node) {
		for(CreationEdge<Context> outgoing : this.outgoingCreationEdges(node)) {
			this.addToWorklist(outgoing.target);
		}
		for(ScheduleNode<Context> outgoing : this.outgoingHBEdges(node)) {
			this.addToWorklist(outgoing);
		}
	}
	
	private void addToWorklist(ScheduleNode<Context> node) {
		workingSet.add(node);
	}
	
	public void addHappensBefore(ScheduleNode<Context> earlier, ScheduleNode<Context> later) {
		List<ScheduleNode<Context>> outgoing = outgoingEdges.get(earlier);
		if(outgoing == null) {
			outgoing = new ArrayList<ScheduleNode<Context>>();
			outgoingEdges.put(earlier, outgoing);
		}
		outgoing.add(later);
		
		List<ScheduleNode<Context>> incoming = incomingEdges.get(later);
		if(incoming == null) {
			incoming = new ArrayList<ScheduleNode<Context>>();
			incomingEdges.put(later, incoming);
		}
		incoming.add(earlier);
	}
	
	private HashMap<ScheduleNode<Context>, List<CreationEdge<Context>>> incomingCreationEdges = new HashMap<ScheduleNode<Context>, List<CreationEdge<Context>>>();
	private HashMap<ScheduleNode<Context>, List<CreationEdge<Context>>> outgoingCreationEdges = new HashMap<ScheduleNode<Context>, List<CreationEdge<Context>>>();
	
	public CreationEdge<Context> addCreationEdge(ScheduleNode<Context> earlier, ScheduleNode<Context> later, P2Set<Context> receivers, List<P2Set<Context>> params) {
		CreationEdge<Context> edge = new CreationEdge<Context>(earlier, later, receivers, params);
		List<CreationEdge<Context>> outgoing = outgoingCreationEdges.get(earlier);
		if(outgoing == null) {
			outgoing = new ArrayList<CreationEdge<Context>>();
			outgoingCreationEdges.put(earlier, outgoing);
		}
		outgoing.add(edge);
		
		List<CreationEdge<Context>> incoming = incomingCreationEdges.get(later);
		if(incoming == null) {
			incoming = new ArrayList<CreationEdge<Context>>();
			incomingCreationEdges.put(later, incoming);
		}
		incoming.add(edge);
		return edge;
	}
	
	public List<CreationEdge<Context>> incomingCreationEdges(ScheduleNode<Context> node) {
		return incomingCreationEdges.get(node);
	}
	
	public List<CreationEdge<Context>> outgoingCreationEdges(ScheduleNode<Context> node) {
		return outgoingCreationEdges.get(node);
	}
	
	public List<ScheduleNode<Context>> incomingHBEdges(ScheduleNode<Context> node) {
		return incomingEdges.get(node);
	}
	
	public List<ScheduleNode<Context>> outgoingHBEdges(ScheduleNode<Context> node) {
		return outgoingEdges.get(node);
	}
}
