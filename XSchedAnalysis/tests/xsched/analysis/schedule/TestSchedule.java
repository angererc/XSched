package xsched.analysis.schedule;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import xsched.analysis.heap.ActivationsTree;
import xsched.analysis.heap.NewActivationRecord;
import xsched.analysis.heap.ParallelActivationsTree;


public class TestSchedule {

	@Test
	public void scheduleCreation() {
		Schedule<Integer> schedule = new Schedule<Integer>(new Factory<Integer>(){
			@Override
			public Heap<Integer> newHeap() {
				return null;
			}

			@Override
			public P2Set<Integer> newP2Set() {
				return null;
			}});
		//enter->exit
		List<ScheduleNode<Integer>> enterOut = schedule.outgoingHBEdges(schedule.enterNode);
		assertEquals(1, enterOut.size());
		assertTrue(enterOut.contains(schedule.exitNode));
		
		List<ScheduleNode<Integer>> exitIn = schedule.incomingHBEdges(schedule.exitNode);
		assertEquals(1, exitIn.size());
		assertTrue(exitIn.contains(schedule.enterNode));
		
		assertNull(schedule.outgoingCreationEdges(schedule.enterNode));
		
		//
		Task task = new Task(){
			@Override
			public int numParams() {
				return 0;
			}};
		ActivationNode<Integer> a1 = schedule.getOrCreateActivationNode(1, task);
		
		//don't create new nodes for the same context
		assertTrue(a1 == schedule.getOrCreateActivationNode(1, task));
				
		//some basic adding through the ActivationsTree mechanism
		ParallelActivationsTree<Integer> newActivations = new ParallelActivationsTree<Integer>(99);
		
		NewActivationRecord<Integer> new1 = new NewActivationRecord<Integer>(2);
		new1.task = task;
		NewActivationRecord<Integer> new2 = new NewActivationRecord<Integer>(3);
		new2.task = task;
		NewActivationRecord<Integer> new2b = new NewActivationRecord<Integer>(3);
		new2b.task = task;
		newActivations.addChild(new1);
		newActivations.addChild(new2);
		newActivations.addChild(new2b);
		
		newActivations.addCreationEdgesToSchedule(schedule, a1);
		
		List<CreationEdge<Integer>> creationEdges = schedule.outgoingCreationEdges(a1);
		assertEquals(3, creationEdges.size());
		assertEquals(new Integer(2), ((ActivationNode<Integer>)creationEdges.get(0).target).context);
		
	}
	
}
