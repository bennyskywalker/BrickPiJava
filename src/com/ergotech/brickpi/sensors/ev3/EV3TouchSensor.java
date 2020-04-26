/*
 *  Copyright ErgoTech Systems, Inc 2014
 *
 * This file is made available online through a Creative Commons Attribution-ShareAlike 3.0  license.
 * (http://creativecommons.org/licenses/by-sa/3.0/)
 *
 *  This is a library of functions for the RPi to communicate with the BrickPi.
 */
package com.ergotech.brickpi.sensors.ev3;

import java.io.IOException;

import com.ergotech.brickpi.sensors.Sensor;
import com.ergotech.brickpi.sensors.SensorType;
import com.ergotech.brickpi.sensors.Sensor.SENSOR_STATE;

/**
 * Representation of a Touch Sensor.
 * @author sdaubin
 */
public class EV3TouchSensor extends Sensor {
	
    /**
     * The current value of the sensor.
     */
    protected volatile int value;
	
    /**
     * Returns an instance of this sensor.
     */
    public EV3TouchSensor() {
        super(SensorType.EV3_TOUCH);
    }
    
    /**
     * Returns the 1 or 0 for consistency with the sensor interface.
     */
    public int getValue() {
        return this.value;
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
            case TOUCH:
            case EV3_TOUCH:
            case NXT_TOUCH:
            	this.value = message[6]; 
            	break;
            default:
            	throw new IOException("getSensor error: Invalid sensor data");
            }
    	}
    }    
}
