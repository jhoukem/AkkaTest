package com.lightbend.akka.counter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.routing.ActorRefRoutee;
import akka.routing.RoundRobinRoutingLogic;
import akka.routing.Routee;
import akka.routing.Router;

public class Master extends AbstractActor {

	private static final boolean DEBUG = true;

	Router router;
	int totalCount;
	int nbCounterToWait;
	String filePath;
	char charToCount;
	ProgramStatus status;

	static public Props props(ProgramStatus status, String filePath, char charToCount, int nbCounterToWait) {
		return Props.create(Master.class, () -> new Master(status, filePath, charToCount, nbCounterToWait));
	}

	public Master(ProgramStatus status, String filePath, char charToCount, int nbCounterToWait) {
		this.status = status;
		this.nbCounterToWait = nbCounterToWait;
		this.charToCount = charToCount;
		this.filePath = filePath;
		totalCount = 0;

		// Create the counters.
		List<Routee> routees = new ArrayList<Routee>();
		for (int i = 0; i < nbCounterToWait; i++) {
			ActorRef r = getContext().actorOf(Counter.props());
			getContext().watch(r);
			routees.add(new ActorRefRoutee(r));
		}
		router = new Router(new RoundRobinRoutingLogic(), routees);

		startCount();
	}


	@Override
	public Receive createReceive() {
		return receiveBuilder().
				match(CounterResult.class, c -> {
					// Increment the total occurrence count.
					this.totalCount += c.count;
					// Stop the counter
					getContext().stop(getSender());
				}).
				// A counter has stopped.
				match(Terminated.class, message -> {
					router = router.removeRoutee(message.actor());
					this.nbCounterToWait--;
					if(nbCounterToWait == 0){
						System.out.println("There is "+totalCount+" occurence(s) for the char '"
								+charToCount+"' in the text '"+filePath+"'");
						status.setOver(true);
					}
				})
				.build();
	}

	private void startCount() {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(filePath));

			int totalFileLines = 0;
			// Get total line count.
			while (br.readLine() != null) {
				totalFileLines++;
			}
			br.close();

			int lineToReadPerCounter = totalFileLines/nbCounterToWait;

			if(DEBUG){
				System.out.println("[Router]: Total number of line(s) = "+totalFileLines);
				System.out.println("[Router]: Number of counter = "+nbCounterToWait);
				System.out.println("[Router]: Line(s) to read per counter = "+lineToReadPerCounter);
			}

			for(int i = 0; i < nbCounterToWait; i++){

				int fromLine = i * lineToReadPerCounter;
				int toLine;
				// Last counter read till the end of the file.
				if(i+1 == nbCounterToWait){
					toLine = totalFileLines;
				} else {
					toLine = (i+1) * lineToReadPerCounter;
				}
				router.route(new CounterStart(filePath, fromLine, toLine, charToCount), getSelf());
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
