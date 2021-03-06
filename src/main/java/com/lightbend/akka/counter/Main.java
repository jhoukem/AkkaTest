package com.lightbend.akka.counter;

import akka.actor.ActorSystem;

public class Main {

	private final static int NB_COUNTER = 4;
	private final static char CHAR_TO_COUNT = 'o';
	private final static String FILE_PATH = "fra-tv_web_2016_10K-sentences.txt";

	public static void main(String[] args) {

		ProgramStatus status = new ProgramStatus();
		final ActorSystem system = ActorSystem.create("CounterSystem");

		// Create the router manager that hold the router.
		system.actorOf(Master.props(status, FILE_PATH, CHAR_TO_COUNT, NB_COUNTER), "routerActor");

		
		System.out.println("Processing to count '"+CHAR_TO_COUNT+"' occurences in the text '"+ FILE_PATH +"'.");
		long startTime = System.currentTimeMillis();
		while(!status.isOver()){
			try {
				Thread.sleep(1);
				// Waiting for the program to finish.
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		system.terminate();
		System.out.println("Occurence count took "+ (System.currentTimeMillis() - startTime) + " ms.");
	}
}
