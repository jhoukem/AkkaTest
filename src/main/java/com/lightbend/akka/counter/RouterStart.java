package com.lightbend.akka.counter;

public class RouterStart {

	String filePath;
	char toCount;

	public RouterStart(String path, char toCount) {
		this.filePath = path;
		this.toCount = toCount;
	}
}
