package com.ergotech.brickpi;

public class BrickPiConstants {
	
	public enum COMTYPE {
		SPI,
		RS232
	}

	public final static class PORTS {
		public final static int PORT_A = 0x01;
		public final static int PORT_B = 0x02;
		public final static int PORT_C = 0x04;
		public final static int PORT_D = 0x08;
		
		public final static int PORT_1 = 0x01;
		public final static int PORT_2 = 0x02;
		public final static int PORT_3 = 0x04;
		public final static int PORT_4 = 0x08;
	}
	
	public enum BPSPI_MESSAGE_TYPE {
        NONE(0), //0

        GET_MANUFACTURER(1), //1
        GET_NAME(2),
        GET_HARDWARE_VERSION(3),
        GET_FIRMWARE_VERSION(4),
        GET_ID(5),
        SET_LED(6),
        GET_VOLTAGE_3V3(7),
        GET_VOLTAGE_5V(8),
        GET_VOLTAGE_9V(9),
        GET_VOLTAGE_VCC(10),
        SET_ADDRESS(11), //11

        SET_SENSOR_TYPE(12), //12

        GET_SENSOR_1(13), //13
        GET_SENSOR_2(14),
        GET_SENSOR_3(15),
        GET_SENSOR_4(16),

        I2C_TRANSACT_1(17), //17
        I2C_TRANSACT_2(18),
        I2C_TRANSACT_3(19),
        I2C_TRANSACT_4(20),

        SET_MOTOR_POWER(21),

        SET_MOTOR_POSITION(22),

        SET_MOTOR_POSITION_KP(23),

        SET_MOTOR_POSITION_KD(24), //24

        SET_MOTOR_DPS(25), //25

        SET_MOTOR_DPS_KP(26),

        SET_MOTOR_DPS_KD(27),

        SET_MOTOR_LIMITS(28),

        OFFSET_MOTOR_ENCODER(29), //29

        GET_MOTOR_A_ENCODER(30), //30
        GET_MOTOR_B_ENCODER(31),
        GET_MOTOR_C_ENCODER(32),
        GET_MOTOR_D_ENCODER(33),

        GET_MOTOR_A_STATUS(34), //34
        GET_MOTOR_B_STATUS(35),
        GET_MOTOR_C_STATUS(36),
        GET_MOTOR_D_STATUS(37);
		
		private final int message;
		
		BPSPI_MESSAGE_TYPE(int message) {
			this.message = message;
		}
		
		public int getInt() {
			return this.message;
		}
		
		public byte getByte() {
			return (byte)this.message;
		}
	}
	
	public enum SENSOR_TYPE {
        NONE(1),
        I2C(2),
        CUSTOM(3),

        TOUCH(4),
        NXT_TOUCH(5),
        EV3_TOUCH(6),

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
		SENSOR_TYPE(int type) {
			this.type = type;
		}
		
		public int getInt() {
			return this.type;
		}
	}
}
