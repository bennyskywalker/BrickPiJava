package com.ergotech.brickpi;

public class BrickPiFactory {
	
	private static BrickPiFactory piFactory = new BrickPiFactory();
	
	private BrickPiFactory() {	
	}
	
	public static BrickPiFactory getInstance() {
		return piFactory;
	}
	
	public IBrickPi getBrick(BrickPiConstants.COMTYPE comType) throws Exception {
		IBrickPi retInterface = null;
		
		switch(comType) {
		case RS232:
			//retInterface = BrickPi.getBrickPi();
			throw new Exception("Not Implemented");
			
		default:
		case SPI:
			retInterface = BrickPiSPI.getBrickPi((byte)0x01); //hardcode for now
			break;
		}
		
		return retInterface;
	}
}
