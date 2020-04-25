package com.ergotech.brickpi.sensors;

/**
 * An enumeration of sensor types.  The main thing we need to know to operate a sensor
 * is its type id and the number of bits that the sensor decodes on update.
 * @author sdaubin
 *
 */
public enum SensorType {
    NONE(1),
    I2C(2),
    CUSTOM(3),

    TOUCH(4,3, 6),
    NXT_TOUCH(5),
    EV3_TOUCH(6,3,6),

    NXT_LIGHT_ON(7),
    NXT_LIGHT_OFF(8),

    NXT_COLOR_RED(9),
    NXT_COLOR_GREEN(10),
    NXT_COLOR_BLUE(11),
    NXT_COLOR_FULL(12),
    NXT_COLOR_OFF(13),

    NXT_ULTRASONIC(14),

    EV3_GYRO_ABS(15),
    EV3_GYRO_DPS(16),
    EV3_GYRO_ABS_DPS(17),

    EV3_COLOR_REFLECTED(18),
    EV3_COLOR_AMBIENT(19),
    EV3_COLOR_COLOR(20),
    EV3_COLOR_RAW_REFLECTED(21),
    EV3_COLOR_COLOR_COMPONENTS(22),

    EV3_ULTRASONIC_CM(23),
    EV3_ULTRASONIC_INCHES(24),
    EV3_ULTRASONIC_LISTEN(25),

    EV3_INFRARED_PROXIMITY(26),
    EV3_INFRARED_SEEK(27),
    EV3_INFRARED_REMOTE(28);
	
	private int type;
	private int payloadSize;
	private int resultIndex;
	
	SensorType(int type) {
		this(type, 0, 0);
	}
	
	SensorType(int type, int payloadSize) {
		this(type, payloadSize, 0);
	}


    private SensorType(int type, int payloadSize, int resultIndex) {
        this.type = (byte)type;
        this.payloadSize = payloadSize;
    }
	
	public int getInt() {
		return this.type;
	}

    public byte getType() {
        return (byte)type;
    }

    public int getPayloadSize() {
        return payloadSize;
    }    
    
    public int getDecodeBitCount() {
    	return 0;
    }
}
