package erco.activations.sor;
/*
 * Copyright (C) 2000 by ETHZ/INF/CS
 * All rights reserved
 * 
 * @version $Id: Sor.java 2094 2003-01-30 09:41:18Z praun $
 * @author Florian Schneider
 */

import java.util.*;

import xsched.Activation;

public class Sor {

	public final static int N = 500;
	public final static int M = 500;
	public static int iterations = 100;

	public static float[][] black_ = new float[M + 2][N + 1];
	public static float[][] red_ = new float[M + 2][N + 1];

	public static int nprocs = 1;
	
	static Object[] t;

	public static void main(String[] args) {

		boolean nop = false;

		try {
			if (args[0].equals("--nop"))
				nop = true;
			else {
				nprocs = Integer.parseInt(args[1]);
				iterations = Integer.parseInt(args[0]);
			}
		} catch (Exception e) {
			System.out
					.println("usage: java Sor <iterations> <number of threads>");
			System.out.println("    or java Sor --nop");
			System.exit(-1);
		}

		t = new Object[nprocs];
		
		// initialize arrays
		int first_row = 1;
		int last_row = M;

		for (int i = first_row; i <= last_row; i++) {
			/*
			 * Initialize the top edge.
			 */
			if (i == 1)
				for (int j = 0; j <= N; j++)
					red_[0][j] = black_[0][j] = (float) 1.0;
			/*
			 * Initialize the left and right edges.
			 */
			if ((i & 1) != 0) {
				red_[i][0] = (float) 1.0;
				black_[i][N] = (float) 1.0;
			} else {
				black_[i][0] = (float) 1.0;
				red_[i][N] = (float) 1.0;
			}
			/*
			 * Initialize the bottom edge.
			 */
			if (i == M)
				for (int j = 0; j <= N; j++)
					red_[i + 1][j] = black_[i + 1][j] = (float) 1.0;
		}

		// start computation
		System.gc();
		long a = new Date().getTime();

		if (!nop) {
			Activation<Void> main = Activation.schedule(new Sor(), "begin()V;");
			Activation.kickOffMain(main);
			
		}

		long b = new Date().getTime();

		System.out.println("Sor-" + nprocs + "\t" + Long.toString(b - a));

		// print out results
		float red_sum = 0, black_sum = 0;
		for (int i = 0; i < M + 2; i++)
			for (int j = 0; j < N + 1; j++) {
				red_sum += red_[i][j];
				black_sum += black_[i][j];
			}
		System.out.println("Exiting. red_sum = " + red_sum + ", black_sum = "
				+ black_sum);
	}

	public static void print(String s) {
		System.out.println(Thread.currentThread().getName() + ":" + s);
	}

	public void begin() {
		Activation<Void> later = Activation.schedule(this, "end()V;");
		
		for (int proc_id = 0; proc_id < nprocs; proc_id++) {
			int first_row = (M * proc_id) / nprocs + 1;
			int last_row = (M * (proc_id + 1)) / nprocs;

			if ((first_row & 1) != 0)
				t[proc_id] = new sor_first_row_odd(first_row, last_row);
			else
				t[proc_id] = new sor_first_row_even(first_row, last_row);
		}
		
		Activation<Void> iteration = Activation.schedule(this, "iteration(Lxsched/Activation;)V;", later);
		iteration.hb(later);
	}
	
	public static volatile int i = -1;
	public void iteration(Activation<Void> later) {
		i++;
		//System.out.println("Setting up next iteration " + i);
		if(i < Sor.iterations) {
			Activation<Void> nextIteration = Activation.schedule(this, "iteration(Lxsched/Activation;)V;", later);
			Activation<Void> barrier1 = Activation.schedule(this, "barrier1()V;");
			Activation<Void> barrier2 = Activation.schedule(this, "barrier2()V;");
			
			barrier1.hb(barrier2);
			barrier2.hb(nextIteration);			
			nextIteration.hb(later);
			
			for (int proc_id = 0; proc_id < nprocs; proc_id++) {
				Activation<Void> p1 = Activation.schedule(t[proc_id], "phase1()V;");
				Activation<Void> p2 = Activation.schedule(t[proc_id], "phase2()V;");
				p1.hb(barrier1);
				barrier1.hb(p2);
				p2.hb(barrier2);
			}
		}		
	}
	
