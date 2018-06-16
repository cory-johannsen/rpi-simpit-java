package cjohannsen;

import cjohannsen.protocol.Handler;
import cjohannsen.protocol.InvalidPacketException;
import cjohannsen.protocol.MessageType;
import cjohannsen.protocol.Packet;
import com.fazecast.jSerialComm.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import static cjohannsen.SimpitHost.ReceiveState.*;

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

    @Autowired
    public SimpitHost(final SerialPort serialPort) {
        this.serialPort = serialPort;
        this.handlers = new ConcurrentHashMap<>();
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
        byte[] incomingBytes = new byte[Packet.PACKET_SIZE];
        while (bytesRead < HANDSHAKE_ACK_MIN_LENGTH) {
            long startTime = -1;
            while (serialPort.bytesAvailable() == 0) {
                if (startTime == -1 || System.currentTimeMillis() - startTime > 10000) {
                    logger.info("SimpitHost initiating handshake...");
                    byte[] message = Packet.encodePacket(MessageType.Command.SYNC_MESSAGE, SYN);
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
            logger.info("Decoded message of type " + ackMessage.getDatagram());
            logger.info(Util.hexString(ackMessage.getPayload()));
            byte ackByte = ackMessage.getPayload()[0];
            logger.debug("ack byte: " + ackByte);
            if (ackByte == HANDSHAKE_ACK) {
                logger.info("ACK received, sending SYNACK...");
                byte[] synack = Packet.encodePacket(MessageType.Command.SYNC_MESSAGE, SYNACK);
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
        byte[] buffer = Packet.encodePacket(MessageType.Command.ECHO_REQ_MESSAGE, echoMessage.getBytes());
        logger.info("Sending echo request: " + Util.hexString(buffer));
        int bytesWritten = serialPort.writeBytes(buffer, buffer.length);
        logger.debug("Wrote " + bytesWritten + " bytes successfully.");
        return bytesWritten == buffer.length;
    }


    public void registerHandler(MessageType.Datagram type, Handler handler) {
        logger.info("Registering handler for " + type);
        handlers.put(type, handler);
    }

    enum ReceiveState {
        WAITING_FOR_HEADER_BYTE_0,
        WAITING_FOR_HEADER_BYTE_1,
        WAITING_FOR_SIZE_BYTE,
        WAITING_FOR_TYPE_BYTE,
        WAITING_FOR_DATA
    }

    private void setupDataPoller() {
        logger.info("Initializing KerbalSimpit. Starting serial port data poller.");
        Executors.newSingleThreadExecutor().execute(() -> {
            while(true) {
                byte[] buffer = new byte[Packet.PACKET_SIZE];
                ReceiveState receiveState = ReceiveState.WAITING_FOR_HEADER_BYTE_0;
                while (serialPort.bytesAvailable() == 0) {
                    try {
                        Thread.sleep(POLL_INTERVAL_MILLIS);
                    } catch (InterruptedException e) {
                        // NO-OP
                    }
                }
                final int bytesAvailable = serialPort.bytesAvailable();
                logger.info(bytesAvailable + " bytes available.");

                byte[] incomingBytes = new byte[bytesAvailable];
                int bytesRead = serialPort.readBytes(incomingBytes, bytesAvailable);

                logger.info("Incoming data: " + Util.hexString(incomingBytes));

                int payloadSize = 0;
                int index = 0;
                for (int i = 0; i < bytesRead; i++) {
                    switch (receiveState) {
                        case WAITING_FOR_HEADER_BYTE_0:
                            if (incomingBytes[i] != Packet.PACKET_HEADER_BYTE_0) {
                                buffer[index++] = incomingBytes[i];
                                continue;
                            }
                            else {
                                receiveState = WAITING_FOR_HEADER_BYTE_1;
                            }
                            break;
                        case WAITING_FOR_HEADER_BYTE_1:
                            if (incomingBytes[i] != Packet.PACKET_HEADER_BYTE_1) {
                                buffer[index++] = incomingBytes[i];
                                continue;
                            }
                            else {
                                receiveState = WAITING_FOR_HEADER_BYTE_0;
                            }
                            break;
                        case WAITING_FOR_SIZE_BYTE:
                            payloadSize = incomingBytes[i];
                            buffer[index++] = incomingBytes[i];
                            receiveState = WAITING_FOR_TYPE_BYTE;
                            break;
                        case WAITING_FOR_TYPE_BYTE:
                            buffer[index++] = incomingBytes[i];
                            receiveState = WAITING_FOR_TYPE_BYTE;
                            break;
                        case WAITING_FOR_DATA:
                            if (i >= payloadSize + 4) {
                                continue;
                            }
                            break;
                    }

                    buffer[index++] = incomingBytes[i];
                    if (index >= Packet.PACKET_SIZE) {
                        break;
                    }
                }

                try {
                    Packet packet = Packet.decodePacket(buffer);
                    logger.info("Incoming packet: " + packet.getDatagram());
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
                        logger.warn("No handler found for type " + packet.getDatagram());
                    }

                } catch (InvalidPacketException e) {
                    logger.error("Invalid packet: " + e);
                }
            }
        });

    }

}
