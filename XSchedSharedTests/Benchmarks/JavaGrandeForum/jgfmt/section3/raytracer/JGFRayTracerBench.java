/**************************************************************************
 *                                                                         *
 *         Java Grande Forum Benchmark Suite - Thread Version 1.0          *
 *                                                                         *
 *                            produced by                                  *
 *                                                                         *
 *                  Java Grande Benchmarking Project                       *
 *                                                                         *
 *                                at                                       *
 *                                                                         *
 *                Edinburgh Parallel Computing Centre                      *
 *                                                                         * 
 *                email: epcc-javagrande@epcc.ed.ac.uk                     *
 *                                                                         *
 *                                                                         *
 *      This version copyright (c) The University of Edinburgh, 2001.      *
 *                         All rights reserved.                            *
 *                                                                         *
 **************************************************************************/

package jgfmt.section3.raytracer;

import xsched.Activation;
import jgfmt.jgfutil.JGFInstrumentor;
import jgfmt.jgfutil.JGFSection3;

public class JGFRayTracerBench extends RayTracer implements JGFSection3 {

	public static int nthreads;
	public static long checksum1 = 0;
	public static int staticnumobjects;
	public boolean failed;

	public JGFRayTracerBench(int nthreads) {
		this.nthreads = nthreads;
	}

	public void JGFsetsize(int size) {
		this.size = size;
	}

	public void JGFinitialise() {

		// set image size
		width = height = datasizes[size];

	}
	
	public void start() {
		Activation<Void> barrier1 = Activation.schedule(this, "barrier(Ljava/lang/String;)V;", "1");
		Activation<Void> barrier2 = Activation.schedule(this, "barrier(Ljava/lang/String;)V;", "2");		
		Activation<Void> barrier3 = Activation.schedule(this, "barrier(Ljava/lang/String;)V;", "3");
		Activation<Void> barrier4 = Activation.schedule(this, "barrier(Ljava/lang/String;)V;", "4");
		
		for (int i = 0; i < nthreads; i++) {
			ActivationsRayTracerRunner runner = new ActivationsRayTracerRunner(i, width, height);
			Activation<Void> p1 = Activation.schedule(runner, "phase1()V;");
			p1.hb(barrier1);
			
			Activation<Void> p2 = Activation.schedule(runner, "phase2()V;");
			barrier1.hb(p2);
			p2.hb(barrier2);
			
			Activation<Void> p3 = Activation.schedule(runner, "phase3()V;");
			barrier2.hb(p3);
			p3.hb(barrier3);
			
			Activation<Void> p4 = Activation.schedule(runner, "phase4()V;");
			barrier3.hb(p4);
			p4.hb(barrier4);
		}
	}
	
	public void barrier(String name) {
		//System.out.println("Barrier " + name + " reached");
	}

	public void JGFapplication() {

		if(nthreads == -1) {
			nthreads = 2;
			Activation<Void> start = Activation.schedule(this, "start()V;"); 
			Activation.kickOffMain(start);			
		} else {	
			//TODO commented out to avoid spurious call in schedule analysis
//			Runnable thobjects[] = new Runnable[nthreads];
//			Thread th[] = new Thread[nthreads];
//			Barrier br = new TournamentBarrier(nthreads);
//	
//			// Start Threads
//	
//			for (int i = 1; i < nthreads; i++) {
//	
//				thobjects[i] = new RayTracerRunner(i, width, height, br);
//				th[i] = new Thread(thobjects[i]);
//				th[i].start();
//			}
//	
//			thobjects[0] = new RayTracerRunner(0, width, height, br);
//			thobjects[0].run();
//	
//			for (int i = 1; i < nthreads; i++) {
//				try {
//					th[i].join();
//				} catch (InterruptedException e) {
//				}
//			}
		}
	}

	public void JGFvalidate() {
		long refval[] = { 2676692, 29827635 };
		long dev = checksum1 - refval[size];
		if (dev != 0) {
			failed = true;
			System.out.println("Validation failed");
			System.out.println("Pixel checksum = " + checksum1);
			System.out.println("Reference value = " + refval[size]);
		}
	}

	public void JGFtidyup() {
		scene = null;
		lights = null;
		prim = null;
		//NDM tRay = null;
		inter = null;

		System.gc();
	}

	public void JGFrun(int size) {

		JGFInstrumentor.addTimer("Section3:RayTracer:Total", "Solutions", size);
		JGFInstrumentor.addTimer("Section3:RayTracer:Init", "Objects", size);
		JGFInstrumentor.addTimer("Section3:RayTracer:Run", "Pixels", size);

		JGFsetsize(size);

		JGFInstrumentor.startTimer("Section3:RayTracer:Total");

		JGFinitialise();
		JGFapplication();
		JGFvalidate();
		JGFtidyup();

		JGFInstrumentor.stopTimer("Section3:RayTracer:Total");

		JGFInstrumentor.addOpsToTimer(
				"Section3:RayTracer:Init",
				(double) staticnumobjects);
		JGFInstrumentor.addOpsToTimer(
				"Section3:RayTracer:Run",
				(double) (width * height));
		JGFInstrumentor.addOpsToTimer("Section3:RayTracer:Total", 1);

		JGFInstrumentor.printTimer("Section3:RayTracer:Init");
		JGFInstrumentor.printTimer("Section3:RayTracer:Run");
		JGFInstrumentor.printTimer("Section3:RayTracer:Total");
	}

}

class RayTracerRunner extends RayTracer implements Runnable {

	int id, height, width;
	Barrier br;

	public RayTracerRunner(int id, int width, int height, Barrier br) {
		this.id = id;
		this.width = width;
		this.height = height;
		this.br = br;

		JGFInstrumentor.startTimer("Section3:RayTracer:Init");

		// create the objects to be rendered
		scene = createScene();

		// get lights, objects etc. from scene.
		setScene(scene);

		numobjects = scene.getObjects();
		JGFRayTracerBench.staticnumobjects = numobjects;

		JGFInstrumentor.stopTimer("Section3:RayTracer:Init");

	}

	public void run() {

		// Set interval to be rendered to the whole picture
		// (overkill, but will be useful to retain this for parallel versions)

		Interval interval = new Interval(0, width, height, 0, height, 1, id);

		// synchronise threads and start timer

		br.DoBarrier(id);
		if (id == 0)
			JGFInstrumentor.startTimer("Section3:RayTracer:Run");

		render(interval);

		// Signal this thread has done iteration

		synchronized (scene) {
			for (int i = 0; i < JGFRayTracerBench.nthreads; i++)
				if (id == i)
					JGFRayTracerBench.checksum1 = JGFRayTracerBench.checksum1
							+ checksum;
		}

		// synchronise threads and stop timer

		br.DoBarrier(id);
		if (id == 0)
			JGFInstrumentor.stopTimer("Section3:RayTracer:Run");

	}
}
