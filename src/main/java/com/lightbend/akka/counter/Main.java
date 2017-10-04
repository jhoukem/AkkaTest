package com.lightbend.akka.counter;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class Main {

	private final static int NB_COUNTER = 4;
	private final static char CHAR_TO_COUNT = 'o';
	private final static String FILE_PATH = "fra_news_2005-2008_300K-sentences.txt";

	public static void main(String[] args) {

		ProgramStatus status = new ProgramStatus();
		final ActorSystem system = ActorSystem.create("CounterSystem");

		// Create actors.
		final ActorRef router = system.actorOf(Router.props(status, FILE_PATH, CHAR_TO_COUNT, NB_COUNTER), "routerActor");

		System.out.println("Processing to count '"+CHAR_TO_COUNT+"' occurences in the text '"+ FILE_PATH +"'.");
		for (int i = 0; i < NB_COUNTER; i++) {
			system.actorOf(Counter.props(router), "counter" + i);
		}
		
		long startTime = System.currentTimeMillis();
		while(!status.isOver()){
			try {
				Thread.sleep(100);
				// Waiting for the program to finish.
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		system.terminate();
		System.out.println("Occurence count took "+ (System.currentTimeMillis() - startTime) + " ms.");
	}
}
