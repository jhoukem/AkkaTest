package com.lightbend.akka.counter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class Counter extends AbstractActor {

	int count, fromLine, toLine;
	String filePath;
	char charToCount;

	static public Props props() {
		return Props.create(Counter.class, () -> new Counter());
	}

	public Counter() {
		count = 0;
		fromLine = 0;
		toLine = 0;
	}

	private void countOccurenceOnLine(String line) {
		for (int i = 0; i < line.length(); i++) {
			if (line.charAt(i) == charToCount) {
				count++;
			}
		}
	}

	private void count() {

		BufferedReader br = null;
		try  {
			br = new BufferedReader(new FileReader(filePath));

			int lineCount = 0;// The number of line currently read.

			String line = br.readLine();
			while (line != null) {

				// After the lines we have to count.
				if(lineCount > toLine){
					break;
				}
				// Lines we need to read and count occurences.
				if(lineCount >= fromLine && lineCount < toLine){
					countOccurenceOnLine(line);
				}
				line = br.readLine();
				lineCount++;
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


	@Override
	public Receive createReceive() {
		return receiveBuilder().match(CounterStart.class, cs -> {
			this.filePath = cs.filePath;
			this.fromLine = cs.fromLine;
			this.toLine = cs.toLine;
			this.charToCount = cs.toCount;
			// Proceed to count.
			count();
			// Send the count to the router.
			getSender().tell(new Count(count), getSelf());
		}).build();
	}

}
