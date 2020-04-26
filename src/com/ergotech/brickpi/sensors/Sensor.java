/*
 *  Copyright ErgoTech Systems, Inc 2014
 *
 * This file is made available online through a Creative Commons Attribution-ShareAlike 3.0  license.
 * (http://creativecommons.org/licenses/by-sa/3.0/)
 *
 *  This is a library of functions for the RPi to communicate with the BrickPi.
 */
package com.ergotech.brickpi.sensors;

import java.io.IOException;
import java.util.BitSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ergotech.brickpi.BrickPiCommunications;

/**
 * The class representing a sensor.  You can create most sensors by invoking the Sensor 
 * constructor with a SensorType, but there are some subclasses of Sensor that provide a 
 * richer sensor specific api.
 */
public abstract class Sensor {
    
    protected final Logger LOGGER = 
            LoggerFactory.getLogger(getClass().getName());
    
    public enum SENSOR_STATE {
        VALID_DATA(0),
        NOT_CONFIGURED(1),
        CONFIGURING(2),
        NO_DATA(3),
        I2C_ERROR(4);

    	private int ordinal;
    	
    	SENSOR_STATE(int ordinal) {
    		this.ordinal = ordinal;
    	}
    	
    	public int getInt() {
    		return this.ordinal;
    	}
    }

    public static final byte MASK_D0_M = 0x01;
    public static final byte MASK_D0_S = 0x08;

    public static final byte TYPE_SENSOR_RAW = 0;
    public static final byte TYPE_SENSOR_LIGHT_OFF = 0;
    public static final byte TYPE_SENSOR_LIGHT_ON = (MASK_D0_M | MASK_D0_S);
    public static final byte TYPE_SENSOR_TOUCH = 32;
    public static final byte TYPE_SENSOR_ULTRASONIC_CONT = 33;
    public static final byte TYPE_SENSOR_ULTRASONIC_SS = 34;
    public static final byte TYPE_SENSOR_RCX_LIGHT = 35;
    public static final byte TYPE_SENSOR_COLOR_FULL = 36;
    public static final byte TYPE_SENSOR_COLOR_RED = 37;
    public static final byte TYPE_SENSOR_COLOR_GREEN = 38;
    public static final byte TYPE_SENSOR_COLOR_BLUE = 39;
    public static final byte TYPE_SENSOR_COLOR_NONE = 40;
    public static final byte TYPE_SENSOR_I2C = 41;
    public static final byte TYPE_SENSOR_I2C_9V = 42;
       
    // Mode information for EV3 is here: http://www.ev3dev.org/docs/sensors/lego-ev3-ultrasonic-sensor/
    /**
     * Continuous measurement, distance, cm
     */
    public static final byte TYPE_SENSOR_EV3_US_M0 = 43;
    /**
     * Continuous measurement, distance, in
     */
    public static final byte TYPE_SENSOR_EV3_US_M1 = 44;
    /**
     * Listen // 0 r 1 depending on presence of another US sensor.
     */
    public static final byte TYPE_SENSOR_EV3_US_M2 = 45;
    public static final byte TYPE_SENSOR_EV3_US_M3 = 46;
    public static final byte TYPE_SENSOR_EV3_US_M4 = 47;
    public static final byte TYPE_SENSOR_EV3_US_M5 = 48;
    public static final byte TYPE_SENSOR_EV3_US_M6 = 49;

    // http://www.ev3dev.org/docs/sensors/lego-ev3-color-sensor/
    /**
     * Reflected light intensity (0 to 100)
     */
    public static final byte TYPE_SENSOR_EV3_COLOR_M0 = 50;
    /**
     * Ambient light intensity (0 to 100)
     */
    public static final byte TYPE_SENSOR_EV3_COLOR_M1 = 51;
    /**
     * Detected color.
     * 0   none
     * 1   black
     * 2   blue
     * 3   green
     * 4   yellow
     * 5   red
     * 6   white
     * 7   brown
     */
    public static final byte TYPE_SENSOR_EV3_COLOR_M2 = 52;
    /**
     * Raw reflected
     */
    public static final byte TYPE_SENSOR_EV3_COLOR_M3 = 53;
    /**
     * Raw Color Components
     */
    public static final byte TYPE_SENSOR_EV3_COLOR_M4 = 54;
    /**
     * Calibration???  Not currently implemented.
     */
    public static final byte TYPE_SENSOR_EV3_COLOR_M5 = 55;

    /**
     * Angle
     */
    public static final byte TYPE_SENSOR_EV3_GYRO_M0 = 56;
    /**
     * Rotational Speed
     */
    public static final byte TYPE_SENSOR_EV3_GYRO_M1 = 57;
    /**
     * Raw sensor value ???
     */
    public static final byte TYPE_SENSOR_EV3_GYRO_M2 = 58;
    /**
     * Angle and Rotational Speed?
     */
    public static final byte TYPE_SENSOR_EV3_GYRO_M3 = 59;
    /**
     * Calibration ???
     */
    public static final byte TYPE_SENSOR_EV3_GYRO_M4 = 60;

    // Mode information is here:  http://www.ev3dev.org/docs/sensors/lego-ev3-infrared-sensor/
    /**
     * Proximity, 0 to 100
     */
    public static final byte TYPE_SENSOR_EV3_INFRARED_M0 = 61;
    /**
     * IR Seek, -25 (far left) to 25 (far right)
     */
    public static final byte TYPE_SENSOR_EV3_INFRARED_M1 = 62;
    /**
     * IR Remote Control, 0 - 11
     */
    public static final byte TYPE_SENSOR_EV3_INFRARED_M2 = 63;
    public static final byte TYPE_SENSOR_EV3_INFRARED_M3 = 64;
    public static final byte TYPE_SENSOR_EV3_INFRARED_M4 = 65;
    public static final byte TYPE_SENSOR_EV3_INFRARED_M5 = 66;
    
    public static final byte TYPE_SENSOR_EV3_TOUCH_0 = 67;
    public static final byte TYPE_SENSOR_EV3_TOUCH_DEBOUNCE = 68;

    
    protected final SensorType sensorType;

    /**
     * Returns the type of this sensor.
     */
    public final byte getSensorType() {
        return sensorType.getType();
    }
    
    public final SensorType getSensorTypeEnum() {
    	return sensorType;
    }

    /**
     * @param sensorType The type of this sensor. 
     * @param decodeBitLength The number of bits to decode for this sensor's value.
     */
    public Sensor(SensorType sensorType) {
        this.sensorType = sensorType;
    }
  
    /**
     * Process the sensor result string
     * Set the values internally
     * @param message
     */
    public abstract void processResult(byte[]message) throws IOException;
        
}
