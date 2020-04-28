package com.ergotech.brickpi;

import java.io.IOException;

import com.ergotech.brickpi.motion.MotorPort;
import com.ergotech.brickpi.motion.MotorStatus;
import com.ergotech.brickpi.sensors.Sensor;
import com.ergotech.brickpi.sensors.SensorPort;

public interface IBrickPi {
	
	/**
	 * Fire the motor
	 * @param motorPort
	 * @param power An 8-bit signed value to specify motor power. 
	 * -100 to 100 for reverse full to 
	 * forward full, or greater than 100 for float.
	 */
	public void setMotor(MotorPort motorPort[], int power) throws IOException;
	
	/**
	 * 
	 * @param motorPort
	 * @param position A 32-bit signed value to specify motor 
	 * target position in degrees. -2,147,483,648 to 2,147,483,647.
	 * @throws IOException
	 */
	public void setMotorPosition(MotorPort motorPort[], int position) throws IOException;
	
	
	/**
	 * Set the relative motor target position in degrees. 
	 * Current position plus the specified degrees.
	 * @param motorPort
	 * @param degrees The relative target position in degrees
	 * @throws IOException
	 */
	public void setMotorPositionRelative(MotorPort motorPort[], int degrees) throws IOException;
	
	/**
	 * 
	 * @param motorPort
	 * @param offset A 32-bit signed value to specify motor encoder offset in 
	 * degrees. -2,147,483,648 to 2,147,483,647. To zero the encoder, write the 
	 * current position as the offset.
	 * @throws IOException
	 */
	public void setMotorEncoderOffset(MotorPort motorPort[], int offset) throws IOException;
	
	/**
	 * Set the sensor type on the port
	 * @param sensor
	 * @param port
	 */
	public void setSensor(Sensor sensor, SensorPort port) throws IOException;
	
	/**
	 * 
	 * @param <T>
	 * @param port
	 * @return
	 * @throws IOException
	 */
	public <T extends Sensor> T getSensor(SensorPort port) throws IOException;
	
	/**
	 * 
	 * @param motorPort
	 * @return Returns the encoder position in degrees
	 * @throws IOException
	 */
	public int getMotorEncoder(MotorPort motorPort) throws IOException;
	
	/**
	 * Get the current status of a motor.
	 * @param motorPort
	 * @return
	 * @throws IOException
	 */
	public MotorStatus getMotorStatus(MotorPort motorPort) throws IOException;
}
