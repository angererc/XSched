package xsched.analysis;

import java.util.HashMap;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.jimple.spark.builder.ContextInsensitiveBuilder;
import soot.jimple.spark.pag.AllocNode;
import soot.jimple.spark.pag.PAG;
import soot.options.SparkOptions;

import xsched.Activation;
import xsched.analysis.schedule.Schedule;
import xsched.analysis.schedule.ScheduleNode;

public class XSchedAnalyzer {

	private SparkOptions sparkOptions;
	private Schedule schedule;
	private PAG pag;
	
	static {
		soot.options.Options.v().set_keep_line_number(true);
		soot.options.Options.v().set_whole_program(true);
	}
	
	public void analyzeMainActivation(String taskMethodSignature) {
				
		sparkOptions = createDefaultSparkOptions();
		
		loadInitialSootClasses(taskMethodSignature);
		
		internalAnalyze(taskMethodSignature);
	}
	
	private void internalAnalyze(String taskMethodSignature)
    {
		SootMethod initialTask = Scene.v().getMethod(taskMethodSignature);
		
        // Build pointer assignment graph
        ContextInsensitiveBuilder b = new ContextInsensitiveBuilder();
        pag = b.setup(sparkOptions);
        this.schedule = new Schedule(pag);
        
        Type activationType = Scene.v().getRefType(Activation.class.getName());
        AllocNode enter = pag.makeAllocNode(this, activationType, null);
        ScheduleNode enterNode = this.schedule.addNode(enter, initialTask);
        
        this.schedule.addHappensBefore(enterNode, this.schedule.exitNode);
        
        b.build();
        System.out.println(pag);
    }
	
	private void loadInitialSootClasses(String taskMethodSignature) {
		//soot default classes
		
		//our stuff
		SootClass c;
		//we use Activation...
		String activationClassName = Activation.class.getName();
		c = Scene.v().loadClassAndSupport(activationClassName);
		c.setApplicationClass();
		
		//.. and the main task class
		String mainTaskClassName = Scene.v().signatureToClass(taskMethodSignature);
		c = Scene.v().loadClassAndSupport(mainTaskClassName);
		c.setApplicationClass();
				
		soot.Scene.v().loadNecessaryClasses();
	}
	
	private SparkOptions createDefaultSparkOptions() {
		HashMap<String, String> opt = new HashMap<String, String>();
		opt.put("enabled","true");
		opt.put("verbose","true");      
		opt.put("simple-edges-bidirectional","false");
		opt.put("on-fly-cg","true");            		
		opt.put("propagator","iter");
		opt.put("set-impl","double");
		opt.put("double-set-old","hybrid");         
		opt.put("double-set-new","hybrid"); 
		opt.put("cs-demand", "true");
		opt.put("traversal", "100");
		opt.put("passes", "10");
		
		return new SparkOptions(opt);
	}
}
