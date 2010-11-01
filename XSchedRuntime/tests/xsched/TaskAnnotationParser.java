package xsched;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskAnnotationParser {

	private HashMap<String, Activation> activations = new HashMap<String, Activation>();
	private ArrayList<Activation> unconditional = new ArrayList<Activation>();
	private ArrayList<Implication> implications = new ArrayList<Implication>();
	private ArrayList<Arrow> arrows = new ArrayList<Arrow>();
	
	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("@Task(\n");
		buff.append("activations:\n");
		for(Activation a : activations.values()) {
			buff.append("\t");
			buff.append(a);
			buff.append(",\n");
		}
		buff.append("unconditional:\n");
		for(Activation a : unconditional) {
			buff.append("\t");
			buff.append(a.shortName());
			buff.append(",\n");
		}
		buff.append("implications:\n");
		for(Implication i : implications) {
			buff.append("\t");
			buff.append(i);
			buff.append(",\n");
		}
		buff.append("schedule:\n");
		for(Arrow a : arrows) {
			buff.append("\t");
			buff.append(a);
			buff.append(",\n");
		}
		
		return buff.toString();
	}
	
	public TaskAnnotationParser(Task t) {
		parseActivations(components(t.activations()));
		parseImplications(components(t.implications()));	
		parseSchedule(components(t.schedule()));
	}
	
	private String[] components(String str) {
		String[] result = str.split(",");
		for(int i = 0; i < result.length; i++) {
			result[i] = result[i].trim();
		}
		return result;
	}
	
	private void parseActivations(String[] components) {
		for(String c : components) {
			String[] namePatternTask = c.split(":");
			assert(namePatternTask.length == 4) : "Expected format 'A:Param1*Param2:Pattern:some.Class.taskMethod()'";
			Activation a = getOrCreateActivation(namePatternTask[0].trim());
			assert(a.pattern == null) : "Activation with name " + a.name + " was defined multiple times.";
			a.setPattern(namePatternTask[2]);
			a.setTask(namePatternTask[3]);
			
			String[] params = namePatternTask[1].split("\\*");
			Node[] nodes = new Node[params.length];
			for(int i = 0; i < params.length; i++) {
				nodes[i] = getOrCreateActivationOrParam(params[i]);
			}
			a.setParams(nodes);
			this.activations.put(a.name, a);
		}
	}
	
	private void parseImplications(String[] components) {
		for(String c : components) {
			if(c.contains("<=>")) {
				String[] as = c.split("<=>");
				Activation lhs = getActivation(as[0]);
				Activation rhs = getActivation(as[1]);
				this.implications.add(new Implication(lhs, rhs));
				this.implications.add(new Implication(rhs, lhs));
			} else if (c.contains("=>")) {
				String[] as = c.split("=>");
				Activation lhs = getActivation(as[0]);
				Activation rhs = getActivation(as[1]);
				this.implications.add(new Implication(lhs, rhs));
			} else {
				Activation a = getActivation(c);
				this.unconditional.add(a);
			}
		}		
	}
		
	private void parseSchedule(String[] components) {
		for(String c : components) {
			String[] as = c.split("->");
			Node lhs = getActivationOrParam(as[0]);
			Node rhs = getActivationOrParam(as[1]);
			arrows.add(new Arrow(lhs, rhs));
		}
		
	}
	
	private Activation getOrCreateActivation(String name) {
		Activation a = activations.get(name.trim());
		if(a == null) {
			a = new Activation(name);
			activations.put(name.trim(), a);
		}
		return a;
	}
	
	private Activation getActivation(String name) {
		Activation a = activations.get(name.trim());
		assert(a!=null) : "No activation with name " + name;
		return a;
	}
	
	private Node getOrCreateActivationOrParam(String name) {
		try {
			int param = Integer.parseInt(name);
			return new Param(param);
		} catch (NumberFormatException ex) {
			return getOrCreateActivation(name);
		}
	}
	
	private Node getActivationOrParam(String name) {
		try {
			int param = Integer.parseInt(name);
			return new Param(param);
		} catch (NumberFormatException ex) {
			return getActivation(name);
		}
	}
	/*
	 * 
	 */
	
	public static abstract class Node {
		public abstract String shortName();
	}
	
	public static class Param extends Node {
		public final int num;
		public Param(int num) {
			this.num = num;
		}
		public String shortName() {
			return ""+ num;
		}
	}
	public static class Activation extends Node {
		private final String name;
		private Node[] params;
		private Pattern pattern;
		private String task;
		
		public Activation(String name) {
			this.name = name;			
		}
		
		public String shortName() {
			return name;
		}
		
		public void setTask(String task) {
			this.task = task;
		}
		
		public void setPattern(String pattern) {
			if(pattern.equals("Singleton")) {
				this.pattern = Pattern.SINGLETON;
			} else if(pattern.equals("FwdChain")) {
				this.pattern = Pattern.FWD_CHAIN;
			} else if(pattern.equals("BckwdChain")) {
				this.pattern = Pattern.BCKWD_CHAIN;
			} else if(pattern.equals("Ordered")) {
				this.pattern = Pattern.ORDERED;
			} else if(pattern.equals("Unordered")) {
				this.pattern = Pattern.UNORDERED;
			} else {
				throw new RuntimeException("Unknown pattern: " + pattern);
			}
		}
		
		public void setParams(Node[] params) {
			this.params = params;
		}
		
		public String toString() {
			StringBuffer buff = new StringBuffer();
			buff.append(name);
			buff.append(":");
			boolean first = true;
			for(Node param : params) {
				if(!first)
					buff.append("*");				
				first = false;
				buff.append(param.shortName());
				
			}
			buff.append(":");
			buff.append(pattern);
			buff.append(":");
			buff.append(task);
			return buff.toString();
			
		}
		
		//activation annotations are equal by name because there can only be one activation with a given name per annotation
		@Override
		public boolean equals(Object other) {
			if(other instanceof Activation) {				
				return this.name.equals(((Activation)other).name);
			} else {
				return false;
			}
		}
		@Override
		public int hashCode() {
			return name.hashCode();
		}
	}
	
	public static class Implication {
		public final Activation lhs;
		public final Activation rhs;
		public Implication(Activation lhs, Activation rhs) {
			this.lhs = lhs;
			this.rhs = rhs;
		}
		public String toString() {
			return lhs.shortName() + "=>" + rhs.shortName();
		}
	}
	
	public static class Arrow {
		public final Node lhs;
		public final Node rhs;
		public Arrow(Node lhs, Node rhs) {
			this.lhs = lhs;
			this.rhs = rhs;
		}
		
		public String toString() {
			return lhs.shortName() + "->" + rhs.shortName();
		}
	}
	
	public static enum Pattern {
		SINGLETON, FWD_CHAIN, BCKWD_CHAIN, ORDERED, UNORDERED
	}
	
}
