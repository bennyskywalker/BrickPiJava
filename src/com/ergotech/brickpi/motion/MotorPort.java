package com.ergotech.brickpi.motion;

/**
 * An enumeration of BrickPi motor ports.
 * @author sdaubin
 *
 */
public enum MotorPort {
    
    MA(1),
    MB(2),
    MC(4),
    MD(8);

    private final int port;
    
    private MotorPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }
   
}
