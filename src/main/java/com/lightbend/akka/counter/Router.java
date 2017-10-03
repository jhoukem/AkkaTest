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

	ArrayList<ActorRef> counterList = new ArrayList<ActorRef>();

	static public Props props() {
		return Props.create(Router.class, () -> new Router());
	}

	public Router() {
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
						System.out.println("Total count is : " + totalCount);
					}
				}).
				match(Counter.class, c -> {
					this.counterList.add(c.getSelf());
					nbCounterToWait = counterList.size();
				}).
				match(RouterStart.class, rs -> {
					launchStart(rs.filePath, rs.toCount);
				})
				.build();
	}

	private void launchStart(String filePath, char toCount) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(filePath));

			// Get total line count.
			while (br.readLine() != null) {
				totalFileLines++;
			}
			br.close();
			// Reset the BufferedReader.
			br =  new BufferedReader(new FileReader(filePath));

			int ligneToReadPerCounter = totalFileLines/counterList.size();
			int lineCount = 0;
			int counterIndex = 0;

			StringBuilder sb = new StringBuilder();
			// Read the file again and give to each counter the same amount of line to read.
			String line = br.readLine();
			while (line != null) {
				lineCount++;
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();

				if(lineCount == ligneToReadPerCounter && (counterIndex != counterList.size()-1)){
					lineCount = 0; // Reset the line counter.
					ActorRef counter = counterList.get(counterIndex++);
					String toRead = sb.toString();
					// Reset the string builder.
					sb = new StringBuilder();
					counter.tell(new CounterStart(toRead, toCount), getSelf());
				}
			}
			String toRead = sb.toString();
			// The last counter read the end of the file.
			counterList.get(counterIndex).tell(new CounterStart(toRead, toCount), getSelf());
			
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
