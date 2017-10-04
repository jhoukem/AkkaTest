package com.lightbend.akka.counter;

public class RouterStart {

	String filePath;
	char toCount;
	int nbCounterToWait;

	public RouterStart(String path, char toCount, int nbCounterToWait) {
		this.filePath = path;
		this.toCount = toCount;
		this.nbCounterToWait = nbCounterToWait;
	}
}
