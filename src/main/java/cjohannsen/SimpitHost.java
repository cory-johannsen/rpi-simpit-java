package cjohannsen;

import cjohannsen.protocol.*;
import cjohannsen.protocol.Handler;
import cjohannsen.protocol.MessageType;
import cjohannsen.protocol.Packet;
import com.fazecast.jSerialComm.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

@Component
public class SimpitHost {
    static final Logger logger = LoggerFactory.getLogger(SimpitHost.class);

    static class HandlerAndProvider {
        private Handler handler;
        private Payload.Provider provider;

        public HandlerAndProvider(Handler handler, Payload.Provider provider) {
            this.handler = handler;
            this.provider = provider;
        }

        public Handler getHandler() {
            return handler;
        }

        public Payload.Provider getProvider() {
            return provider;
        }
    }

    public static final int HANDSHAKE_RETRY_FREQUENCY_MILLIS = 5000;

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
    public static final int PACKET_TIMEOUT_MILLIS = 10000;


    private final SerialPort serialPort;
    private final Map<MessageType.Datagram, HandlerAndProvider> handlers;
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
        boolean handshakeComplete = false;
        long startTimeMillis = System.currentTimeMillis();
        while (!handshakeComplete) {
            logger.info("SimpitHost initiating handshake...");
            byte[] message = Packet.encodePacket(MessageType.Command.SYNC_MESSAGE, SYN);
            logger.trace(Util.hexString(message));
            serialPort.writeBytes(message, message.length);
            logger.info("Waiting for ACK...");

            while (System.currentTimeMillis() - startTimeMillis < HANDSHAKE_RETRY_FREQUENCY_MILLIS ) {
                Optional<Packet> ackMessage = packetSource.next(Optional.of(new Integer(PACKET_TIMEOUT_MILLIS)));
                if (ackMessage.isPresent() && ackMessage.get().getDatagram() == MessageType.Datagram.SYNC_MESSAGE) {
                    byte ackByte = ackMessage.get().getPayload()[0];
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
            }
            startTimeMillis = System.currentTimeMillis();
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


    public void registerHandler(MessageType.Datagram type, Payload.Provider provider, Handler handler) {
        logger.info("Registering handler for " + type);
        handlers.put(type, new HandlerAndProvider(handler, provider));
    }

    public void activateStandardActionGroup(MessageType.ActionGroupIndex index) {
        logger.info("Activating standard action group " + index);
        byte[] buffer = Packet.encodePacket(MessageType.Command.AGACTIVATE_MESSAGE, (byte) index.getValue());
        logger.debug("Sending activate standard action group request: " + Util.hexString(buffer));
        int bytesWritten = serialPort.writeBytes(buffer, buffer.length);
        logger.trace("Wrote " + bytesWritten + " bytes successfully.");
    }

    public void deactivateStandardActionGroup(MessageType.ActionGroupIndex index) {
        logger.info("Deactivating standard action group " + index);
        byte[] buffer = Packet.encodePacket(MessageType.Command.AGDEACTIVATE_MESSAGE, (byte) index.getValue());
        logger.debug("Sending deactivate standard action group request: " + Util.hexString(buffer));
        int bytesWritten = serialPort.writeBytes(buffer, buffer.length);
        logger.trace("Wrote " + bytesWritten + " bytes successfully.");
    }

    public void toggleStandardActionGroup(MessageType.ActionGroupIndex index) {
        logger.info("Toggling standard action group " + index);
        byte[] buffer = Packet.encodePacket(MessageType.Command.AGTOGGLE_MESSAGE, (byte) index.getValue());
        logger.debug("Sending toggle standard action group request: " + Util.hexString(buffer));
        int bytesWritten = serialPort.writeBytes(buffer, buffer.length);
        logger.trace("Wrote " + bytesWritten + " bytes successfully.");
    }

    public void activateCustomActionGroup(int index) {
        logger.info("Activating custom action group " + index);
        byte[] buffer = Packet.encodePacket(MessageType.Command.CAGACTIVATE_MESSAGE, (byte) index);
        logger.debug("Sending activate custom action group request: " + Util.hexString(buffer));
        int bytesWritten = serialPort.writeBytes(buffer, buffer.length);
        logger.trace("Wrote " + bytesWritten + " bytes successfully.");
    }

    public void deactivateCustomActionGroup(int index) {
        logger.info("Deactivating custom action group " + index);
        byte[] buffer = Packet.encodePacket(MessageType.Command.CAGDEACTIVATE_MESSAGE, (byte) index);
        logger.debug("Sending deactivate custom action group request: " + Util.hexString(buffer));
        int bytesWritten = serialPort.writeBytes(buffer, buffer.length);
        logger.trace("Wrote " + bytesWritten + " bytes successfully.");

    }

    public void toggleCustomActionGroup(int index) {
        logger.info("Toggling custom action group " + index);
        byte[] buffer = Packet.encodePacket(MessageType.Command.CAGTOGGLE_MESSAGE, (byte) index);
        logger.debug("Sending toggle custom action group request: " + Util.hexString(buffer));
        int bytesWritten = serialPort.writeBytes(buffer, buffer.length);
        logger.trace("Wrote " + bytesWritten + " bytes successfully.");
    }



    private void setupDataPoller() {
        logger.info("Initializing KerbalSimpit. Starting serial port data poller.");
        Executors.newSingleThreadExecutor().execute(() -> {
            while(true) {
                Packet packet = packetSource.next();
                logger.debug("Incoming packet: " + packet.getDatagram());
                HandlerAndProvider handlerAndProvider = handlers.get(packet.getDatagram());
                if (handlerAndProvider != null) {
                    logger.debug("Found a handler");
                    handlerAndProvider.getHandler().handle(packet.getDatagram(), packet.getPayload(), handlerAndProvider.getProvider());
                }
                else {
                    logger.debug("No handler found for type " + packet.getDatagram());
                }
            }
        });

    }



}
