/*
 *  Copyright ErgoTech Systems, Inc 2014
 *
 * This file is made available online through a Creative Commons Attribution-ShareAlike 3.0  license.
 * (http://creativecommons.org/licenses/by-sa/3.0/)
 *
 *  This is a library of functions for the RPi to communicate with the BrickPi.
 */
package com.ergotech.brickpi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ergotech.brickpi.BrickPiConstants.BPSPI_MESSAGE_TYPE;
import com.ergotech.brickpi.motion.Motor;
import com.ergotech.brickpi.motion.MotorPort;
import com.ergotech.brickpi.sensors.Sensor;
import com.ergotech.brickpi.sensors.SensorPort;
import com.ergotech.brickpi.sensors.SensorType;

/**
 * This class provides utility method for communication with the brick pi.
 */
public abstract class BrickPiCommunications {

    private static final Logger LOGGER = 
            LoggerFactory.getLogger(BrickPiCommunications.class.getName());

    /**
     * The current debug level.
     */
    public static int DEBUG_LEVEL = 1;
    
    /**
     * It would seem to be a desirable, and fairly likely feature that the brick
     * pis could be made stackable. In this case we will have multiple slaves on
     * the serial port. Currently this is not the case and we have only two, but
     * just to simplify future changes, I'll make this a constant.
     */
    public static final int SPI_TARGETS = 1;
    public static final int MOTOR_TARGETS = 4;
    public static final int SENSOR_TARGETS = 4;

    /**
     * Change the UART address.
     */
    public static final byte MSG_TYPE_CHANGE_ADDR = 1;
    /**
     * Change/set the sensor type.
     */
    public static final byte MSG_TYPE_SENSOR_TYPE = 2;
    /**
     * Set the motor speed and direction, and return the sesnors and encoders.
     */
    public static final byte MSG_TYPE_VALUES = 3;
    /**
     * Float motors immediately
     */
    public static final byte MSG_TYPE_E_STOP = 4;
    /**
     * Set the timeout
     */
    public static final byte MSG_TYPE_TIMEOUT_SETTINGS = 5;

    /** A thread safe list of event listeners .*/
    public final List<BrickPiUpdateListener> listeners;

    /**
     * The addresses of the 2 brick pi atmel chips. At this point in development
     * I have not yet found a reason why these should be exposed to the user at
     * all. If I find a reason, I'll expose them (maybe a future brick pi design
     * will need it).
     */
    protected final byte[] spiAddresses;

    /**
     * The array of sensors.
     */
    protected final Sensor[] sensorType;

    /**
     * The array of motors.
     */
    protected final Motor[] motors;

