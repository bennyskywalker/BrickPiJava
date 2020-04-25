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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;
import com.pi4j.io.spi.SpiMode;

/**
 * This class provides utility method for communication with the brick pi.
 */
public abstract class BrickPiCommunications {

    private static final Logger LOGGER = 
            LoggerFactory.getLogger(BrickPiCommunications.class.getName());
    
    protected final SpiDevice spi;
    private final int HEADER_SIZE = 4;
    
    /**
     * The current debug level.
     */
    public static int DEBUG_LEVEL = 1;
    
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
    protected BrickPiCommunications(SpiChannel spiChannel) throws IOException {
    	
       	try {
    		spi = SpiFactory.getInstance(spiChannel, 
    									500000, 
   										SpiMode.MODE_0);
    		
    	} catch(Exception ex) {
    		LOGGER.error(ex.getMessage(), ex);
    		throw new IOException("Failed to open spi to BrickPi");
    	}
     	
    }
    
    protected byte[] buildByteMessageArray(int payloadSize) {
    	byte[] byteBuffer = new byte[HEADER_SIZE+payloadSize];
    	    	
    	return byteBuffer;
    }
   
    
    /**
     * Send a packet to the brick pi.
     *
     * @param destinationAddress
     * @param packet
     */
    protected byte[] sendToBrickPi(byte[] toSend) {
    	
    	byte resultBytes[] = new byte[] {};
    	    	
    	if (DEBUG_LEVEL > 0) {
            StringBuffer output = new StringBuffer();
            output.append("Sending");
            for (byte toAdd : toSend) {
                output.append(" ");
                output.append(Integer.toHexString(toAdd & 0xFF));
            }
            System.out.println(output.toString());
        }
    	
    	try {
        	resultBytes = spi.write(toSend);
        	
        	if (DEBUG_LEVEL > 0) {
                StringBuffer input = new StringBuffer();
                input.append("Received ");

                for (byte received : resultBytes) {
                    input.append(" ");
                    input.append(Integer.toHexString(received & 0xFF));
                }
                System.out.println(input.toString());
            }
        	
        }
        catch(IOException ex) {
    		LOGGER.error(ex.getMessage(), ex);        	
        }
    	
    	return resultBytes;
    }
    
    protected boolean verifyTransaction(byte [] result) {
    	String retCode = String.format("%02X",result[3]);
    	
    	if(DEBUG_LEVEL>0) {
    		System.out.println(retCode);	
    	}
    	
    	if(retCode.compareTo("A5")==0) {
    		return true;
    	}
    	
    	return false;
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
    

}
