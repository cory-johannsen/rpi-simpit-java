package cjohannsen;

import cjohannsen.protocol.Packet;
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

    public static final int HANDSHAKE_ACK_MIN_LENGTH = 5;

    public static final byte HANDSHAKE_SYN = 0x00;
    public static final byte HANDSHAKE_ACK = 0x01;
    public static final byte HANDSHAKE_SYNACK = 0x02;

    public static final String KERBALSIMPIT_VERSION = "1.1.3";

    private static final byte[] SYN = {
            HANDSHAKE_SYN,
            KERBALSIMPIT_VERSION.getBytes()[0],
            KERBALSIMPIT_VERSION.getBytes()[0],
            KERBALSIMPIT_VERSION.getBytes()[0],
            KERBALSIMPIT_VERSION.getBytes()[0],
            KERBALSIMPIT_VERSION.getBytes()[0],
            0x00
    };




    public static final long POLL_INTERVAL_MILLIS = 50;

    @Autowired
    public SimpitHost(final SerialPort serialPort) {
        this.serialPort = serialPort;
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
        int bytesRead = 0;
        byte[] incomingBytes = new byte[64];
        while (bytesRead < HANDSHAKE_ACK_MIN_LENGTH) {
            long startTime = -1;
            while (serialPort.bytesAvailable() == 0) {
                if (startTime == -1 || System.currentTimeMillis() - startTime > 10000) {
                    logger.info("SimpitHost initiating handshake...");
                    byte[] message = Packet.encodePacket(Messages.Type.SYNC_MESSAGE, SYN);
                    logger.info(hexString(message));
                    serialPort.writeBytes(message, message.length);
                    logger.info("Waiting for ACK...");
                    startTime = System.currentTimeMillis();
                }
                try {
                    Thread.sleep(POLL_INTERVAL_MILLIS);
                } catch (InterruptedException e) {
                    // NO-OP
                }
            }
            byte[] readBuffer = new byte[serialPort.bytesAvailable()];
            int numRead = serialPort.readBytes(readBuffer, readBuffer.length);
            logger.info("Read " + numRead + " bytes.");
            logger.info(hexString(readBuffer));
            for (final byte b : readBuffer) {
                incomingBytes[bytesRead++] = b;
            }
        }
        Packet ackMessage = Packet.decodePacket(incomingBytes);
        logger.info("Decoded message of type " + ackMessage.getType());
        logger.info(hexString(ackMessage.getPayload()));
        byte ackByte = ackMessage.getPayload()[0];
        logger.info("ack byte: " + ackByte);
        if (ackByte == HANDSHAKE_ACK) {
            logger.info("ACK received, sending SYNACK...");
            byte[] synack = Packet.encodePacket(Messages.Type.SYNC_MESSAGE, HANDSHAKE_SYNACK);
            serialPort.writeBytes(synack, 1);
        }


        return setupDataListener();
    }

    public boolean sendEchoRequest(String echoMessage) {
        byte[] buffer = Packet.encodePacket(Messages.Type.ECHO_REQ_MESSAGE, echoMessage.getBytes());
        logger.info(hexString(buffer));
        return serialPort.writeBytes(buffer, buffer.length) == buffer.length;
    }

    private boolean setupDataListener() {

        logger.info("Initializing KerbalSimpit. Registering serial port data listener - ");
        final boolean success = this.serialPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }
            @Override
            public void serialEvent(SerialPortEvent event)
            {
                logger.info("New event of type: " + printableEventType(event));
                if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                    return;
                final int bytesAvailable = serialPort.bytesAvailable();
                logger.info(bytesAvailable + " bytes available.");
                final byte[] newData = new byte[bytesAvailable];
                int numRead = serialPort.readBytes(newData, newData.length);
                logger.info("Read " + numRead + " bytes.");
                logger.info(hexString(newData));
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

    private static String printableEventType(SerialPortEvent event) {
        switch (event.getEventType()) {
            case SerialPort.LISTENING_EVENT_DATA_AVAILABLE:
                return "DATA_AVAILABLE";
            case SerialPort.LISTENING_EVENT_DATA_RECEIVED:
                return "DATA_RECEIVED";
            case SerialPort.LISTENING_EVENT_DATA_WRITTEN:
                return "DATA_WRITTEN";
            default:
                return "UNKNOWN";
        }
    }

    private static final String hexString(final byte[] buffer) {
        StringBuilder sb = new StringBuilder();
        for (byte b : buffer) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }
}
