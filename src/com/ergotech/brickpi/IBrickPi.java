package com.ergotech.brickpi;

import java.io.IOException;

import com.ergotech.brickpi.motion.MotorPort;
import com.ergotech.brickpi.sensors.Sensor;
import com.ergotech.brickpi.sensors.SensorPort;

public interface IBrickPi {
	
	/**
	 * Fire the motor
	 * @param motorPort
	 * @param power
	 */
	public void setMotor(MotorPort motorPort, int power) throws IOException;
	
	/**
	 * Set the sensor type on the port
	 * @param sensor
	 * @param port
	 */
	public void setSensor(Sensor sensor, SensorPort port) throws IOException;
	
	public <T extends Sensor> T getSensor(SensorPort port) throws IOException;
}
