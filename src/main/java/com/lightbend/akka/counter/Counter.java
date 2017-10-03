package com.lightbend.akka.counter;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class Counter extends AbstractActor {

	int count;
	String toRead;

	char toCount;
	ActorRef router;

	static public Props props(ActorRef router) {
		return Props.create(Counter.class, () -> new Counter(router));
	}

	public Counter(ActorRef router) {
		this.router = router;
		router.tell(this, getSelf());
	}

	private void count() {
		for (int i = 0; i < toRead.length(); i++) {
			if (toRead.charAt(i) == toCount) {
				count++;
			}
		}
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(CounterStart.class, s -> {
			this.toRead = s.toRead;
			this.toCount = s.toCount;
			// Proceed to count.
			count();
			// Send the count to the router.
			router.tell(new Count(count), getSelf());
		}).build();
	}

}
