package com.ergotech.brickpi.sensors.ev3;

import java.io.IOException;

import com.ergotech.brickpi.sensors.Sensor;
import com.ergotech.brickpi.sensors.SensorType;

/**
 * This is the Infrared proximity sensor
 * @author benny
 *
 */
public class EV3InfraredSensor extends Sensor {

	public EV3InfraredSensor(SensorType sensorType) {
		super(sensorType);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void processResult(byte[] message) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
