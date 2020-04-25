package com.ergotech.brickpi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ergotech.brickpi.BrickPiConstants.BPSPI_MESSAGE_TYPE;
import com.ergotech.brickpi.motion.MotorPort;
import com.ergotech.brickpi.sensors.Sensor;
import com.ergotech.brickpi.sensors.SensorPort;
import com.pi4j.io.spi.SpiChannel;

public class BrickPiSPI extends BrickPiCommunications implements IBrickPi {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrickPi.class.getName());
    private final byte brickPiAddress = 0x01;

    /**
     * The singleton instance of this class.
     */
    protected static BrickPiSPI brickPi;
    
    private Map<SensorPort, Sensor> sensorMap;
    
    

    /**
     * Return the brick pi singleton.
     *
     * @return the brick pi instance.
     */
    public static IBrickPi getBrickPi() {
        if (brickPi == null) {
            try {
                // we'll try/catch the exception and log it here.
                // the "getBrickPi" could be called often and should not
                // fail (at least after initial debugging) and catch the 
                // exception externally might be irritating after a while...
                brickPi = new BrickPiSPI();

            } catch (IOException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
        return brickPi;
    }
    
    public BrickPiSPI() throws IOException {
    	this(SpiChannel.CS1);
    	
		getManufacturer(brickPiAddress);
    }
    
    public BrickPiSPI(SpiChannel spiChannel) throws IOException {
    	super(spiChannel);
    	
    	sensorMap = new HashMap<SensorPort, Sensor>();
    }

    /**
     * Set the sensor at the particular port. There are current four sensor
     * ports.
     *
     * @param sensor the sensor to associate with the port. May be null to clear
     * the sensor configuration.
     * @param port the port.
     */
    public void setSensor(byte address, Sensor sensor, SensorPort port) throws IOException {
    	        
    	//Keep internal structure
    	sensorMap.put(port, sensor);
    	
    	//Setup in the Brick Pi
        byte[] packet = buildByteMessageArray(BPSPI_MESSAGE_TYPE.SET_SENSOR_TYPE.getPayloadSize());
        packet[0] = address;
        packet[1] = BPSPI_MESSAGE_TYPE.SET_SENSOR_TYPE.getByte();
        packet[3] = (byte)port.getPort();
        packet[4] = (byte)sensor.getSensorType();
        
        byte [] ret = sendToBrickPi(packet);           
        
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
    public <T extends Sensor> T getSensor(byte address, SensorPort sensorPort) {
    	
    	Sensor sensor = sensorMap.get(sensorPort);
        int port = sensorPort.getPort();
        
        //byte[] packet = buildByteMessageArray()
        
        /*
        if (sensorType[port] == null) {
            LOGGER.debug("Uninitialized sensor: {}", port);
            sensorType[port] = new Sensor(SensorType.Raw);
        }
        */
        return (T) sensor;
    }    

    /**
     * Set the motor at the particular port. There are currently four motor ports.
     *
     * @param motor the motor to associate with the port. May be null to clear
     * the motor configuration.
     * @param port the port. 
     */
    public void setMotor(byte address, MotorPort motorPort, int power) throws IOException {
                
        byte[] packet = buildByteMessageArray(BPSPI_MESSAGE_TYPE.SET_MOTOR_POWER.getPayloadSize()); 
        
        packet[0] = address;
        packet[1] = BPSPI_MESSAGE_TYPE.SET_MOTOR_POWER.getByte();
        packet[2] = (byte)motorPort.getPort();
        packet[3] = (byte)power;
        
        byte[] ret =sendToBrickPi(packet);           
        
    	if(verifyTransaction(ret)==false) {
    		throw new IOException("failed setSensor");
    	}
    }
        
    
    //Custom message
    public void getManufacturer(byte address) throws IOException {
    	byte[] sendTest = buildByteMessageArray(BPSPI_MESSAGE_TYPE.GET_MANUFACTURER.getPayloadSize());
    	
    	sendTest[0] = address;
    	sendTest[1] = BPSPI_MESSAGE_TYPE.GET_MANUFACTURER.getByte();
		//{address, 21, 15, 0}; motor test 
		//{address, 0x01, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    	
    	byte[] result = spi.write(sendTest);
    	
    	String retCode = String.format("%02X",result[3]);
    	System.out.println(retCode);
    	if(retCode.compareTo("A5")==0) {
    		for(int i=4;i<result.length;i++) {
    			System.out.print(Character.toString((char)result[i]));		
    		}
    		System.out.println("");
    	}
    	
    	try {
    		Thread.sleep(5000);
    	} catch(Exception ex) {}
    	System.exit(0);
    	return;
    }

}
