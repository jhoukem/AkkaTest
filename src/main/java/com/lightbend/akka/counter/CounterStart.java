package com.lightbend.akka.counter;

public class CounterStart {

	char toCount;
	int fromLine, toLine;
	String filePath;

	public CounterStart(String filePath, int fromLine, int toLine, char charToCount) {
		this.filePath = filePath;
		this.fromLine = fromLine;
		this.toLine = toLine;
		this.toCount = charToCount;
	}

}
