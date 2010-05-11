package xsched;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xsched.ThreadPool.WorkItem;
import xsched.ThreadPool.Worker;

class Debug {
	
	public static final boolean ENABLED = true;
	public static final boolean DUMP_IMMEDIATELY = true;

	public static final boolean ENABLED_WAIT_COUNTS = true;	 /** Debug statements related to wait counts. */
	public static final boolean ENABLED_SCHEDULE = true;       /** Debug statements related to higher-level scheduling */
	public static final boolean ENABLED_WORK_STEAL = false;  /** Debug statements related to the work-stealing queue */
	
	public static List<Event> events = Collections.synchronizedList(new ArrayList<Event>());
	
	public static void addEvent(Event e) {
		assert ENABLED; // all debug actions should be protected by if(ENABLED)
		if(ENABLED) { // just in case.
			if(DUMP_IMMEDIATELY) {
				synchronized(events) {
					System.err.println(e.toString());
				}
			} else
				events.add(e);
		}
	}
	
	public static void dump() {
		synchronized(events) {
			for(Event e : events)
				System.err.println(e.toString());
			events.clear();
		}
	}

	static abstract class Event {
		/* empty event, fill in the details*/
	}

	/*
	 * SCHEDULE events
	 */
	static class NewActivationEvent extends Event {
		public final Activation<?> activation;

		public NewActivationEvent(Activation<?> inter) {
			this.activation = inter;
		}
		
		@Override
		public String toString() {
			return String.format("NEW %s", this.activation);
		}
	}
	
	public static void newActivation(Activation<?> activation) {
		if(ENABLED_SCHEDULE)
			addEvent(new NewActivationEvent(activation));
	}

	static class ActivationStateChangeEvent extends Event {
		public final Activation<?> activation;
		public final int retainCount;
		public final String state;
		
		public ActivationStateChangeEvent(Activation<?> inter) {
			this.activation = inter;
			this.retainCount = ((Activation<?>)inter).retainCount();
			if(inter.isInFuture()) {
				this.state = "in future";
			} else if (inter.isAboutToExecute()) {
				this.state = "about to execute";
			} else if (inter.isExecuting()) {
				this.state = "executing";
			} else if (inter.hasRetired()) {
				this.state = "retired";
			} else {
				throw new Error("activation is stateless?!?");
			}
			
		}
		
		@Override
		public String toString() {
			return String.format("ActivationStateChange act=%s, retainCount=%s, state=%s", this.activation, this.retainCount, this.state);
		}
	}
	
	public static void activationStateChange(Activation<?> activation) {
		if(ENABLED_SCHEDULE)
			addEvent(new ActivationStateChangeEvent(activation));
	}
	
	/*
	 * ThreadPool Events
	 */
	static class EnqueueEvent extends Event {
		public final Worker enqueueWorker;
		public final WorkItem item;
		
		public EnqueueEvent(Worker enqueueWorker, WorkItem item) {
			this.enqueueWorker = enqueueWorker;
			this.item = item;
		}
		
		@Override
		public String toString() {
			return String.format("ENQUEUE %s item=%s", this.enqueueWorker, this.item);
		}
	}
	
	public static void enqeue(Worker worker, WorkItem item) {
		if(ENABLED_WORK_STEAL)
			addEvent(new EnqueueEvent(worker, item));
	}

	static class DequePutEvent extends Event {
		public final Worker owner;
		public final int l, ownerHead, ownerTail, taskIndex;
		public final WorkItem task;

		public DequePutEvent(Worker owner, int l, int ownerHead, int ownerTail,
				int taskIndex, WorkItem task) {
			super();
			this.owner = owner;
			this.l = l;
			this.ownerHead = ownerHead;
			this.ownerTail = ownerTail;
			this.task = task;
			this.taskIndex = taskIndex;
		}



		@Override
		public String toString() {
			return String.format("DEQUE_PUT %s l=%d owner=%d-%d tasks[%d]=%s", 
					this.owner, this.l, this.ownerHead, this.ownerTail, this.taskIndex, this.task);
		}
	}

