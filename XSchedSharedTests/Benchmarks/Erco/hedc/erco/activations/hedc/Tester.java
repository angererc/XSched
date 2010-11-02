package erco.activations.hedc;
/*
 * Copyright (C) 2000 by ETHZ/INF/CS
 * All rights reserved
 * 
 * @version $Id: Tester.java 3342 2003-07-31 09:36:46Z praun $
 * @author Christoph von Praun
 */

import java.util.*;
import java.io.*;

import xsched.Activation;
import ethz.util.*;

public class Tester {

	private static RandomDate randomDate_ = null;
	private static MetaSearchImpl msi_ = null;
	private final static int TES_THREADS_;
	private final static int TES_PAUSE_;
	private final static int TES_ITERATIONS_;
	private final static String TES_START_;
	private final static String TES_END_;
	private final static String TES_SOHO_SYNOPTIC_;
	private final static String TES_RAG_;
	private final static String TES_MESOLA_;
	private final static String TES_SACREAMENTAL_;
	private final static String TES_WAIT_TIME_;

	static {
		SystemProperties.setSource("shared_tests/Benchmarks/Erco/hedc/properties.txt");
		SystemProperties sp = SystemProperties.getUniqueInstance();
		TES_THREADS_ = sp.getInteger("Tester.THREADS", 0);
		TES_PAUSE_ = sp.getInteger("Tester.PAUSE", 0);
		TES_ITERATIONS_ = sp.getInteger("Tester.ITERATIONS", 0);
		TES_START_ = sp.getString("Tester.START", null);
		TES_END_ = sp.getString("Tester.END", null);
		TES_SOHO_SYNOPTIC_ = sp.getString("Tester.SOHO_SYNOPTIC", "0");
		TES_RAG_ = sp.getString("Tester.RAG", "0");
		TES_MESOLA_ = sp.getString("Tester.MESOLA", "0");
		TES_SACREAMENTAL_ = sp.getString("Tester.SACREAMENTAL", "0");
		TES_WAIT_TIME_ = sp.getString("Tester.WAIT_TIME", "3");
	}

	public static void main(String[] args) throws Exception {
		Messages.debug(0, "Tester::main - starting HEDC metasearch.");
		msi_ = MetaSearchImpl.getUniqueInstance();
		randomDate_ = new RandomDate(TES_START_, TES_END_);
		
		Activation<Void> main = Activation.schedule(new Driver(), "begin");
		Activation.kickOffMain(main);
		
		Messages.debug(0, "Tester::main - running ... metasearch terminates after all testers processed their queries.");
	}

	private int pause_ = 0;
	private int iterations_ = 0;
	private FileWriter fw_ = null;
	private String name_;
	public Tester(String name, int pause, int iterations) throws IOException {
		Messages.debug(1, "Tester::<init> name=%1 pause=%2 it=%3", name, 
				String.valueOf(pause), String.valueOf(iterations));
		fw_ = new FileWriter(name);
		pause_ = pause;
		name_ = name;
		iterations_ = iterations;
	}

	public static class Driver {
		public void begin() {
			Activation<Void> later = Activation.schedule(this, "end");
			for (int i=TES_THREADS_-1; i >= 0; i--) {
				try {
					Messages.debug(0, "Tester::main - creating tester thread %1.", String.valueOf(i));				
					Activation<Void> next = Activation.schedule(new Tester("thread" + i + ".log", TES_PAUSE_, TES_ITERATIONS_), "run", later);
					next.hb(later);
					later = next;
				} catch (Exception e) {
					e.printStackTrace();
					Messages.check(false);
				}
			}
		}
		public void end() {
			System.out.println("all activations are done");		
		}
	}
		
	public void run(Activation<Void> later) {
		Messages.debug(0, "Tester::run start");
		Activation<Void> iteration = Activation.schedule(this, "iteration", later);
		iteration.hb(later);
	}
		
	public void writeResult(MetaSearchRequest m) throws IOException {
		System.out.println("Writing result");
		iterations_--;
		fw_.write("OK: " + m.printResults() + "\n");		
	}

	public void iteration(Activation<Void> later) {		
		System.out.println("starting Iteration " + iterations_);
		if (iterations_ > 0) {
			try {
				Thread.sleep((long) (pause_ * Math.random()));
				Hashtable parameters = new Hashtable();
				parameters.put("MESOLA", TES_MESOLA_);
				parameters.put("SACREAMENTAL", TES_SACREAMENTAL_);
				parameters.put("SOHO_SYNOPTIC", TES_SOHO_SYNOPTIC_);
				parameters.put("RAG", TES_RAG_);
				parameters.put("WAIT_TIME", TES_WAIT_TIME_);
				parameters.put("DATETIME", randomDate_.nextString());
				MetaSearchRequest m = new MetaSearchRequest(null, msi_, parameters);

				Activation<Void> go = Activation.schedule(m, "go");
				Activation<Void> writeResult = Activation.schedule(this, "writeResult", m);
				Activation<Void> nextIteration = Activation.schedule(this, "iteration", later);
				
				go.hb(writeResult);
				writeResult.hb(nextIteration);
				nextIteration.hb(later);

			} catch (Exception e) {
				iterations_--;
				try {
					fw_.write("BROKEN: - exception=" + e + "\n");
				} catch (Exception _) {}
			}
		} else {
			try {
				fw_.close();
			} catch (Exception e) { 
				Messages.check(false);
			}
			Messages.debug(0, "Tester::run end -- left output in logfile '%1'", name_);
		}
	}
}
