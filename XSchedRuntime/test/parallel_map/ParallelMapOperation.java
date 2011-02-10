package parallel_map;

import java.util.Vector;
import xsched.Task;

public class ParallelMapOperation {
	Vector<String> out = new Vector<String>();
    
    public void xschedTask_process(Task<String> now, Object data) {
    	System.out.println("processing " + data);
    	try {
			Thread.sleep(100);
		} catch (InterruptedException e) {}
        now.setResult(data.toString());
        if(data.equals(15)) {
        	System.out.println("error about to happen");
        }
    }
    
    public void xschedTask_write(Task<Void> now, Task<String> processTask) {
    	try {
			Thread.sleep(100);
		} catch (InterruptedException e) {}
    	System.out.println("Writing data " + processTask.result());
        out.add(processTask.result()); 
    }    
    
    public void xschedMainTask_writeToOut(Task<Void> now, Vector<Object> input) {
        Task<?> lastProcess = now;
        Task<?> lastWrite = now;
        
        for(Object data : input) {
            Task<String> process;
            this.xschedTask_process(process = new Task<String>(), data);
            //xsched.Runtime.scheduleNormalTask(this, "xschedTask_process", new Object[] {process = new Task<String>(), data});
            
            Task<Void> write;
            this.xschedTask_write(write = new Task<Void>(), process);
            //xsched.Runtime.scheduleNormalTask(this, "xschedTask_write", new Object[] { write = new Task<Void>(), process});
            
            lastProcess.hb(process);
            process.hb(write);
            lastWrite.hb(write);
            
            lastProcess = process;
            lastWrite = write;
        }
    }
    
    public static void main(String[] args) {
    	System.out.println("Starting ParallelMapOperation");
    	Vector<Object> v = new Vector<Object>();
    	for(int i = 0; i < 100; i++) {
    		v.add(new Integer(i));
    	}
    	//v.add(new Integer(15));
    	//v.add(new Boolean(false));
    	v.add(new String("Hello world!"));
    	
    	ParallelMapOperation map = new ParallelMapOperation();
    	map.xschedMainTask_writeToOut(new Task<Void>(), v);
    	//xsched.Runtime.scheduleMainTask(map, "xschedMainTask_writeToOut", new Object[] {new Task<Void>(), v});
    	
    	System.out.println(map.out);
    }
}