	public static void dequePut(Worker owner, int l, int ownerHead, int ownerTail,
			int taskIndex, WorkItem task) {
		if(ENABLED_WORK_STEAL)
			addEvent(new DequePutEvent(owner, l, ownerHead, ownerTail, taskIndex, task));
	}

	static class DequeTakeEvent extends Event {
		public final Worker owner;
		public final int l, ownerHead, ownerTail, lastIndex;
		public final WorkItem task;

		public DequeTakeEvent(Worker owner, int l, int ownerHead, int ownerTail,
				int lastIndex, WorkItem task) {
			super();
			this.owner = owner;
			this.l = l;
			this.ownerHead = ownerHead;
			this.ownerTail = ownerTail;
			this.lastIndex = lastIndex;
			this.task = task;
		}

		@Override
		public String toString() {
			return String.format("DEQUE_TAKE %s l=%d owner=%d-%d tasks[%d]=%s",
					this.owner, this.l, this.ownerHead, this.ownerTail, this.lastIndex, this.task);
		}
	}

	public static void dequeTake(Worker owner, int l, int ownerHead, int ownerTail,
			int lastIndex, WorkItem task) {
		if(ENABLED_WORK_STEAL)
			addEvent(new DequeTakeEvent(owner, l, ownerHead, ownerTail, lastIndex, task));
	}
	
	static class DequeStealEvent extends Event {
		public final Worker victimWorker, thiefWorker;
		public final int thiefHead;
		public final int taskIndex;
		public final WorkItem task;

		public DequeStealEvent(Worker victimWorker, Worker thiefWorker,
				int thiefHead, int taskIndex, WorkItem task) {
			super();
			this.victimWorker = victimWorker;
			this.thiefWorker = thiefWorker;
			this.thiefHead = thiefHead;
			this.taskIndex = taskIndex;
			this.task = task;
		}

		@Override
		public String toString() {
			return String.format("DEQUE_STEAL %s thief=%s head=%d tasks[%d]=%s",
					this.victimWorker, this.thiefWorker, this.thiefHead, this.taskIndex, this.task);
		}
	}
	
	public static void dequeSteal(Worker victimWorker, Worker thiefWorker, int thiefHead, int taskIndex, WorkItem task) {
		if(ENABLED_WORK_STEAL)
			addEvent(new DequeStealEvent(victimWorker, thiefWorker, thiefHead, taskIndex, task));
	}
	
	static class ExecuteEvent extends Event {
		public final Worker worker;
		public final WorkItem item;
		public final boolean started;
		
		public ExecuteEvent(Worker worker, WorkItem item, boolean started) {
			this.worker = worker;
			this.item = item;
			this.started = started;
		}
		
		@Override
		public String toString() {
			return String.format("EXECUTE %s started=%s item=%s", this.worker, this.started, this.item);
		}
	}
	
	public static void execute(Worker worker, WorkItem item, boolean started) {
		if(ENABLED_WAIT_COUNTS)
			addEvent(new ExecuteEvent(worker, item, started));
	}
	
	static class AwakenIdleEvent extends Event {
		public final Worker awakenedByWorker;
		public final WorkItem workItem;
		public final Worker idleWorker;
		
		public AwakenIdleEvent(Worker awakenWorker, WorkItem workItem, Worker idleWorker) {
			this.awakenedByWorker = awakenWorker;
			this.workItem = workItem;
			this.idleWorker = idleWorker;
		}
		
		@Override
		public String toString() {
			return String.format("AWAKEN_IDLE %s workItem=%s awakenedBy=%s", this.idleWorker, this.workItem, this.awakenedByWorker);
		}
	}
	
	public static void awakenIdle(Worker worker, WorkItem workItem,
			Worker idleWorker) {
		if(ENABLED_WORK_STEAL)
			addEvent(new AwakenIdleEvent(worker, workItem, idleWorker));
	}
	
}
