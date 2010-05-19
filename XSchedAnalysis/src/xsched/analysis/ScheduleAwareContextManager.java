package xsched.analysis;

import soot.Context;
import soot.Kind;
import soot.MethodContext;
import soot.MethodOrMethodContext;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.ContextManager;
import soot.jimple.toolkits.callgraph.Edge;

public class ScheduleAwareContextManager implements ContextManager {
    private CallGraph cg;

    //set the context manager in CallGraphBuilder.java
    public ScheduleAwareContextManager( CallGraph cg ) {
        this.cg = cg;
    }

    public void addStaticEdge( MethodOrMethodContext src, Unit srcUnit, SootMethod target, Kind kind ) {
        cg.addEdge( new Edge( src, srcUnit, MethodContext.v( target, srcUnit ), kind ) );
    }

    public void addVirtualEdge( MethodOrMethodContext src, Unit srcUnit, SootMethod target, Kind kind, Context typeContext ) {
        cg.addEdge( new Edge( src, srcUnit, MethodContext.v( target, srcUnit ), kind ) );
    }

    public CallGraph callGraph() { return cg; }
}
