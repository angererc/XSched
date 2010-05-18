package xsched.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import soot.jimple.spark.ondemand.DotPointerGraph;
import soot.jimple.spark.pag.AllocNode;
import soot.jimple.spark.pag.FieldRefNode;
import soot.jimple.spark.pag.PAG;
import soot.jimple.spark.pag.VarNode;
import soot.toolkits.scalar.Pair;

public class PAG2DOT {
	private DotPointerGraph dumper = new DotPointerGraph();
	
	private List<Pair<Object,Object>> flattenEdges(Set<Entry<Object,Object>> edges) {
		List<Pair<Object,Object>> result = new ArrayList<Pair<Object,Object>>();
		for(Entry<Object,Object> entry : edges) {
			Object source = entry.getKey();
			Object targets = entry.getValue();
			if(targets instanceof Set<?>) {
				Set<?> targetSet = (Set<?>)targets;
				for(Object target : targetSet) {
					result.add(new Pair<Object,Object>(source, target));
				}
			} else {
				Object[] targetArray = (Object[])targets;
				for(Object target : targetArray) {
					result.add(new Pair<Object,Object>(source, target));
				}
			}
		}
		return result;
	}
	private void buildGraph(PAG pag) {
		for (Pair<Object,Object> edge : flattenEdges(pag.storeEdges().entrySet())) {
			dumper.addStore((VarNode)edge.getO1(), (FieldRefNode)edge.getO2());
		}
		
		for (Pair<Object,Object> edge : flattenEdges(pag.loadEdges().entrySet())) {
			dumper.addLoad((FieldRefNode)edge.getO1(), (VarNode)edge.getO2());
		}
		
		for (Pair<Object,Object> edge : flattenEdges(pag.allocEdges().entrySet())) {
			dumper.addNew((AllocNode)edge.getO1(), (VarNode)edge.getO2());
		}
		
		for (Pair<Object,Object> edge : flattenEdges(pag.simpleEdges().entrySet())) {
			dumper.addAssign((VarNode)edge.getO1(), (VarNode)edge.getO2());
		}
		
	}
	public void dump(PAG pag, String filename) {
		buildGraph(pag);
		dumper.dump(filename);
	}
	
}
