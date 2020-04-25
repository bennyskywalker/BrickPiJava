package com.ergotech.brickpi.sensors;

/**
 * An enumeration of BrickPi sensor ports.
 * @author sdaubin
 *
 */
public enum SensorPort {
    
    S1(1),
    S2(2),
    S3(3),
    S4(8);

    private final int port;
    
    private SensorPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }
}
