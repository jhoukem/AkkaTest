package com.lightbend.akka.counter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class Router extends AbstractActor {

	private static final boolean DEBUG = true;

	int totalCount;
	int nbCounterToWait;
	String filePath;
	char charToCount;
	ProgramStatus status;

	ArrayList<ActorRef> counterList = new ArrayList<ActorRef>();

	static public Props props(ProgramStatus status, String filePath, char charToCount, int nbCounterToWait) {
		return Props.create(Router.class, () -> new Router(status, filePath, charToCount, nbCounterToWait));
	}

	public Router(ProgramStatus status, String filePath, char charToCount, int nbCounterToWait) {
		this.status = status;
		this.nbCounterToWait = nbCounterToWait;
		this.charToCount = charToCount;
		this.filePath = filePath;
		totalCount = 0;

		// Create the counters.
		for (int i = 0; i < nbCounterToWait; i++) {
			counterList.add(getContext().getSystem().actorOf(Counter.props(), "counter" + i));
		}

		startCount();
	}


	@Override
	public Receive createReceive() {
		return receiveBuilder().
				match(Count.class, c -> {
					this.totalCount += c.count;
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

				counterList.get(i).tell(new CounterStart(filePath, fromLine, toLine, charToCount), getSelf());
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
