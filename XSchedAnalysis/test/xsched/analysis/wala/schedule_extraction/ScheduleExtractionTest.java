package xsched.analysis.wala.schedule_extraction;

import java.util.Iterator;

import org.junit.Test;
import static org.junit.Assert.*;

import com.ibm.wala.classLoader.IMethod;

import xsched.analysis.core.TaskSchedule;
import xsched.analysis.wala.AnalysisProperties;
import xsched.analysis.wala.WalaScheduleAnalysisDriver;
import xsched.analysis.wala.WalaTaskScheduleManager;


public class ScheduleExtractionTest {

	@Test
	public void testNodeFlowData() throws Exception {
		AnalysisProperties properties = new AnalysisProperties(				
				"xsched/analysis/wala/Exclusions.txt",
				"bin/testclasses/");
		
		WalaScheduleAnalysisDriver driver = new WalaScheduleAnalysisDriver(properties);
			
		driver._1_setUp();
		driver._2_findTaskMethods();
		
		for(IMethod taskMethod : driver.taskMethods()) {
			NormalNodeFlowData flowDat = driver._n_computeNodeFlowData(driver.irForMethod(taskMethod));
			if(taskMethod.getReference().getName().toString().equals("xschedTask_A1")) {
				checkFlowData_A1(flowDat);
			} else if(taskMethod.getReference().getName().toString().equals("xschedTask_A2")) {
				checkFlowData_A2(flowDat);
			} else if(taskMethod.getReference().getName().toString().equals("xschedTask_A3")) {
				checkFlowData_A3(flowDat);
			} else if(taskMethod.getReference().getName().toString().equals("xschedTask_B")) {
				checkFlowData_B(flowDat);
			} else {
				System.err.println("Unknown task method " + taskMethod + " in unit test class " + this);
			}
		}
	}
	
	void checkFlowData_A1(NormalNodeFlowData flowDat) {
//		System.out.println("A1: " + flowDat);
//		flowDat.print(System.out);
		assertEquals(13, flowDat.basicBlock.getGraphNodeId());
		assertEquals(2, flowDat.loopContexts.size());
		//we want {}now, {}4, {}10, and {9->10}10 in the result
		assertEquals(4, flowDat.partialSchedule.getNumberOfNodes());
		//we want phi {}14={}4 and phi {9->10}14 = {}10 || {9->10}10
		assertEquals(2, flowDat.phiMappings.size());
	}
	
	void checkFlowData_A2(NormalNodeFlowData flowDat) {		
//		System.out.println("A2: " + flowDat);
//		flowDat.print(System.out);
		assertEquals(15, flowDat.basicBlock.getGraphNodeId());
		assertEquals(1, flowDat.loopContexts.size());
		//we want {}now, {}10, {}7, and {}14 in the result
		assertEquals(4, flowDat.partialSchedule.getNumberOfNodes());
		//we want phi {}13={}10 || {}7
		assertEquals(1, flowDat.phiMappings.size());
	}
	
	void checkFlowData_A3(NormalNodeFlowData flowDat) {	
//		System.out.println("A3: " + flowDat);
//		flowDat.print(System.out);
		assertEquals(11, flowDat.basicBlock.getGraphNodeId());
		assertEquals(1, flowDat.loopContexts.size());
		//we want {}now, {}4, {}7,
		assertEquals(3, flowDat.partialSchedule.getNumberOfNodes());		
		assertNull(flowDat.phiMappings);
	}
	
	void checkFlowData_B(NormalNodeFlowData flowDat) {
//		System.out.println("B: " + flowDat);
//		flowDat.print(System.out);
		assertEquals(3, flowDat.basicBlock.getGraphNodeId());
		assertEquals(1, flowDat.loopContexts.size());
		//we always expect now
		assertEquals(1, flowDat.partialSchedule.getNumberOfNodes());
		assertNull(flowDat.phiMappings);		
	}
	
