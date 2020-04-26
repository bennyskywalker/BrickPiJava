package com.ergotech.brickpi.sensors.ev3;

import java.io.IOException;

import com.ergotech.brickpi.sensors.Sensor;
import com.ergotech.brickpi.sensors.SensorType;
import com.ergotech.brickpi.sensors.generic.Color;

/**
 * The EV3 color sensor.
 * @author sdaubin
 *
 */
public class EV3ColorSensor extends Sensor {

    public EV3ColorSensor() {
        super(SensorType.EV3_COLOR_COLOR);
    }
    
    public Color getColor() {
        Color color = Color.COLORS.get(getValue());
        return color == null ? Color.Unknown : color;
    }

    public int getValue() {
    	return 0;
    }

	@Override
	public void processResult(byte[] message) throws IOException {
		// TODO Auto-generated method stub
		
	}
}
