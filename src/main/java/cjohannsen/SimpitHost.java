package cjohannsen;

import cjohannsen.protocol.*;
import cjohannsen.protocol.Handler;
import cjohannsen.protocol.InvalidPacketException;
import cjohannsen.protocol.MessageType;
import cjohannsen.protocol.Packet;
import com.fazecast.jSerialComm.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

@Component
public class SimpitHost {
    static final Logger logger = LoggerFactory.getLogger(SimpitHost.class);

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

    public static final long POLL_INTERVAL_MILLIS = 75;


    private final SerialPort serialPort;
    private final Map<MessageType.Datagram, Handler> handlers;
    private final PacketSource packetSource;

    @Autowired
    public SimpitHost(final SerialPort serialPort, final PacketSource packetSource) {
        this.serialPort = serialPort;
        this.handlers = new ConcurrentHashMap<>();
        this.packetSource = packetSource;
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
        long startTime = -1;
        boolean handshakeComplete = false;
        while (!handshakeComplete) {
            if (startTime == -1 || System.currentTimeMillis() - startTime > Duration.ofSeconds(10).toMillis()) {
                logger.info("SimpitHost initiating handshake...");
                byte[] message = Packet.encodePacket(MessageType.Command.SYNC_MESSAGE, SYN);
                logger.trace(Util.hexString(message));
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

            Packet ackMessage = packetSource.next();
            while(ackMessage.getDatagram() != MessageType.Datagram.SYNC_MESSAGE) {
                ackMessage = packetSource.next();
            }

            byte ackByte = ackMessage.getPayload()[0];
            logger.debug("ack byte: " + ackByte);
            if (ackByte == HANDSHAKE_ACK) {
                logger.info("ACK received, sending SYNACK...");
                byte[] synack = Packet.encodePacket(MessageType.Command.SYNC_MESSAGE, SYNACK);
                logger.trace(Util.hexString(synack));
                serialPort.writeBytes(synack, 1);
            }
            setupDataPoller();
            return true;
        }

        return false;
    }

    public boolean sendEchoRequest(String echoMessage) {
        byte[] buffer = Packet.encodePacket(MessageType.Command.ECHO_REQ_MESSAGE, echoMessage.getBytes());
        logger.debug("Sending echo request: " + Util.hexString(buffer));
        int bytesWritten = serialPort.writeBytes(buffer, buffer.length);
        logger.trace("Wrote " + bytesWritten + " bytes successfully.");
        return bytesWritten == buffer.length;
    }

    public boolean enableChannel(MessageType.Datagram type) {
        byte[] buffer = Packet.encodePacket(MessageType.Command.REGISTER_MESSAGE, (byte) type.getValue());
        logger.debug("Sending channel register request: " + Util.hexString(buffer));
        int bytesWritten = serialPort.writeBytes(buffer, buffer.length);
        logger.trace("Wrote " + bytesWritten + " bytes successfully.");
        return bytesWritten == buffer.length;
    }

    public boolean disableChannel(MessageType.Datagram type) {
        byte[] buffer = Packet.encodePacket(MessageType.Command.DEREGISTER_MESSAGE, (byte) type.getValue());
        logger.debug("Sending channel register request: " + Util.hexString(buffer));
        int bytesWritten = serialPort.writeBytes(buffer, buffer.length);
        logger.trace("Wrote " + bytesWritten + " bytes successfully.");
        return bytesWritten == buffer.length;
    }


    public void registerHandler(MessageType.Datagram type, Handler handler) {
        logger.info("Registering handler for " + type);
        handlers.put(type, handler);
    }


    private void setupDataPoller() {
        logger.info("Initializing KerbalSimpit. Starting serial port data poller.");
        Executors.newSingleThreadExecutor().execute(() -> {
            while(true) {
                Packet packet = packetSource.next();
                logger.debug("Incoming packet: " + packet.getDatagram());
                Handler handler = handlers.get(packet.getDatagram());
                if (handler == null) {
                    for(MessageType.Datagram t : handlers.keySet()) {
                        if (t.equals(packet.getDatagram())) {
                            handler = handlers.get(t);
                        }
                    }
                }
                if (handler != null) {
                    logger.debug("Found a handler");
                    handler.handle(packet.getDatagram(), packet.getPayload());
                }
                else {
                    logger.debug("No handler found for type " + packet.getDatagram());
                }
            }
        });

    }



}
