package cjohannsen;

import cjohannsen.protocol.MessageCodec;
import cjohannsen.protocol.Messages;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SimpitHost {
    static final Logger logger = LoggerFactory.getLogger(SimpitHost.class);

    private final SerialPort serialPort;
    private final MessageCodec messageCodec;

    public static final String KERBALSIMPIT_VERSION = "1.1.3";
    public static final int HANDSHAKE_SYN_LENGTH = KERBALSIMPIT_VERSION.length() + 2;

    public static final int HANDSHAKE_ACK_MIN_LENGTH = 5;

    public static final byte HANDSHAKE_SYN = 0x00;
    public static final byte HANDSHAKE_ACK = 0x01;
    public static final byte HANDSHAKE_SYNACK = 0x02;

    @Autowired
    public SimpitHost(final SerialPort serialPort, final MessageCodec messageCodec) {
        this.serialPort = serialPort;
        this.messageCodec = messageCodec;
    }

    public boolean handshake() {
        // Handshake protocol:
        // SEND:
        //   0xAA
        //   0x50
        //   MESSAGE_SIZE
        //   0x00 (MESSAGE_TYPE for HANDSHAKE)
        //   KERBALSIMPIT_VERSION
        //   0x00 SYN
        // EXPECT:
        //   0xAA 
        //   0x50 
        //   MESSAGE_SIZE
        //   MESSAGE_TYPE 
        //   0x01 SYNACK

        try {
            int bytesRead = 0;
            byte[] incomingBytes = new byte[64];
            while (bytesRead < HANDSHAKE_ACK_MIN_LENGTH) {
                long startTime = -1;
                while (serialPort.bytesAvailable() == 0) {
                    if (startTime == -1 || System.currentTimeMillis() - startTime > 3000) {
                        logger.info("SimpitHost initiating handshake...");
                        byte[] message = messageCodec.encodeMessage(Messages.Common.SYNC_MESSAGE, KERBALSIMPIT_VERSION.getBytes());
                        serialPort.writeBytes(message, message.length);
                        logger.info("Waiting for ACK...");
                        startTime = System.currentTimeMillis();
                    }
                    Thread.sleep(20);
                }
                byte[] readBuffer = new byte[serialPort.bytesAvailable()];
                int numRead = serialPort.readBytes(readBuffer, readBuffer.length);
                System.out.println("Read " + numRead + " bytes.");
                logBuffer(readBuffer);
                for (final byte b : readBuffer) {
                    incomingBytes[bytesRead++] = b;
                }
            }
            byte[] ackMessage = messageCodec.decodeMessage(incomingBytes);
            if (ackMessage[0] == HANDSHAKE_ACK) {
                logger.info("ACK received, sending SYNACK...");
                byte[] synack = messageCodec.encodeMessage(Messages.Common.SYNC_MESSAGE, HANDSHAKE_SYNACK);
                serialPort.writeBytes(synack, 1);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return setupDataListener();
    }

    private boolean setupDataListener() {

        logger.info("Initializing KerbalSimpit. Registering serial port data listener - ");
        final boolean success = this.serialPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }
            @Override
            public void serialEvent(SerialPortEvent event)
            {
                String eventType = "UNKNOWN";
                switch (event.getEventType()) {
                    case SerialPort.LISTENING_EVENT_DATA_AVAILABLE:
                        eventType = "DATA_AVAILABLE";
                        break;
                    case SerialPort.LISTENING_EVENT_DATA_RECEIVED:
                        eventType = "DATA_RECEIVED";
                        break;
                    case SerialPort.LISTENING_EVENT_DATA_WRITTEN:
                        eventType = "DATA_WRITTEN";
                        break;
                }
                logger.info("New event of type: " + eventType);
                if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                    return;
                final int bytesAvailable = serialPort.bytesAvailable();
                logger.info(bytesAvailable + " bytes available.");
                final byte[] newData = new byte[bytesAvailable];
                int numRead = serialPort.readBytes(newData, newData.length);
                logger.info("Read " + numRead + " bytes.");
                logBuffer(newData);
            }
        });

        if (success) {
            logger.info("Data listener registered.  KerbalSimpit ready.");
        }
        else {
            logger.error("Failed to add serial data listener!");
        }

        return success;
    }

    private static final byte[] synPacket() {
        final byte[] syn = new byte[HANDSHAKE_SYN_LENGTH];
        int i = 0;
        syn[i++] = 0x00;
        for(int j = 0; j < KERBALSIMPIT_VERSION.length(); j++) {
            syn[i++] = (byte)KERBALSIMPIT_VERSION.charAt(j);
        }
        syn[i] = HANDSHAKE_SYN;
        return syn;
    }

    private static final void logBuffer(final byte[] buffer) {
        StringBuilder sb = new StringBuilder();
        for (byte b : buffer) {
            sb.append(String.format("%02X ", b));
        }
        logger.info(sb.toString());
    }
}
