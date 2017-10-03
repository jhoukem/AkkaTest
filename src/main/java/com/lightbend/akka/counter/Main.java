package com.lightbend.akka.counter;

import java.io.IOException;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class Main {

	private final static int NB_COUNTER = 3;
	private final static char TO_COUNT = 'e';

	public static void main(String[] args) {
		final ActorSystem system = ActorSystem.create("helloakka");

		// Create actors.
		final ActorRef router = system.actorOf(Router.props(), "routerActor");

		for (int i = 0; i < NB_COUNTER; i++) {
			system.actorOf(Counter.props(router), "counter" + i);
		}

		router.tell(new RouterStart("fra-tv_web_2016_10K-sentences.txt", TO_COUNT), ActorRef.noSender());

		System.out.println(">>> Press ENTER to exit <<<");
		try {
			System.in.read();
		} catch (IOException ioe) {
		} finally {
			system.terminate();
		}
	}
}
