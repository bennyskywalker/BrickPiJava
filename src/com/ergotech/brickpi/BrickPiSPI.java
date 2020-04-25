package com.ergotech.brickpi;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;
import com.pi4j.io.spi.SpiMode;

public class BrickPiSPI extends BrickPiCommunications implements IBrickPi {

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
    public static IBrickPi getBrickPi() {
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
    	byte[] sendTest = new byte[] {0x01, 21, 15, 0}; 
    			//{0x01, 0x01, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    	byte[] result = spi.write(sendTest);
    	
    	String retCode = String.format("%02X",result[3]);
    	System.out.println(retCode);
    	if(retCode.compareTo("A5")==0) {
    		for(int i=4;i<result.length;i++) {
    			System.out.print(Character.toString((char)result[i]));		
    		}
    		System.out.println("");
    	}
    	
    	try {
    		Thread.sleep(5000);
    	} catch(Exception ex) {}
    	System.exit(0);
    	return;
    }


    /**
     * Send a packet to the brick pi.
     *
     * @param destinationAddress
     * @param packet
     */
    protected void sendToBrickPi(byte destinationAddress, byte[] packet) {
    	byte toSend[] = new byte[2+packet.length];
    	
    	toSend[0] = destinationAddress;
    	System.arraycopy(packet, 0, toSend, 1, packet.length);
    	
    	if (DEBUG_LEVEL > 0) {
            StringBuffer output = new StringBuffer();
            output.append("Sending");
            for (byte toAdd : toSend) {
                output.append(" ");
                output.append(Integer.toHexString(toAdd & 0xFF));
            }
            System.out.println(output.toString());
        }
    	
    	try {
        	resultBytes = new byte[] {};
        	resultBytes = spi.write(toSend);
        }
        catch(IOException ex) {
    		LOGGER.error(ex.getMessage(), ex);        	
        }
    }

    protected byte[] readFromBrickPi(int timeout) throws IOException { // timeout in mS
        
    	if (DEBUG_LEVEL > 0) {
            StringBuffer input = new StringBuffer();
            input.append("Received ");

            for (byte received : resultBytes) {
                input.append(" ");
                input.append(Integer.toHexString(received & 0xFF));
            }
            System.out.println(input.toString());
        }
    	
    	return resultBytes;
    }
}
