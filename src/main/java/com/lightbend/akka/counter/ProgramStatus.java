package com.lightbend.akka.counter;

public class ProgramStatus {

	private boolean over;

	public ProgramStatus() {
		this.over = false;
	}

	public boolean isOver() {
		return over;
	}

	public void setOver(boolean over) {
		this.over = over;
	}

}
