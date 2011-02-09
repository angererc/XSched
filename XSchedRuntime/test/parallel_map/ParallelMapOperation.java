package parallel_map;

import java.util.Vector;
import xsched.Task;
import xsched.runtime.RTTask;

public class ParallelMapOperation {
	Vector<String> out = new Vector<String>();
    
    public void xschedTask_process(Task<String> now, Object data) {
    	System.out.println("processing " + data);
    	try {
			Thread.sleep(1);
		} catch (InterruptedException e) {}
        now.setResult(data.toString());
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
            //this.xschedTask_process(process = new Task<String>(), data);
            process = new RTTask<String>(this, "xschedTask_process", data);
            Task<Void> write;
            //this.xschedTask_write(write = new Task<Void>(), process);
            write = new RTTask<Void>(this, "xschedTask_write", process);
            
            lastProcess.hb(process);
            process.hb(write);
            lastWrite.hb(write);
            
            lastProcess = process;
            lastWrite = write;
        }
    }
    
    public static void main(String[] args) {
    	Vector<Object> v = new Vector<Object>();
    	for(int i = 0; i < 100; i++) {
    		v.add(new Integer(i));
    	}
    	v.add(new Boolean(false));
    	v.add(new String("Hello world!"));
    	
    	ParallelMapOperation map = new ParallelMapOperation();
    	//map.xschedMainTask_writeToOut(new Task<Void>(), v);
    	new RTTask<Void>(map, "xschedMainTask_writeToOut", v);
    	
    	System.out.println(map.out);
    }
}