package xsched;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class XSchedThreadPoolExecutor extends ThreadPoolExecutor {

	private final AtomicInteger executing = new AtomicInteger(0);
	
	public XSchedThreadPoolExecutor(int coorPoolSize, int maxPoolSize, long keepAliveTime,
			TimeUnit seconds, BlockingQueue<Runnable> queue) {
		super(coorPoolSize, maxPoolSize, keepAliveTime, seconds, queue);
	}

	
	@Override
	public void execute(Runnable command) {
		executing.incrementAndGet();
		super.execute(command);
	}


	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		super.afterExecute(r, t);
		int count = executing.decrementAndGet();
		if(count == 0) {
			this.shutdown();
		}
	}
	
}
