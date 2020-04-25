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

        GET_MANUFACTURER(1, 20), //1
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
		private final int payloadSize;
		
		BPSPI_MESSAGE_TYPE(int message) {
			this(message, 0);
		}
		
		BPSPI_MESSAGE_TYPE(int message, int payloadSize) {
			this.message = message;
			this.payloadSize = payloadSize;			
		}
		
		public int getInt() {
			return this.message;
		}
		
		public byte getByte() {
			return (byte)this.message;
		}
		
		public int getPayloadSize() {
			return this.payloadSize;
		}
	}
	
}
