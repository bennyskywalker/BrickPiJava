package com.ergotech.brickpi.motion;

public class MotorStatus {
	
	public int state;
	public int power;
	public int position;
	public int dps;
	
	public MotorStatus(int state, int power, int position, int dps) {
		this.state = state;
		this.power = power;
		this.position = position;
		this.dps = dps;
	}
}
