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
	int nbCounter;

	ArrayList<ActorRef> counterList = new ArrayList<ActorRef>();

	static public Props props() {
		return Props.create(Router.class, () -> new Router());
	}

	public Router() {
		totalCount = 0;
	}


	@Override
	public Receive createReceive() {
		return receiveBuilder().
				match(Count.class, c -> {
					this.totalCount += c.count;
					this.nbCounter--;
					if(nbCounter == 0){
						System.out.println("Total count is : " + totalCount);
					}
				}).
				match(InitCounter.class, ic -> {
					this.counterList = ic.counterList;
					nbCounter = counterList.size();
				}).
				match(RouterStart.class, rs -> {
					launchStart(rs.path);
				})
				.build();
	}

	private void launchStart(String path) {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(path));

			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				totalFileLines++;
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			String everything = sb.toString();
			int ligneToReadPerCounter = totalFileLines/counterList.size();


			for(int i = 0; i < counterList.size(); i++){

				String toReadByCounter;
				
				if(i < counterList.size() - 1){
					toReadByCounter = everything.substring(i*ligneToReadPerCounter);
				} else {
					toReadByCounter = everything.substring(i*ligneToReadPerCounter, (i+1)*ligneToReadPerCounter);
				}
				
				ActorRef counter = counterList.get(i);
				counter.tell(new Start(toReadByCounter), getSelf());

			}


		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			//		    br.close();
		}
	}


}
