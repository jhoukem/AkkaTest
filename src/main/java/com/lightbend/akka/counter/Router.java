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
	char toCount;

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
						System.out.println("There is "+totalCount+" occurence(s) for the char '"
								+toCount+"' in the text '"+filePath+"'");
					}
				}).
				match(Counter.class, c -> {
					this.counterList.add(c.getSelf());
					nbCounterToWait = counterList.size();
				}).
				match(RouterStart.class, rs -> {
					this.toCount = rs.toCount;
					this.filePath = rs.filePath;
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
			// Reset the BufferedReader.
			br =  new BufferedReader(new FileReader(filePath));

			int ligneToReadPerCounter = totalFileLines/counterList.size();
			int lineCount = 0;// The number of line currently read.
			int counterIndex = 0; // The index in the list of the counter to call.

			StringBuilder sb = new StringBuilder();
			// Read the file again and give to each counter the same amount of line to read
			// (except the last counter if counterNumber%totalLigne != 0).
			String line = br.readLine();
			while (line != null) {
				lineCount++;
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();

				// The last counter is started at the end to avoid => totalFileLignes%nbCounter != 0.
				if(lineCount == ligneToReadPerCounter && (counterIndex != counterList.size()-1)){
					// Reset the line counter so we read another "ligneToReadPerCounter" before starting a new counter.
					lineCount = 0; 
					String toRead = sb.toString();
					// Reset the string builder so the next counter only have the text that concern him.
					sb = new StringBuilder();
					ActorRef counter = counterList.get(counterIndex++);
					counter.tell(new CounterStart(toRead, toCount), getSelf());
				}
			}
			String toRead = sb.toString();
			// The last counter read till the end of the file.
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
