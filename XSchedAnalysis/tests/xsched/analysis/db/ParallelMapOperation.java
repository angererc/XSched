package xsched.analysis.db;
import java.util.Vector;
import xsched.Activation;

public class ParallelMapOperation {
	Vector<Object> out;
    
    public Object process(Object data) {
        return new Object();
    }
    public void write(Activation<Object> processActivation) {
        out.add(processActivation.result()); 
    }    
    public void writeToOut(Vector<Object> input) {
        Activation<Object> lastProcess = Activation.now();
        Activation<Void> lastWrite = Activation.now();
        
        for(Object data : input) {
            Activation<Object> process = Activation.after(this, "process(Ljava/lang/Object;)Ljava/lang/Object;", data);
            Activation<Void> write = Activation.after(this, "write(L/xsched/Activation;)V;", process);
    
            lastProcess.hb(process);
            process.hb(write);
            lastWrite.hb(write);
            
            lastProcess = process;
            lastWrite = write;
        }
    }
}