	@Test
	public void testTaskSchedule() throws Exception {
		AnalysisProperties properties = new AnalysisProperties(				
				"xsched/analysis/wala/Exclusions.txt",
				"bin/testclasses/");
		
		WalaScheduleAnalysisDriver driver = new WalaScheduleAnalysisDriver(properties);
			
		driver._1_setUp();
		driver._2_findTaskMethods();
		
		for(IMethod taskMethod : driver.taskMethods()) {
			TaskSchedule<Integer, WalaTaskScheduleManager> schedule = driver._n_computeTaskSchedule(driver.irForMethod(taskMethod));
			
			if(taskMethod.getReference().getName().toString().equals("xschedTask_A1")) {
				checkSchedule_A1(schedule);
			} else if(taskMethod.getReference().getName().toString().equals("xschedTask_A2")) {
				checkSchedule_A2(schedule);
			} else if(taskMethod.getReference().getName().toString().equals("xschedTask_A3")) {
				checkSchedule_A3(schedule);
			} else if(taskMethod.getReference().getName().toString().equals("xschedTask_B")) {
				checkSchedule_B(schedule);
			} else {
				System.err.println("Unknown task method " + taskMethod + " in unit test class " + this);
			}
		}
	}
	
	void checkNumberOfNodesAndIterator(TaskSchedule<Integer, WalaTaskScheduleManager> schedule, int expectedFormals, int expectedNonFormals) {
		assertEquals(expectedFormals + expectedNonFormals, schedule.numberOfAllTaskVariables());
		assertEquals(expectedFormals, schedule.numberOfFormalParameterTaskVariables());
		assertEquals(expectedNonFormals, schedule.numberOfNonParameterTaskVariables());
		
		Iterator<Integer> it;
		int count = 0;
		it = schedule.iterateAllTaskVariables();
		while(it.hasNext()) {
			it.next();
			count++;
		}
		assertEquals(expectedFormals + expectedNonFormals, count);
		
		count = 0;
		it = schedule.iterateFormalParameterTaskVariables();
		while(it.hasNext()) {
			it.next();
			count++;
		}
		assertEquals(expectedFormals, count);
		
		count = 0;
		it = schedule.iterateNonParameterTaskVariables();
		while(it.hasNext()) {
			it.next();
			count++;
		}
		assertEquals(expectedNonFormals, count);
	}
	
	void checkSchedule_A1(TaskSchedule<Integer, WalaTaskScheduleManager> schedule) {
		this.checkNumberOfNodesAndIterator(schedule, 1, 2);
		
		assertEquals(1, schedule.actualsForTaskVariable(1).length);
		assertEquals(1, schedule.actualsForTaskVariable(2).length);
		
		//now
		assertEquals(TaskSchedule.Relation.singleton, schedule.relationForTaskVariables(0, 0));
		assertEquals(TaskSchedule.Relation.happensBefore, schedule.relationForTaskVariables(0, 1));
		assertEquals(TaskSchedule.Relation.happensAfter, schedule.relationForTaskVariables(1, 0));
		
		assertEquals(TaskSchedule.Relation.singleton, schedule.relationForTaskVariables(1, 1));
		assertEquals(TaskSchedule.Relation.happensBefore, schedule.relationForTaskVariables(1, 2));
		assertEquals(TaskSchedule.Relation.happensAfter, schedule.relationForTaskVariables(2, 1));
		
		assertEquals(TaskSchedule.Relation.ordered, schedule.relationForTaskVariables(2, 2));
	}
	
	void checkSchedule_A2(TaskSchedule<Integer, WalaTaskScheduleManager> schedule) {
	}
	
	void checkSchedule_A3(TaskSchedule<Integer, WalaTaskScheduleManager> schedule) {
	}
	
	void checkSchedule_B(TaskSchedule<Integer, WalaTaskScheduleManager> schedule) {
	}
}
