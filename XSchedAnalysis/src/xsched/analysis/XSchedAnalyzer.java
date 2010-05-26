package xsched.analysis;

import java.util.HashMap;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SourceLocator;
import soot.Type;
import soot.jimple.spark.builder.ContextInsensitiveBuilder;
import soot.jimple.spark.builder.PAGNodeFactory;
import soot.jimple.spark.pag.AllocNode;
import soot.jimple.spark.pag.Node;
import soot.jimple.spark.pag.PAG;
import soot.jimple.spark.solver.PropIter;
import soot.jimple.spark.solver.Propagator;
import soot.options.SparkOptions;
import soot.toolkits.scalar.Pair;

import xsched.Activation;
import xsched.analysis.schedule.ActivationNode;
import xsched.analysis.schedule.Factory;
import xsched.analysis.schedule.Heap;
import xsched.analysis.schedule.P2Set;
import xsched.analysis.schedule.Schedule;
import xsched.utils.PAG2DOT;

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
	private Schedule<Node> schedule;
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
		sparkOptions = createDefaultSparkOptions();
		
		loadInitialSootClasses(taskMethodSignature);
		createPAG();
		
		initSchedule(taskMethodSignature);
		
		new PAG2DOT().dump(pag, SourceLocator.v().getOutputDir() + "/before.dot");
		propagator = new PropIter(pag);
		
		schedule.analyze();		
	}
	
	private void initSchedule(String taskMethodSignature)
    {
		this.schedule = new Schedule<Node>(new Factory<Node>() {
			@Override
			public Heap<Node> newHeap() {
				throw new RuntimeException("nyi");
			}

			@Override
			public P2Set<Node> newP2Set() {
				throw new RuntimeException("nyi");
			}
        	
        });
        
		//create the main activation
		SootMethod mainTask = Scene.v().getMethod(taskMethodSignature);
		AllocNode mainActivationAlloc = PAGNodeFactory.v().makeAllocNode(new Pair<XSchedAnalyzer,String>(this,"initialActivation"), ACTIVATION_TYPE, null);
        ActivationNode<Node> mainActivation = this.schedule.getOrCreateActivationNode(mainActivationAlloc, null /* fixme, dont' use null but some wrapped soot method */);
        
        //create the main "this"; could be the mainActivationAlloc in the future, if I allow static tasks
        //AllocNode mainInstance = PAGNodeFactory.v().makeAllocNode(new Pair<XSchedAnalyzer,String>(this,"initialInstance"), mainTask.getDeclaringClass().getType(), null);        
        this.schedule.addCreationEdge(this.schedule.enterNode, mainActivation, null, null /*TODO: fixme; don't use null but a newly created alloc node in a p2 set*/);
                
        //update the call graph
        pag.getOnFlyCallGraph().build();
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
