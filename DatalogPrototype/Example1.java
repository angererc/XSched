import java.util.Vector;

class Example1 {
    Vector<Object> out;
    
    public void process(Object data) {
        Activation.now.result = new Object();
    }
    public void write(Activation processActivation) {
        out.add(processActivation.result); 
    }    
    public void writeToOut(Vector<Object> input) {
        Activation lastProcess = Activation.now;
        Activation lastWrite = Activation.now;
        
        for(Object data : input) {
            Activation process = new Activation(this, "process", data);
            Activation write = new Activation(this, "write", process);
    
            lastProcess.hb(process);
            process.hb(write);
            lastWrite.hb(write);
            
            lastProcess = process;
            lastWrite = write;
        }
    }
}