package com.lightbend.akka.counter;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class Main {

	private final static int NB_COUNTER = 3;
	private final static char TO_COUNT = 'y';

	public static void main(String[] args) {

		ProgramStatus status = new ProgramStatus();
		final ActorSystem system = ActorSystem.create("CounterSystem");

		// Create actors.
		final ActorRef router = system.actorOf(Router.props(status), "routerActor");

		for (int i = 0; i < NB_COUNTER; i++) {
			system.actorOf(Counter.props(router), "counter" + i);
		}

		System.out.println("Processing to count '"+TO_COUNT+"' occurences in the given text...");
		router.tell(new RouterStart("fra-tv_web_2016_10K-sentences.txt", TO_COUNT), ActorRef.noSender());
		
		while(!status.isOver()){
			try {
				Thread.sleep(100);
				// Waiting for the program to finish.
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		system.terminate();
	}
}
