package xsched.analysis;

import java.util.Collection;
import java.util.HashMap;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SourceLocator;
import soot.Type;
import soot.jimple.spark.builder.ContextInsensitiveBuilder;
import soot.jimple.spark.pag.AllocNode;
import soot.jimple.spark.pag.PAG;
import soot.jimple.spark.pag.PAGDumper;
import soot.jimple.spark.solver.PropIter;
import soot.jimple.spark.solver.Propagator;
import soot.options.SparkOptions;
import soot.toolkits.scalar.Pair;

import xsched.Activation;
import xsched.analysis.schedule.Heap;
import xsched.analysis.schedule.Schedule;
import xsched.analysis.schedule.ScheduleNode;
import xsched.analysis.schedule.Heap.NewActivationRecord;
import xsched.analysis.schedule.Heap.NewHBRelationshipRecord;

/*
 * the XSchedAnalyzer is modelled after the SparkTransformer
 */
public class XSchedAnalyzer {

	public static final Type ACTIVATION_TYPE;
	public static final SootMethod HB_METHOD;
	public static final SootMethod ACTIVATION_CONSTRUCTOR0;
	public static final SootMethod ACTIVATION_CONSTRUCTOR1;
	public static final SootMethod ACTIVATION_CONSTRUCTOR2;
	public static final SootMethod ACTIVATION_CONSTRUCTOR3;
	
	private SparkOptions sparkOptions;
	private PAG pag;
	private Schedule schedule;
	protected String outputDir;
	private Propagator propagator;
	
	static {
		soot.options.Options.v().set_keep_line_number(true);
		soot.options.Options.v().set_whole_program(true);
		
		String activationClassName = Activation.class.getName();
		SootClass c = Scene.v().loadClassAndSupport(activationClassName);
		c.setApplicationClass();
		
		ACTIVATION_TYPE = Scene.v().getRefType(Activation.class.getName());
		HB_METHOD = Scene.v().getMethod("<xsched.Activation: void hb(xsched.Activation)>");
		ACTIVATION_CONSTRUCTOR0 = Scene.v().getMethod("<xsched.Activation: void <init>(java.lang.Object,java.lang.String)>");
		ACTIVATION_CONSTRUCTOR1 = Scene.v().getMethod("<xsched.Activation: void <init>(java.lang.Object,java.lang.String,java.lang.Object)>");
		ACTIVATION_CONSTRUCTOR2 = Scene.v().getMethod("<xsched.Activation: void <init>(java.lang.Object,java.lang.String,java.lang.Object,java.lang.Object)>");
		ACTIVATION_CONSTRUCTOR3 = Scene.v().getMethod("<xsched.Activation: void <init>(java.lang.Object,java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object)>");
	}
	
	public void analyzeMainActivation(String taskMethodSignature) {
		outputDir = SourceLocator.v().getOutputDir();
		sparkOptions = createDefaultSparkOptions();
		
		loadInitialSootClasses(taskMethodSignature);
		createPAG();
		ScheduleNode enterNode = initSchedule(taskMethodSignature);
		
		new PAGDumper( pag, outputDir ).dump();
		propagator = new PropIter(pag);
		analyzeScheduleNode(enterNode);
	}
	
	private void analyzeScheduleNode(ScheduleNode node) {
		PAG incomingPAG = pag;
		propagator.setPAG(incomingPAG);
		Heap resultHeap = new Heap(incomingPAG);
		
		propagator.propagate();
		
		propagator.donePropagating();
		
		node.setResultHeap(resultHeap);
		
		Pair<Collection<NewActivationRecord>, Collection<NewHBRelationshipRecord>> newDeclarations = resultHeap.findNewHBDeclarations();
		
		System.out.println(newDeclarations.getO1());
		System.out.println(newDeclarations.getO2());
	}
	
	private ScheduleNode initSchedule(String taskMethodSignature)
    {
		SootMethod initialTask = Scene.v().getMethod(taskMethodSignature);
		
        this.schedule = new Schedule(pag);
        
        AllocNode enter = pag.makeAllocNode(this, ACTIVATION_TYPE, null);
        ScheduleNode enterNode = this.schedule.addNode(enter, initialTask);
        
        this.schedule.addHappensBefore(enterNode, this.schedule.exitNode);
        
        //update the call graph
        pag.getOnFlyCallGraph().build();
        return enterNode;
    }
	
	private void createPAG() {
		// Build pointer assignment graph
        ContextInsensitiveBuilder b = new ContextInsensitiveBuilder();
        pag = b.setup(sparkOptions);
        b.build();
        
        pag.getTypeManager().makeTypeMask();
        //see SparkTransformer for more cleaning options
        pag.cleanUpMerges();
                
	}
	
	private void loadInitialSootClasses(String taskMethodSignature) {
		//soot default classes
		
		//our stuff
		SootClass c;
				
		//.. and the class of the main task
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
