package xsched.tests._1;
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
        Activation<?> lastProcess = Activation.now();
        Activation<?> lastWrite = Activation.now();
        
        for(Object data : input) {
            Activation<Object> process = Activation.schedule(this, "process(Ljava/lang/Object;)Ljava/lang/Object;", data);
            Activation<Void> write = Activation.schedule(this, "write(Lxsched/Activation;)V;", process);
    
            lastProcess.hb(process);
            process.hb(write);
            lastWrite.hb(write);
            
            lastProcess = process;
            lastWrite = write;
        }
    }
}
