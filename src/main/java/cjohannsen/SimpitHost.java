package cjohannsen;

import cjohannsen.protocol.InvalidPacketException;
import cjohannsen.protocol.Packet;
import cjohannsen.protocol.Messages;
import com.fazecast.jSerialComm.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;

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
            KERBALSIMPIT_VERSION.getBytes()[1],
            KERBALSIMPIT_VERSION.getBytes()[2],
            KERBALSIMPIT_VERSION.getBytes()[3],
            KERBALSIMPIT_VERSION.getBytes()[4],
            0x00
    };

    private static final byte[] SYNACK= {
            HANDSHAKE_SYNACK,
            KERBALSIMPIT_VERSION.getBytes()[0],
            KERBALSIMPIT_VERSION.getBytes()[1],
            KERBALSIMPIT_VERSION.getBytes()[2],
            KERBALSIMPIT_VERSION.getBytes()[3],
            KERBALSIMPIT_VERSION.getBytes()[4],
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
        //   0x00 SYN
        //   KERBALSIMPIT_VERSION
        //   0x00
        // EXPECT:
        //   0xAA 
        //   0x50 
        //   MESSAGE_SIZE
        //   0x00 (MESSAGE_TYPE for HANDSHAKE)
        //   0x01 ACK
        //   MESSAGE_BYTES
        // REPLY:
        //   0xAA
        //   0x50
        //   MESSAGE_SIZE
        //   0x00 (MESSAGE_TYPE for HANDSHAKE)
        //   0x02 SYNACK
        //   KERBALSIMPIT_VERSION
        //   0x00
        int bytesRead = 0;
        byte[] incomingBytes = new byte[64];
        while (bytesRead < HANDSHAKE_ACK_MIN_LENGTH) {
            long startTime = -1;
            while (serialPort.bytesAvailable() == 0) {
                if (startTime == -1 || System.currentTimeMillis() - startTime > 10000) {
                    logger.info("SimpitHost initiating handshake...");
                    byte[] message = Packet.encodePacket(Messages.Type.SYNC_MESSAGE, SYN);
                    logger.info(Util.hexString(message));
                    serialPort.writeBytes(message, message.length);
                    logger.info("Waiting for ACK...");
                    startTime = System.currentTimeMillis();
                }
                try {
                    Thread.sleep(POLL_INTERVAL_MILLIS);
                } catch (InterruptedException e) {
                    // NO-OP
                    logger.error("Interrupted: " + e);
                }
            }
            byte[] readBuffer = new byte[serialPort.bytesAvailable()];
            int numRead = serialPort.readBytes(readBuffer, readBuffer.length);
            logger.info("Read " + numRead + " bytes.");
            logger.info(Util.hexString(readBuffer));
            for (final byte b : readBuffer) {
                incomingBytes[bytesRead++] = b;
            }
        }
        try {
            Packet ackMessage = Packet.decodePacket(incomingBytes);
            logger.info("Decoded message of type " + ackMessage.getType());
            logger.info(Util.hexString(ackMessage.getPayload()));
            byte ackByte = ackMessage.getPayload()[0];
            logger.debug("ack byte: " + ackByte);
            if (ackByte == HANDSHAKE_ACK) {
                logger.info("ACK received, sending SYNACK...");
                byte[] synack = Packet.encodePacket(Messages.Type.SYNC_MESSAGE, SYNACK);
                logger.info(Util.hexString(synack));
                serialPort.writeBytes(synack, 1);
            }
            setupDataPoller();
            return true;
        } catch (InvalidPacketException e) {
            logger.error("Invalid packet: " + e);
        }
        return false;
    }

    public boolean sendEchoRequest(String echoMessage) {
        byte[] buffer = Packet.encodePacket(Messages.Type.ECHO_REQ_MESSAGE, echoMessage.getBytes());
        logger.info("Sending echo request: " + Util.hexString(buffer));
        int bytesWritten = serialPort.writeBytes(buffer, buffer.length);
        logger.debug("Wrote " + bytesWritten + " bytes successfully.");
        return bytesWritten == buffer.length;
    }

    private void setupDataPoller() {
        logger.info("Initializing KerbalSimpit. Starting serial port data poller - ");
        Executors.newSingleThreadExecutor().execute(() -> {
            while(true) {
                byte[] buffer = new byte[Packet.PACKET_SIZE];
                int index = 0;
                while (index < Packet.PACKET_SIZE) {
                    while (serialPort.bytesAvailable() == 0) {
                        try {
                            Thread.sleep(POLL_INTERVAL_MILLIS);
                        } catch (InterruptedException e) {
                            // NO-OP
                        }
                    }
                    final int bytesAvailable = serialPort.bytesAvailable();
                    logger.trace(bytesAvailable + " bytes available.");

                    byte[] incomingBytes = new byte[bytesAvailable];
                    serialPort.readBytes(incomingBytes, bytesAvailable);

                    for (byte b : incomingBytes) {
                        buffer[index++] = b;
                    }
                }
                logger.info("Incoming data: " + Util.hexString(buffer));

                try {
                    Packet packet = Packet.decodePacket(buffer);
                    logger.info("Incoming packet: " + packet.getType());
                } catch (InvalidPacketException e) {
                    logger.error("Invalid packet: " + e);
                }
            }
        });

    }

}
