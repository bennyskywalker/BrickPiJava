package com.ergotech.brickpi;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;
import com.pi4j.io.spi.SpiMode;

public class BrickPiSPI extends BrickPiCommunications {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrickPi.class.getName());

    /**
     * The singleton instance of this class.
     */
    protected static BrickPiSPI brickPi;
    
    protected final SpiDevice spi;
    private byte[] resultBytes;

    /**
     * Return the brick pi singleton.
     *
     * @return the brick pi instance.
     */
    public static BrickPiSPI getBrickPi() {
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
    }
    
    public BrickPiSPI(SpiChannel spiChannel) throws IOException {
    	try {
    		spi = SpiFactory.getInstance(spiChannel, 
    									500000, 
   										SpiMode.MODE_0);
    		
    		sendCustom();
    	} catch(Exception ex) {
    		LOGGER.error(ex.getMessage(), ex);
    		throw new IOException("Failed to open spi to BrickPi");
    	}
    }
    
    /*
    public static short ADC_CHANNEL_COUNT = 8;
    private void read() throws IOException, InterruptedException {
    	for(short channel=0;channel<ADC_CHANNEL_COUNT;channel++) {
    		int conversion_value = getConversionValue(channel);
    		System.out.println(String.format(" | %04d", conversion_value));
    	}
    }
    
    private int getConversionValue(short channel) throws IOException {
    	byte data[] = new byte[] {
    			(byte) 0b00000001,
    			(byte)(0b00000000 | ((channel&7)<<4)),
    			(byte) 0b00000000
    	};
    	
    	byte[] result = spi.write(data);
    	
    	int value = (result[1]<<8) & 0b1100000000;
    	value |= (result[2] & 0xff);
    	return value;
   	}
   	*/
    
    @Override
    public void setTimeout(long timeout) throws IOException {
    	return;
    }
    
    //Custom message
    public void sendCustom() throws IOException {
    	byte[] sendTest = new byte[] {0x01, 22, 0x01, 25}; 
    			//{0x01, 0x02, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    	byte[] result = spi.write(sendTest);
    	System.out.println(result);
    	return;
    }


    /**
     * Send a packet to the brick pi.
     *
     * @param destinationAddress
     * @param packet
     */
    protected void sendToBrickPi(byte destinationAddress, byte[] packet) {
        // clear the read buffer before writing...
        //serial.flush();
        // the checksum is the sum of all the bytes in the entire packet EXCEPT the checksum
        int checksum = destinationAddress + packet.length;
        for (byte toAdd : packet) {
            checksum += (int) (toAdd & 0xFF);
        }
        byte[] toSend = new byte[packet.length + 3];
        System.arraycopy(packet, 0, toSend, 3, packet.length);
        toSend[0] = destinationAddress;
        toSend[1] = (byte) (checksum & 0xFF);  // checksum...
        toSend[2] = (byte) (packet.length & 0xFF);
        if (DEBUG_LEVEL > 0) {
            StringBuffer output = new StringBuffer();
            output.append("Sending");
            for (byte toAdd : toSend) {
                output.append(" ");
                output.append(Integer.toHexString(toAdd & 0xFF));
            }
            System.out.println(output.toString());
        }
        
        //serial.write(toSend);
        try {
        	resultBytes = new byte[] {};
        	resultBytes = spi.write(toSend);
        }
        catch(IOException ex) {
    		LOGGER.error(ex.getMessage(), ex);        	
        }
        
        //serial.write(packet);
    }

    protected byte[] readFromBrickPi(int timeout) throws IOException { // timeout in mS
    	return resultBytes;
    }
    
    /**
     * Read a packet from the brick pi.
     *
     * @param timeout total read timeout in ms
     * @return the packet read from the serial port/brickpi
     * @throws java.io.IOException thrown if there's a timeout reading the port.
     */
    
    /*
    protected byte[] readFromBrickPi(int timeout) throws IOException { // timeout in mS

        int delay = timeout / 5;  // we'll wait a maximum of timeout
        while (serial.availableBytes() < 2 && delay-- >= 0) { // we need at least the checksum and bytecount (2 bytes)
            LOGGER.debug("Available: {}", serial.availableBytes());
            try {
                Thread.sleep(5);  // 5ms

            } catch (InterruptedException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }

        if (serial.availableBytes() < 1) {
            throw new IOException("Read timed out - Header");
        }

        // the first byte of the received packet in the checksum.
        // the second is the number of bytes in the packet.
        byte checksum = (byte) serial.read();
        byte packetSize = (byte) serial.read();  // the packet size does not include this two byte header.
        int inCheck = packetSize;  // the incoming checksum does not include the checksum...

        // so, we have packetSize bytes left to read.
        // delay should still be good.  If we had to wait above, it will be less than timeout/5
        // but the overall timeout in the method should still max out at timeout.
        while (serial.availableBytes() < packetSize && delay-- >= 0) { // we need at least the checksum and bytecount (2 bytes)
            try {
                Thread.sleep(5);  // 5ms

            } catch (InterruptedException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }

        if (serial.availableBytes() < packetSize) {
            throw new IOException("Read timed out - Packet");
        }

        byte[] packet = new byte[packetSize];
        for (int counter = 0; counter < packetSize; counter++) {
            packet[counter] = (byte) (serial.read() & 0xFF);
            inCheck += (int) (packet[counter] & 0xFF);
        }
        if (DEBUG_LEVEL > 0) {
            StringBuffer input = new StringBuffer();
            input.append("Received ");
            input.append(Integer.toHexString(checksum & 0xFF));
            input.append(" ");
            input.append(Integer.toHexString(packetSize & 0xFF));
            for (byte received : packet) {
                input.append(" ");
                input.append(Integer.toHexString(received & 0xFF));
            }
            System.out.println(input.toString());
        }

        if ((inCheck & 0xFF) != (checksum & 0xFF)) {
            throw new IOException("Bad Checksum " + inCheck + " expected " + checksum);
        }
        // if we get to here, all is well.
        return packet;
    }
	*/

}
