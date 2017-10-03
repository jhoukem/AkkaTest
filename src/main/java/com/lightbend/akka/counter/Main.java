package com.lightbend.akka.counter;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class Main {

	private final static int NB_COUNTER = 3;
	private final static char TO_COUNT = 'a';

	public static void main(String[] args) {
		final ActorSystem system = ActorSystem.create("helloakka");

		InitCounter init = new InitCounter();

		// Create actors.
		final ActorRef router = 
				system.actorOf(Router.props(), "routerActor");

		for(int i = 0; i < NB_COUNTER; i++){
			final ActorRef counterActor = 
					system.actorOf(Counter.props(router), "printerActor");
			counterActor.tell(new ToCount(TO_COUNT), ActorRef.noSender());
			init.counterList.add(counterActor);
		}
		router.tell(init, ActorRef.noSender());

		router.tell(new RouterStart("xxx"), ActorRef.noSender());
		
		system.terminate();
	}
}
