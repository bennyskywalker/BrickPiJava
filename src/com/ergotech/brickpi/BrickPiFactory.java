package com.ergotech.brickpi;

public class BrickPiFactory {
	
	private static BrickPiFactory piFactory = new BrickPiFactory();
	
	private BrickPiFactory() {	
	}
	
	public static BrickPiFactory getInstance() {
		return piFactory;
	}
	
	public IBrickPi getBrick(BrickPiConstants.COMTYPE comType) {
		IBrickPi retInterface;
		
		switch(comType) {
		case RS232:
			retInterface = BrickPi.getBrickPi();
			break;
		default:
		case SPI:
			retInterface = BrickPiSPI.getBrickPi();
			break;
		}
		
		return retInterface;
	}
}
