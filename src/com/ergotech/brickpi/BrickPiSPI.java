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
import com.ergotech.brickpi.sensors.SensorType;
import com.pi4j.io.spi.SpiChannel;

public class BrickPiSPI extends BrickPiCommunications implements IBrickPi {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrickPiSPI.class.getName());
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
    public static IBrickPi getBrickPi(byte address) {
        if (brickPi == null) {
            try {
                // we'll try/catch the exception and log it here.
                // the "getBrickPi" could be called often and should not
                // fail (at least after initial debugging) and catch the 
                // exception externally might be irritating after a while...
                brickPi = new BrickPiSPI(address, SpiChannel.CS1);

            } catch (IOException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
        return brickPi;
    }
        
    private BrickPiSPI(byte address, SpiChannel spiChannel) throws IOException {
    	super(spiChannel);
    	
    	sensorMap = new HashMap<SensorPort, Sensor>();
		getManufacturer();
		
		//motorTest();
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
    	        
    	//Keep internal structure
    	sensorMap.put(port, sensor);
    	
    	//Setup in the Brick Pi
        byte[] packet = buildByteMessageArray(BPSPI_MESSAGE_TYPE.SET_SENSOR_TYPE.getPayloadSize());
        packet[0] = brickPiAddress;
        packet[1] = BPSPI_MESSAGE_TYPE.SET_SENSOR_TYPE.getByte();
        packet[2] = (byte)port.getPort();
        packet[3] = (byte)sensor.getSensorType();
        
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
    public <T extends Sensor> T getSensor(SensorPort sensorPort) throws IOException {
    	
    	Sensor sensor = sensorMap.get(sensorPort);
    	SensorType sensorType = sensor.getSensorTypeEnum();
        int port = sensorPort.getPort();
        BPSPI_MESSAGE_TYPE sensorMessage = MapSensorPortToSPICommand(sensorPort);
        byte sensorCommand = sensorMessage.getByte();
        int payloadSize = sensorType.getPayloadSize();
        
        byte[] packet = buildByteMessageArray(payloadSize);

        packet[0] = brickPiAddress;
        packet[1] = sensorCommand;
        
        byte[] result = sendToBrickPi(packet);
        
        if(verifyTransaction(result)) {
        	//Set the Sensor result value
        	//Get the value for the sensor
        	//Need to hand this in for the appropriate decoding type
        	sensor.processResult(result);
        }
        
        return (T) sensor;
    }

    private BPSPI_MESSAGE_TYPE MapSensorPortToSPICommand(SensorPort sensorPort) throws IOException {
    	BPSPI_MESSAGE_TYPE spiCommand;
    	switch(sensorPort) {
    	case S1:
    		spiCommand = BPSPI_MESSAGE_TYPE.GET_SENSOR_1;
    		break;
    	case S2:
    		spiCommand = BPSPI_MESSAGE_TYPE.GET_SENSOR_2;
    		break;
    	case S3:
    		spiCommand = BPSPI_MESSAGE_TYPE.GET_SENSOR_3;
    		break;
    	case S4:
    		spiCommand = BPSPI_MESSAGE_TYPE.GET_SENSOR_4;
    		break;
    	default:
    		throw new IOException("Unknown Command");
    	}
    	return spiCommand;
    }
    
    /**
     * Decode the sensor type and determine the payload
     * @param sensor
     * @return
     */
    private int SensorPayload(Sensor sensor) {
    	int payloadLength = 0;
    	SensorType sensorEnum = sensor.getSensorTypeEnum(); 
    	
    	switch(sensorEnum) {
    	case TOUCH:
    	case EV3_TOUCH:
    		payloadLength = sensorEnum.getPayloadSize();
    		break;
		default:
			payloadLength = 0;
			break;
    	}
    	
    	return payloadLength;
    }

    /**
     * Set the motor at the particular port. There are currently four motor ports.
     *
     * @param motor the motor to associate with the port. May be null to clear
     * the motor configuration.
     * @param port the port. 
     */
    public void setMotor(MotorPort motorPort, int power) throws IOException {
                
        byte[] packet = buildByteMessageArray(BPSPI_MESSAGE_TYPE.SET_MOTOR_POWER.getPayloadSize()); 
        
        packet[0] = brickPiAddress;
        packet[1] = BPSPI_MESSAGE_TYPE.SET_MOTOR_POWER.getByte();
        packet[2] = (byte)motorPort.getPort();
        packet[3] = (byte)power;
        
        byte[] ret = sendToBrickPi(packet);
                
    	if(verifyTransaction(ret)==false) {
    		throw new IOException("failed setSensor");
    	}
    }
    
    public void motorTest() {
    	byte[] packet = new byte[]{0x01, 21, 15, 30};
    	
    	try {
    		byte[] result = sendToBrickPi(packet);    	
    
    		Thread.sleep(5000);
    		
    		packet = new byte[]{0x01, 21, 15, 0};
    		result = sendToBrickPi(packet);
    	}
    	catch(Exception ex) {}
    }
        
    
    //Custom message
    public void getManufacturer() throws IOException {
    	byte[] sendTest = buildByteMessageArray(BPSPI_MESSAGE_TYPE.GET_MANUFACTURER.getPayloadSize());
    	
    	sendTest[0] = brickPiAddress;
    	sendTest[1] = BPSPI_MESSAGE_TYPE.GET_MANUFACTURER.getByte();
		//{address, 21, 15, 0}; motor test 
		//{address, 0x01, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    	
    	byte[] result = sendToBrickPi(sendTest);
    	
    	if(verifyTransaction(result)==false) {
    		throw new IOException("get Manufacturer failed");
    	}

    	if(DEBUG_LEVEL>0) {
    		for(int i=4;i<result.length;i++) {
    			System.out.print(Character.toString((char)result[i]));		
    		}
    		System.out.println("");    		
    	}
    	
    	return;
    }

}
