package xsched.runtime;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class ThreadPool {
	
	class KeepAliveThread extends Thread {		
		public final Semaphore sem = new Semaphore(1);
		
		@Override
		public void run() {
			System.out.println("starting keep alive thread");
			this.sem.acquireUninterruptibly();
			this.sem.release();
			System.out.println("stopping keep alive thread");
			return;
		}		
	}
	
	private KeepAliveThread keepAliveThread;
	
	/** 
	 * Starts a keep alive thread that prevents the JVM from
	 * exiting. This is invoked when new work is submitted to
	 * the pool from the outside. Always executed under the {@link #idleLock}.
	 */
	private void startKeepAliveThread() {
		if(this.keepAliveThread == null) {
			this.keepAliveThread = new KeepAliveThread();
			this.keepAliveThread.sem.acquireUninterruptibly();
			this.keepAliveThread.start();
		}		
	}
	
	/** 
	 * Stops the keep alive thread that prevents the JVM from
	 * exiting.  This is invoked when all threads become
	 * idle.  Always executed under the {@link #idleLock}.
	 */
	void stopKeepAliveThread() {
		if(this.keepAliveThread != null) {
			this.keepAliveThread.sem.release();
			this.keepAliveThread = null;
		}
	}
	
	static class LazyDeque {
		static class ThiefData {
			int head = 0;
		}
		
		static final int INITIAL_SIZE = (1 << 10);
		static final int PAD = 0, OFFSET = 0; 
		
		private AtomicReferenceArray<WorkItem> tasksArray = new AtomicReferenceArray<WorkItem>(size(INITIAL_SIZE));
		
		int ownerHead = 0, ownerTail = 0;
		private final ThiefData thief = new ThiefData();
		
		private int index(int id) {
			return index(this.tasksArray.length() >> PAD, id);
		}
		
		private static int size(int l) {
			return (l << PAD) + OFFSET;
		}
		
		private static int index(int l, int id) {
			return ((id % l) << PAD) + OFFSET;
		}
		
		public void put(Worker owner, WorkItem task) { // Only owner can put.
			assert task != null;
			while(true) {
				final int l = this.tasksArray.length() >> PAD;
				final int tail = this.ownerTail;
				
				if(tail - this.ownerHead >= l || tail == Integer.MAX_VALUE) {
					expand(); // Would be full or would roll-over
					continue;
				}
				
				final int index = index(l, tail);
				this.tasksArray.set(index, task);
				this.ownerTail = tail + 1; // important: put vol. wr. after array store to create HB rel
				
				if(Debug.ENABLED)
					Debug.dequePut(owner, l, this.ownerHead, this.ownerTail, tail, task);				
				return;
			}
		}
		
		public WorkItem take(Worker owner) { 
			// Only owner can take.  Returns either NULL or a WorkItem that should be executed.
			if(this.ownerHead == this.ownerTail)
				return null; // Empty.
			
			// Pop the last item from the deque.
			final int lastTail = this.ownerTail - 1;
			final int lastIndex = index(lastTail);
			
			// Read the item popped.
			//   Note: if we get back null, the item must have been stolen, since otherwise
			//   we never store null into the array, and we know this location was initialized.
			WorkItem item = this.tasksArray.getAndSet(lastIndex, null);
			
			try {
				if(item == null) { // The item we put here was stolen!
					// If this item was stolen, then all previous entries
					// must have been stolen too.  Update our notion of the head of
					// the deque.
					this.ownerHead = this.ownerTail;
					return null; // Deque is now empty.						
				}
				
				this.ownerTail = lastTail;
				return item;
			} finally { 
				if(Debug.ENABLED)
					Debug.dequeTake(owner, this.tasksArray.length() >> PAD, this.ownerHead, this.ownerTail, lastIndex, item);				
			}				
		}
		
		public WorkItem steal(Worker victimWorker, Worker thiefWorker) {
			synchronized(this.thief) { // At most one thief at a time.
				final int head = this.thief.head;
				final int index = index(head);
				WorkItem item = this.tasksArray.getAndSet(index, null);
				
				if(Debug.ENABLED)
					Debug.dequeSteal(victimWorker, thiefWorker, this.thief.head, index, item);
				
				if(item == null) // if null, was either already taken by owner or never there.
					return null; 
				
				this.thief.head++; // Successfully stolen!
				return item;
			}			
		}
		
		public void expand() { // Only owner can expand.
			synchronized(this.thief) { // No thieves are active.
				assert this.ownerHead <= this.thief.head && this.thief.head <= this.ownerTail;
				this.ownerHead = this.thief.head;
				
				int l = this.tasksArray.length() >> PAD, thold = l >> 4;
				int size = (this.ownerTail - this.ownerHead);
				if((l - size) <= thold) { // Less than 1/16 is free.
					replaceTaskArray(l * 2);
				} else if (this.ownerTail == Integer.MAX_VALUE) { // About to roll-over.
					replaceTaskArray(l); 
				}
			}
		}

		private void replaceTaskArray(int size) {
			AtomicReferenceArray<WorkItem> newTasks = new AtomicReferenceArray<WorkItem>(size(size));
			final int l = this.tasksArray.length() >> PAD;
			int j = 0;
			for(int i = this.ownerHead; i < this.ownerTail; i++)
				newTasks.set(index(size, j++), this.tasksArray.get(index(l, i)));
			this.ownerTail = j;
			this.ownerHead = this.thief.head = 0;			
			this.tasksArray = newTasks;
		}
	}
	
	static abstract interface WorkItem {
		abstract void exec(Worker worker);
	}
		
	final class Worker extends Thread {
		final int id;
		final Semaphore semaphore = new Semaphore(1);
		LazyDeque tasks = new LazyDeque();
		
		Worker(int id) {
			super("Intervals-Worker-"+id);
			this.id = id;
		}
		
		@Override
		public String toString(){
			return "("+getName()+")";
		}

		@Override public void run() {
			currentWorker.set(this);
			this.semaphore.acquireUninterruptibly(); // cannot fail
			while(true) {
				try {
					doWork(true);
				} catch (Exception e) {
					//e.printStackTrace();
				} catch (Error e) {
					//e.printStackTrace();
				}
			}
		}
		
		/**
		 * Tries to do some work. 
		 * @param block if true, will block if no work is found, otherwise just returns false
		 * @return true if work was done, false otherwise
		 */
		boolean doWork(boolean block) {
			WorkItem item;			
			if((item = this.tasks.take(this)) == null)
				if((item = stealTask()) == null)
					if((item = findPendingWork(block)) == null)
						return false;
			
			if(Debug.ENABLED)
				Debug.execute(this, item, true);
			item.exec(this);
			return true;
		}

		private WorkItem stealTask() {
			WorkItem item;
			for(int victim = this.id + 1; victim < ThreadPool.this.numWorkers; victim++)
				if((item = stealTaskFrom(victim)) != null)
					return item;
			for(int victim = 0; victim < this.id; victim++)
				if((item = stealTaskFrom(victim)) != null)
					return item;
			return null;
		}
		
		private WorkItem stealTaskFrom(int victimId) {
			Worker victim = ThreadPool.this.workers[victimId];
			WorkItem item = victim.tasks.steal(victim, this);
			return item;
		}
	
		private WorkItem findPendingWork(boolean block) {
			ThreadPool.this.idleLock.lock();
			
			int l = ThreadPool.this.pendingWorkItems.size();
			if(l != 0) {
				WorkItem item = ThreadPool.this.pendingWorkItems.remove(l - 1);
				ThreadPool.this.idleLock.unlock();
				return item;
			} else if (block) {
				ThreadPool.this.idleWorkersExist = true;
				ThreadPool.this.idleWorkers.add(this);
				
				int length = ThreadPool.this.idleWorkers.size();
				if (length == ThreadPool.this.numWorkers) {
					// All workers asleep.
					stopKeepAliveThread();
				}
				
				ThreadPool.this.idleLock.unlock();
				this.semaphore.acquireUninterruptibly(); // blocks until release() is invoked by some other worker
				return null;
			}
			
			ThreadPool.this.idleLock.unlock();
			return null;
		}

		void enqueue(WorkItem item) {
			if(ThreadPool.this.idleWorkersExist) {
				Worker idleWorker = null;
				ThreadPool.this.idleLock.lock();
				try {
					int l = ThreadPool.this.idleWorkers.size();
					if(l != 0) {
						idleWorker = ThreadPool.this.idleWorkers.remove(l - 1);
						ThreadPool.this.idleWorkersExist = (l != 1);
					}
				} finally {				
					ThreadPool.this.idleLock.unlock();
				}
				
				if(idleWorker != null) {
					if(Debug.ENABLED)
						Debug.awakenIdle(this, item, idleWorker);
					idleWorker.tasks.put(idleWorker, item);
					idleWorker.semaphore.release();
					return;
				}
			} 
			if(Debug.ENABLED)
				Debug.enqeue(this, item);
			this.tasks.put(this, item);
		}			
		
	}
	
	final int numWorkers = Runtime.getRuntime().availableProcessors();
	final Worker[] workers = new Worker[this.numWorkers];
	final Worker sentinel = new Worker(-1);
	final static ThreadLocal<Worker> currentWorker = new ThreadLocal<Worker>();
	
	final Lock idleLock = new ReentrantLock();
	final ArrayList<WorkItem> pendingWorkItems = new ArrayList<WorkItem>(); // guarded by idleLock
	final ArrayList<Worker> idleWorkers = new ArrayList<Worker>(); // guarded by idleLock
	volatile boolean idleWorkersExist;
	
	ThreadPool() {
		for(int i = 0; i < this.numWorkers; i++) {
			Worker worker = new Worker(i);
			this.workers[i] = worker;
			worker.setDaemon(true);
		}
		
		for(Worker worker : this.workers)
			worker.start();
	}
	
	Worker currentWorker() {
		return currentWorker.get();
	}
	
	void submit(WorkItem item) {		
		Worker worker = currentWorker();
		if(worker != null)
			worker.enqueue(item);
		else {
			this.idleLock.lock();		
			startKeepAliveThread();
			int l = this.idleWorkers.size();
			if(l == 0) { 
				// No one waiting to take this job.  
				// Put it on the list of pending items, and
				// someone will get to it rather than becoming idle.
				this.pendingWorkItems.add(item);
				if(Debug.ENABLED)
					Debug.enqeue(null, item);
				this.idleLock.unlock();
			}
			else {
				// There is an idle worker.  Remove it, assign 
				// it this job, and wake it.
				worker = this.idleWorkers.remove(l - 1);
				this.idleWorkersExist = (l != 1);
				this.idleLock.unlock();
				if(Debug.ENABLED)
					Debug.awakenIdle(null, item, worker);
				worker.tasks.put(worker, item);
				worker.semaphore.release();
			}					
		}
	}
	
}
