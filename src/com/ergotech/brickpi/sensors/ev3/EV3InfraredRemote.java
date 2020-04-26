package com.ergotech.brickpi.sensors.ev3;

import java.io.IOException;

import com.ergotech.brickpi.sensors.Sensor;
import com.ergotech.brickpi.sensors.SensorType;
import com.ergotech.brickpi.sensors.Sensor.SENSOR_STATE;

/**
 * This is the EV3 Infrared Sensor
 * @author benny
 *
 */
public class EV3InfraredRemote extends Sensor {
	
	private boolean[] Buttons = new boolean[] {false, false, false, false};	
	private boolean ButtonBeacon = false;

	public EV3InfraredRemote() {
		super(SensorType.EV3_INFRARED_REMOTE);
	}
	
	/**
	 * Button index from 0..3
	 * @param index
	 * @return
	 */
	public boolean getButton(int index) {
		return Buttons[index];
	}
	
	public boolean getBeacon() {
		return ButtonBeacon;
	}

    /**
     * Process the sensor result string
     * Set the values internally
     * @param message
     */
    public final void processResult(byte[] message) throws IOException {
    	int portType = message[4];
    	boolean validData = false;
    	
    	if(message[5]==SENSOR_STATE.VALID_DATA.getInt()) {
    		validData = true;
    	}
    	
    	if(validData) {
            switch(super.sensorType) {
            case EV3_INFRARED_REMOTE:
            	processInfraredValue(message[6]); 
            	break;
            default:
            	throw new IOException("getSensor error: Invalid sensor data");
            }
    	}
    }
    
    private void processInfraredValue(int infraredValue) {
    	
    	Buttons[0] = false;
    	Buttons[1] = false;
    	Buttons[2] = false;
    	Buttons[3] = false;
    	ButtonBeacon = false;
    	
    	switch(infraredValue) {
    	case 1:
    		Buttons[0] = true;
    		break;
    	case 2:
    		Buttons[1] = true;
    		break;
    	case 3:
    		Buttons[2] = true;
    		break;
    	case 4:
    		Buttons[3] = true;
    		break;
    	case 5:
    		Buttons[0] = true;
    		Buttons[2] = true;
    		break;
    	case 6:
    		Buttons[0] = true;
    		Buttons[3] = true;
    		break;
    	case 7:
    		Buttons[1] = true;
    		Buttons[2] = true;
    		break;
    	case 8:
    		Buttons[1] = true;
    		Buttons[3] = true;
    		break;
    	case 9:
    		ButtonBeacon = true;
    		break;
    	case 10:
    		Buttons[0] = true;
    		Buttons[1] = true;
    		break;
    	case 11:
    		Buttons[2] = true;
    		Buttons[3] = true;
    		break;
    	}
    }
}
