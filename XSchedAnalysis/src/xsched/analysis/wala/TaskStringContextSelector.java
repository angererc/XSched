package xsched.analysis.wala;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Context;
import com.ibm.wala.ipa.callgraph.ContextItem;
import com.ibm.wala.ipa.callgraph.ContextKey;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.annotations.Annotations;

public class TaskStringContextSelector implements ContextSelector {
	
	public static final ContextKey TASK_CREATION_SITE = new ContextKey() {
		@Override
		public String toString() {
			return "TASK_CREATION_SITE_KEY";
		}
	};

	private final ContextSelector base;

	public TaskStringContextSelector(ContextSelector base) {
		this.base = base;
	}

	//do something like: have a base context selector (probably a default one). 
	//ask the base selector for a context
	//check for the task context of the caller and add the tasks up to some length n
	//combine the task string and the base context into a single context
	//pretty much, steal from the nCFAContextSelector and its superclass CallStringContextSelector
	@Override
	public Context getCalleeTarget(CGNode caller, CallSiteReference site, IMethod callee, InstanceKey receiver) {
		Context baseContext = base.getCalleeTarget(caller, site, callee, receiver);
				
		IR ir = caller.getIR();
		if(Annotations.hasAnnotation(callee, TypeName.findOrCreate("Lxsched/TaskMethod"))) {
			 SSAAbstractInvokeInstruction[] invokes = ir.getCalls(site);
			 assert invokes.length == 1;
			 //0 is the "this" of the invoke, 1 is the "now" parameter (1st param)
			 int now = invokes[0].getUse(1);
			 SSAInstruction creationSite = caller.getDU().getDef(now);
			 TaskCreationSite tcs = new TaskCreationSite(caller.getMethod(), (SSANewInstruction)creationSite);
						 
			 return new TaskContextPair(tcs, baseContext);			 
		} else {
			//normal call
			TaskCreationSite tcs = (TaskCreationSite)caller.getContext().get(TASK_CREATION_SITE);
			return new TaskContextPair(tcs, baseContext);			
		}
	}
	
	//the "new" site of a task as a combination of it's def-use
	private static class TaskCreationSite implements ContextItem {
		private final SSANewInstruction creationSite;
		private final IMethod method;
		
		public TaskCreationSite(IMethod method, SSANewInstruction creationSite) {
			assert method != null;
			assert creationSite != null;
			this.method = method;
			this.creationSite = creationSite;
		}
		
		@Override
		public int hashCode() {
			return method.hashCode() * creationSite.hashCode();
		}
		
		@Override
		public boolean equals(Object other) {
			if(other instanceof TaskCreationSite) {
				TaskCreationSite otherTCS = (TaskCreationSite)other;
				return otherTCS.method.equals(method) && otherTCS.creationSite.equals(creationSite);
			} else {
				return false;
			}
		}
	}

	private static class TaskContextPair implements Context {
		private final TaskCreationSite taskCreationSite;

		private final Context base;

		private TaskContextPair(TaskCreationSite cs, Context base) {
			assert base != null;
			this.taskCreationSite = cs;
			this.base = base;
		}

		@Override
		public boolean equals(Object o) {
			if(taskCreationSite == null) {
				return (o instanceof TaskContextPair) && ((TaskContextPair) o).taskCreationSite == null
				&& ((TaskContextPair) o).base.equals(base);
			} else {
				return (o instanceof TaskContextPair) && ((TaskContextPair) o).taskCreationSite.equals(taskCreationSite)
				&& ((TaskContextPair) o).base.equals(base);
			}
		}

		@Override
		public String toString() {
			return "TaskCreationSite: " + taskCreationSite + ":" + base.toString();
		}

		@Override
		public int hashCode() {
			return taskCreationSite == null ? base.hashCode() : taskCreationSite.hashCode() * base.hashCode();
		}

		public ContextItem get(ContextKey name) {
			if (TASK_CREATION_SITE.equals(name)) {
				return taskCreationSite;
			} else {
				return base.get(name);
			}
		}
	};
}
