package com.ergotech.brickpi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ergotech.brickpi.BrickPiConstants.BPSPI_MESSAGE_TYPE;
import com.ergotech.brickpi.motion.Motor;
import com.ergotech.brickpi.motion.MotorPort;
import com.ergotech.brickpi.motion.MotorStatus;
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
    
    //This will hold any configured motor ports - A, B, C, D = 0, 1, 2, 3
    //The configured motors will determine any direction and resolutions and
    //encoder returns
    private Motor[] motorPortSettings = new Motor[4];
    
    
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
    
    private byte getMotorPortsFromArray(MotorPort motorPorts[]) {
    	int motorPortByte = 0x00;
    	for(MotorPort motorPort : motorPorts) {
    		motorPortByte = motorPortByte | motorPort.getPort();
    	}
    	return (byte)motorPortByte;
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
     * Initialize the motor ports.
     */
    public void initializeMotor(MotorPort motorPort, Motor motor)
    {
    	int index = 0;
    	switch(motorPort) {
    	case MA:
    		index = 0;
    		break;
    	case MB:
    		index = 1;
    		break;
    	case MC:
    		index = 2;
    		break;
    	case MD:
    		index = 3;
    		break;
    	}
    	
    	motorPortSettings[index] = motor;    	
    	return;
    }
    
    public int getMBIndex(MotorPort mPort) throws IOException {
    	switch(mPort) {
    	case MA:
    		return 0;
    	case MB:
    		return 1;
    	case MC:
    		return 2;
    	case MD:
    		return 3;
    	default:
    		throw new IOException("Index out of bounds.");
    	}
    }
    
    /**
     * Set the motor at the particular port. There are currently four motor ports.
     *
     * @param motor the motor to associate with the port. May be null to clear
     * the motor configuration.
     * @param port the port. 
     */
    public void setMotor(MotorPort motorPort[], int power) throws IOException {
    	byte[] packet; 
        byte[] ret;
        Motor mConfig;
    	
    	for(MotorPort mPort:motorPort) {
    		mConfig = motorPortSettings[getMBIndex(mPort)];
    		if(mConfig==null) {
    			throw new IOException("Motor not initialize");
    		}
    		
    		packet = buildByteMessageArray(BPSPI_MESSAGE_TYPE.SET_MOTOR_POWER.getPayloadSize()); 
            
            packet[0] = brickPiAddress;
            packet[1] = BPSPI_MESSAGE_TYPE.SET_MOTOR_POWER.getByte();
            packet[2] = (byte)mPort.getPort();
            packet[3] = (byte)(power * mConfig.getDirectionVector());
            
            ret = sendToBrickPi(packet);
                    
        	if(verifyTransaction(ret)==false) {
        		throw new IOException("failed setSensor");
        	}    		
    	}
        
    }
    
    /*
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
    */        
    
    //Custom message
    public void getManufacturer() throws IOException {
    	byte[] packet = buildByteMessageArray(BPSPI_MESSAGE_TYPE.GET_MANUFACTURER.getPayloadSize());
    	
    	packet[0] = brickPiAddress;
    	packet[1] = BPSPI_MESSAGE_TYPE.GET_MANUFACTURER.getByte();
    	
    	byte[] result = sendToBrickPi(packet);
    	
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

	@Override
	public void setMotorPosition(MotorPort motorPort[], int position) throws IOException {
    	byte[] packet = buildByteMessageArray(BPSPI_MESSAGE_TYPE.SET_MOTOR_POSITION.getPayloadSize());
    	
    	packet[0] = brickPiAddress;
    	packet[1] = BPSPI_MESSAGE_TYPE.SET_MOTOR_POSITION.getByte();
    	packet[2] = (byte)getMotorPortsFromArray(motorPort);
    	packet[3] = (byte)((position >> 24) & 0xFF);
    	packet[4] = (byte)((position >> 16) & 0xFF);
    	packet[5] = (byte)((position >> 8) & 0xFF);
    	packet[6] = (byte)(position & 0xFF);
    	byte[] result = sendToBrickPi(packet);
    	
    	if(verifyTransaction(result)==false) {
    		throw new IOException("get Manufacturer failed");
    	}
	}

	@Override
	public void setMotorEncoderOffset(MotorPort motorPort[], int offset) throws IOException {
    	byte[] packet = buildByteMessageArray(BPSPI_MESSAGE_TYPE.OFFSET_MOTOR_ENCODER.getPayloadSize());

		packet[0] = brickPiAddress;
		packet[1] = BPSPI_MESSAGE_TYPE.OFFSET_MOTOR_ENCODER.getByte();
		packet[2] = (byte)getMotorPortsFromArray(motorPort);
		packet[3] = (byte)((offset >> 24) & 0xFF);
		packet[4] = (byte)((offset >> 16) & 0xFF);
		packet[5] = (byte)((offset >> 8) & 0xFF);
		packet[6] = (byte)(offset & 0xFF);
		
		byte[] result = sendToBrickPi(packet);
    	
    	if(verifyTransaction(result)==false) {
    		throw new IOException("get Manufacturer failed");
    	}
	}
	
	@Override
	public void setMotorPositionRelative(MotorPort motorPorts[], int degrees) throws IOException {
		for(MotorPort motorPort : motorPorts) {
			// assign error to the error value returned by get_motor_encoder, and if not 0:
			int encoder = getMotorEncoder(motorPort);
		
			// assign error to the error value returned by get_motor_encoder, and if not 0:
			setMotorPosition(new MotorPort[] {motorPort}, (encoder + degrees));
	    }
	}	

	@Override
	public int getMotorEncoder(MotorPort motorPort) throws IOException {
		int value = 0;
		BPSPI_MESSAGE_TYPE msg = getMotorEncodeFromPort(motorPort);
    	byte[] packet = buildByteMessageArray(msg.getPayloadSize());

		packet[0] = brickPiAddress;
		packet[1] = msg.getByte();
		
		byte[] result = sendToBrickPi(packet);
    	
    	if(verifyTransaction(result)==true) {
    		//If this result verifies
    		value = ((result[4] << 24) | 
    				(result[5] << 16) | 
    				(result[6] << 8) | 
    				result[7]);
    	}
    	
		return value;
	}

	@Override
	public MotorStatus getMotorStatus(MotorPort motorPort) throws IOException {
		BPSPI_MESSAGE_TYPE msg = getMotorStatusFromPort(motorPort);
    	byte[] packet = buildByteMessageArray(msg.getPayloadSize());		
		packet[0] = brickPiAddress;
		packet[1] = msg.getByte();		
		byte[] result = sendToBrickPi(packet);

		int state = result[4];
		int power = result[5];
		int position = ((result[6] << 24) | 
						(result[7] << 16) | 
						(result[8] << 8) | 
						result[9]);
		int dps = ((result[10] << 8) | result[11]);
		
		MotorStatus motorStatus = new MotorStatus(state, power, position, dps);
		return motorStatus;
	}
	
	private BPSPI_MESSAGE_TYPE getMotorEncodeFromPort(MotorPort motorPort) throws IOException {
		BPSPI_MESSAGE_TYPE msg;
		switch(motorPort) {
		case MA:
			msg = BPSPI_MESSAGE_TYPE.GET_MOTOR_A_ENCODER;
			break;
		case MB:
			msg = BPSPI_MESSAGE_TYPE.GET_MOTOR_B_ENCODER;
			break;
		case MC:
			msg = BPSPI_MESSAGE_TYPE.GET_MOTOR_C_ENCODER;
			break;
		case MD:
			msg = BPSPI_MESSAGE_TYPE.GET_MOTOR_D_ENCODER;
			break;
		default:
			throw new IOException("Incorrect Port");
		}
		return msg;
	}
	
	private BPSPI_MESSAGE_TYPE getMotorStatusFromPort(MotorPort motorPort) throws IOException {
		BPSPI_MESSAGE_TYPE msg;
		switch(motorPort) {
		case MA:
			msg = BPSPI_MESSAGE_TYPE.GET_MOTOR_A_STATUS;
			break;
		case MB:
			msg = BPSPI_MESSAGE_TYPE.GET_MOTOR_B_STATUS;
			break;
		case MC:
			msg = BPSPI_MESSAGE_TYPE.GET_MOTOR_C_STATUS;
			break;
		case MD:
			msg = BPSPI_MESSAGE_TYPE.GET_MOTOR_D_STATUS;
			break;
		default:
			throw new IOException("Incorrect Port");
		}
		return msg;
	}	
}
