package erco.activations.elevator;
/*
 * Copyright (C) 2000 by ETHZ/INF/CS
 * All rights reserved
 * 
 * @version $Id: Elevator.java 2094 2003-01-30 09:41:18Z praun $
 * @author Roger Karrer
 */

import java.lang.*;
import java.util.*;
import java.io.*;

import xsched.Activation;

public class Elevator {

	// shared control object
	private static Controls controls;
	private static Vector events;
	private final int numFloors, numLifts;

	// Initializer for main class, reads the input and initlizes
	// the events Vector with ButtonPress objects
	private Elevator() {
		InputStreamReader reader = new InputStreamReader(System.in);
		StreamTokenizer st = new StreamTokenizer(reader);
		st.lowerCaseMode(true);
		st.parseNumbers();

		events = new Vector();

		int numFloors = 0, numLifts = 0;
		try {
			System.out.print("How many floors? ");
			numFloors = readNum(st);
			System.out.print("How many lifts? ");
			numLifts = readNum(st);

			int time = 0, to = 0, from = 0;
			do {
				System.out.print("At Time (0 to start simulation)? ");
				time = readNum(st);
				if (time != 0) {
					System.out.print("From floor? ");
					from = readNum(st);
					System.out.print("To floor? ");
					to = readNum(st);
					events.addElement(new ButtonPress(time, from, to));
				}
			} while (time != 0);
		} catch (IOException e) {
			System.err.println("error reading input: " + e.getMessage());
			e.printStackTrace(System.err);
			System.exit(1);
		}

		// Create the shared control object
		controls = new Controls(numFloors);
		this.numFloors = numFloors;
		this.numLifts = numLifts;
	}
	
	public void doButtonPresses() {
		// First tick is 1
		int time = 1;

		for (int i = 0; i < events.size();) {
			ButtonPress bp = (ButtonPress) events.elementAt(i);
			// if the current tick matches the time of th next event
			// push the correct buttton
			if (time == bp.time) {
				System.out
						.println("Elevator::begin - its time to press a button");
				if (bp.onFloor > bp.toFloor)
					controls.pushDown(bp.onFloor, bp.toFloor);
				else
					controls.pushUp(bp.onFloor, bp.toFloor);
				i += 1;
			}
			// wait 1/2 second to next tick
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
			time += 1;
		}
		controls.setTerminated();
	}

	// Press the buttons at the correct time
	public void begin() {
		
		// Create the elevators
		for (int i = 0; i < numLifts; i++) {
			Lift lift = new Lift(numFloors, controls);
			Activation.schedule(lift, "begin()V;");
		}
		
		Activation.schedule(this, "doButtonPresses()V;");
	}

	private int readNum(StreamTokenizer st) throws IOException {
		int tokenType = st.nextToken();

		if (tokenType != StreamTokenizer.TT_NUMBER)
			throw new IOException("Number expected!");
		return (int) st.nval;
	}

	public static void main(String args[]) {
		Elevator building = new Elevator();
		
		Activation<Void> main = Activation.schedule(building, "begin()V;");
		Activation.kickOffMain(main);		
	}
}