	public void barrier1() {
		//System.out.println("Barrier 1 of iteration " + i + " reached");
	}
	
	public void barrier2() {
		//System.out.println("Barrier 2 of iteration " + i + " reached");
	}
	
	public void end() {
		System.out.println("activations are done...");
	}
	
	public static class sor_first_row_odd {

		int first_row;
		int end;
		int N = Sor.N;
		int M = Sor.M;
		float[][] black_ = Sor.black_;
		float[][] red_ = Sor.red_;

		public sor_first_row_odd(int a, int b) {
			first_row = a;
			end = b;
		}

		public void phase1() {
			int j, k;
			//Sor.print("phase 1 iteration A "+i);
			for (j = first_row; j <= end; j++) {

				for (k = 0; k < N; k++) {

					black_[j][k] = (red_[j - 1][k] + red_[j + 1][k]
							+ red_[j][k] + red_[j][k + 1])
							/ (float) 4.0;
				}
				if ((j += 1) > end)
					break;

				for (k = 1; k <= N; k++) {

					black_[j][k] = (red_[j - 1][k] + red_[j + 1][k]
							+ red_[j][k - 1] + red_[j][k])
							/ (float) 4.0;
				}
			}
		}
		
		public void phase2() {
			int j, k;
			//Sor.print("phase 2 iteration A "+i + ", first=" + first_row + " end=" + end);
			for (j = first_row; j <= end; j++) {

				for (k = 1; k <= N; k++) {

					red_[j][k] = (black_[j - 1][k] + black_[j + 1][k]
							+ black_[j][k - 1] + black_[j][k])
							/ (float) 4.0;
				}
				if ((j += 1) > end)
					break;

				for (k = 0; k < N; k++) {

					red_[j][k] = (black_[j - 1][k] + black_[j + 1][k]
							+ black_[j][k] + black_[j][k + 1])
							/ (float) 4.0;
				}
			}
		}

	}

	public static class sor_first_row_even {

		int first_row;
		int end;
		int N = Sor.N;
		int M = Sor.M;
		float[][] black_ = Sor.black_;
		float[][] red_ = Sor.red_;

		public sor_first_row_even(int a, int b) {
			first_row = a;
			end = b;
		}

		public void phase1() {
			//Sor.print("phase 1 iteration B "+i);
			for (int j = first_row; j <= end; j++) {

				for (int k = 1; k <= N; k++) {

					black_[j][k] = (red_[j - 1][k] + red_[j + 1][k]
					                                             + red_[j][k - 1] + red_[j][k])
					                                             / (float) 4.0;
				}
				if ((j += 1) > end)
					break;

				for (int k = 0; k < N; k++) {

					black_[j][k] = (red_[j - 1][k] + red_[j + 1][k]
					                                             + red_[j][k] + red_[j][k + 1])
					                                             / (float) 4.0;
				}
			}
		}
		
		public void phase2() {
			//Sor.print("phase 2 iteration B "+i);
			for (int j = first_row; j <= end; j++) {

				for (int k = 0; k < N; k++) {

					red_[j][k] = (black_[j - 1][k] + black_[j + 1][k]
					                                               + black_[j][k] + black_[j][k + 1])
					                                               / (float) 4.0;
				}
				if ((j += 1) > end)
					break;

				for (int k = 1; k <= N; k++) {

					red_[j][k] = (black_[j - 1][k] + black_[j + 1][k]
					                                               + black_[j][k - 1] + black_[j][k])
					                                               / (float) 4.0;
				}
			}
		}
	}
}
