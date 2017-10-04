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

	int totalCount;
	int totalFileLines;
	int nbCounterToWait;
	String filePath;
	char charToCount;
	ProgramStatus status;

	ArrayList<ActorRef> counterList = new ArrayList<ActorRef>();

	static public Props props(ProgramStatus status) {
		return Props.create(Router.class, () -> new Router(status));
	}

	public Router(ProgramStatus status) {
		this.status = status;
		totalCount = 0;
		totalFileLines = 0;
		nbCounterToWait = 0;
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
				}).
				match(Counter.class, c -> {
					this.counterList.add(c.getSelf());
				}).
				match(RouterStart.class, rs -> {
					this.charToCount = rs.toCount;
					this.filePath = rs.filePath;
					nbCounterToWait = rs.nbCounterToWait;

					// Wait till all the counter are added to the list.
//					while(nbCounterToWait != counterList.size() - 1){
//						Thread.sleep(100);
//						System.out.println("CounterListSize = "+counterList.size());
//					}
					
					startCount();
				})
				.build();
	}

	private void startCount() {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(filePath));

			// Get total line count.
			while (br.readLine() != null) {
				totalFileLines++;
			}
			br.close();

			int lineToReadPerCounter = totalFileLines/nbCounterToWait;

			System.out.println("nb Counter to wait = "+nbCounterToWait);
			System.out.println("line to read per counter = "+lineToReadPerCounter);
			
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
