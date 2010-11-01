package jgfmt.section3.raytracer;

import jgfmt.jgfutil.JGFInstrumentor;

public class ActivationsRayTracerRunner extends RayTracer {
	int id, height, width;
	Interval interval;
	
	public ActivationsRayTracerRunner(int id, int width, int height) {
		this.id = id;
		this.width = width;
		this.height = height;

		JGFInstrumentor.startTimer("Section3:RayTracer:Init");

		// create the objects to be rendered
		scene = createScene();

		// get lights, objects etc. from scene.
		setScene(scene);

		numobjects = scene.getObjects();
		JGFRayTracerBench.staticnumobjects = numobjects;

		JGFInstrumentor.stopTimer("Section3:RayTracer:Init");

	}
	
	public void phase1() {
		//System.out.println("Task " + id + " phase1()");
		// Set interval to be rendered to the whole picture
		// (overkill, but will be useful to retain this for parallel versions)

		interval = new Interval(0, width, height, 0, height, 1, id);
	}

	public void phase2() {
		//System.out.println("Task " + id + " phase2()");
		if (id == 0)
			JGFInstrumentor.startTimer("Section3:RayTracer:Run");
	}
	
	public void phase3() {
		//System.out.println("Task " + id + " phase3()");
		render(interval);

		// Signal this thread has done iteration

		synchronized (scene) {
			for (int i = 0; i < JGFRayTracerBench.nthreads; i++)
				if (id == i)
					JGFRayTracerBench.checksum1 = JGFRayTracerBench.checksum1
					+ checksum;
		}
	}
	
	public void phase4() {
		//System.out.println("Task " + id + " phase4()");
		if (id == 0)
			JGFInstrumentor.stopTimer("Section3:RayTracer:Run");
	}
}