    /**
     * The executor that calls "updateValues" frequently. Tasks are scheduled
     * on this executor in "setupSensors".
     */
    protected final ScheduledExecutorService updateValuesExecutor = 
            Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
                
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "Update values thread");
                    t.setDaemon(true);
                    return t;
                }
            });

    private volatile ScheduledFuture<?> scheduledPoller;

    /**
     * How frequently to call updateValues. This is in milliseconds. A value of
     * zero (or less) stops the updates. Actually, the thread waits this amount
     * of time after the previous call before calling again, so the update rate
     * will be slightly slower by the amount of time it take to complete the
     * call. Additionally, the thread may be woken up, eg by configuring a motor
     * to ensure that the value is passed to the BrickPi.
     */
    protected volatile int updateDelay;

    /**
     * Create the brick pi instance. This will only occur on the "getBrickPi"
     * call, and only if it has not already been created.
     *
     */
    protected BrickPiCommunications() {
        spiAddresses = new byte[SPI_TARGETS];
        spiAddresses[0] = 0x01;  // one board
        sensorType = new Sensor[SENSOR_TARGETS * SPI_TARGETS];
        motors = new Motor[MOTOR_TARGETS * SPI_TARGETS];
        
        updateDelay = 100;
        listeners = Collections.synchronizedList(new ArrayList<BrickPiUpdateListener>());
    }

    /**
     * Send a packet to the brick pi.
     *
     * @param destinationAddress
     * @param packet
     */
    protected abstract void sendToBrickPi(byte destinationAddress, byte[] packet);

    /**
     * Read a packet from the brick pi.
     *
     * @param timeout total read timeout in ms
     * @return the packet read from the serial port/brickpi
     * @throws java.io.IOException thrown if there's a timeout reading the port.
     */
    protected abstract byte[] readFromBrickPi(int timeout) throws IOException;

    protected boolean verifyTransaction(byte [] result) {
    	String retCode = String.format("%02X",result[3]);
    	System.out.println(retCode);
    	if(retCode.compareTo("A5")==0) {
    		return true;
    	}
    	
    	return false;
    }
    
    /**
     * Sets the motor timeout. This is a watchdog. If the brickpi has not seen a
     * message from the pi in this amount of time the motors will gracefully
     * halt.
     *
     * @param timeout the timeout in microseconds (us).
     * @throws java.io.IOException thrown if the message transaction fails.
     */
    public void setTimeout(long timeout) throws IOException {
        byte[] packet = new byte[] {
                MSG_TYPE_TIMEOUT_SETTINGS,
                (byte) (timeout & 0xFF),
                (byte) ((timeout >> 8) & 0xFF),
                (byte) ((timeout >> 16) & 0xFF),
                (byte) ((timeout >> 24) & 0xFF)
        };
        for (int counter = 0; counter < SPI_TARGETS; counter++) {
        	sendToBrickPi(spiAddresses[counter], packet);            
        }
    }

    /**
     * Set the sensor at the particular port. There are current four sensor
     * ports.
     *
     * @param sensor the sensor to associate with the port. May be null to clear
     * the sensor configuration.
     * @param port the port.
     */
    public void setSensor(Sensor sensor, SensorPort port) throws IOException {
    	
    	//Consider building the transaction here - and sending it right away
        sensorType[port.getPort()] = sensor;
        
        byte[] packet = new byte[] {
        		BPSPI_MESSAGE_TYPE.SET_SENSOR_TYPE.getByte(),
                (byte)port.getPort(),
                (byte)sensor.getSensorType()
        };
        
        sendToBrickPi(spiAddresses[0], packet);           
        byte[] ret = readFromBrickPi(100);
    	if(verifyTransaction(ret)==false) {
    		throw new IOException("failed setSensor");
    	}
    }

    /**
     * Returns the sensor attached to a particular port. This method will not
     * return null. If a sensor has not previously been attached to the port, a
     * RawSensor will be created, attached and returned.
     *
     * @param <T> the sensor associated with the port
     * @param port the port associated with the requested sensor.
     * @return a valid Sensor object. If no sensor is current associated with
     * the port a RawSensor will be returned.
     */
    @SuppressWarnings("unchecked")
    public <T extends Sensor> T getSensor(SensorPort sensorPort) {
        int port = sensorPort.getPort();
        if (sensorType[port] == null) {
            LOGGER.debug("Uninitialized sensor: {}", port);
            sensorType[port] = new Sensor(SensorType.Raw);
        }
        return (T) sensorType[port];
    }

    /**
     * Set the motor at the particular port. There are currently four motor ports.
     *
     * @param motor the motor to associate with the port. May be null to clear
     * the motor configuration.
     * @param port the port. 
     */
    public void setMotor(MotorPort motorPort, int power) throws IOException {
                
        byte[] packet = new byte[] {
        		BPSPI_MESSAGE_TYPE.SET_MOTOR_POWER.getByte(),
                (byte)motorPort.getPort(),
                (byte)power
        };
        
        sendToBrickPi(spiAddresses[0], packet);           
        byte[] ret = readFromBrickPi(100);
    	if(verifyTransaction(ret)==false) {
    		throw new IOException("failed setSensor");
    	}
    }

    public void setUpdateDelay(int updateDelay) {
    }    
    
    /**
     * Decode an arbitrary number of bits from the bitset.
     *
     * @param bitLength the number of bits to decode
     * @param incoming the bitset to decode them from
     * @param startingBitLocation the starting bit location in the bitset
     * @return the decoded value
     */
    public static int decodeInt(int bitLength, byte[] incoming, int startingBitLocation) {
        int value = 0;
        while (bitLength-- > 0) {
            value <<= 1;
            int location = bitLength + startingBitLocation;
            boolean set = ((incoming[location / 8] & (1 << (location % 8))) != 0);
            if (set) {
                value |= 1;
            }
        }
        return value;
    }    
    
    /** Add a listener for update events.  This method only allows the same listener
     * to be added once.
     * @param listener a listener for update events.
     */ 
    public void addBrickPiUpdateListener(BrickPiUpdateListener listener) {
        if ( !listeners.contains(listener) ) {
            listeners.add(listener);
        }
    }

    /** Remove a listener for update events. 
     * @param listener a listener for update events.
     */ 
    public void removeBrickPiUpdateListener(BrickPiUpdateListener listener) {
        while ( listeners.contains(listener) ) {
            listeners.remove(listener);
        }
    }

}
