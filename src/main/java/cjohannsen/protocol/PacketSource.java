package cjohannsen.protocol;

import cjohannsen.SimpitHost;
import cjohannsen.Util;
import com.fazecast.jSerialComm.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

import static cjohannsen.protocol.PacketSource.ReceiveState.*;

@Component
public class PacketSource {
    private static final Logger logger = LoggerFactory.getLogger(PacketSource.class);

    private static final int MAX_QUEUE_DEPTH = 10;
    public static final long POLL_INTERVAL_MILLIS = 25;

    private final SerialPort serialPort;
    private final BlockingQueue<Packet> packets;

    enum ReceiveState {
        WAITING_FOR_HEADER_BYTE_0,
        WAITING_FOR_HEADER_BYTE_1,
        WAITING_FOR_SIZE_BYTE,
        WAITING_FOR_TYPE_BYTE,
        WAITING_FOR_DATA
    }


    @Autowired
    public PacketSource(SerialPort serialPort) {
        this.serialPort = serialPort;
        packets = new LinkedBlockingQueue<>();
    }

    public Packet next() {
        Packet packet = null;
        while(packet == null) {
            try {
                packet = packets.take();
            } catch (InterruptedException e) {
            }
        }
        return packet;
    }

    public void start() {
        logger.info("Packet source starting.");
        Executors.newSingleThreadExecutor().execute(() -> {
            while (true) {
                try {
                    final Packet packet = waitForPacket();
                    logger.debug("New packet: " + packet.getDatagram());
                    packets.add(packet);
                    while (packets.size() > MAX_QUEUE_DEPTH) {
                        packets.remove();
                    }
                } catch (InvalidPacketException e) {
                    logger.trace("Invalid packet:" + e.getMessage());
                }
            }
        });
    }

    private Packet waitForPacket() throws InvalidPacketException {
        final byte[] buffer = new byte[Packet.PACKET_SIZE];
        int bufferIndex = 0;
        ReceiveState receiveState = WAITING_FOR_HEADER_BYTE_0;
        boolean complete = false;
        int payloadSize = 0;
        int payloadBytesRead = 0;
        while(!complete) {
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
            int bytesRead = serialPort.readBytes(incomingBytes, bytesAvailable);

            logger.debug("Incoming data: " + Util.hexString(incomingBytes));

            for (int i = 0; i < bytesRead; i++) {
                switch (receiveState) {
                    case WAITING_FOR_HEADER_BYTE_0:
                        if (incomingBytes[i] != Packet.PACKET_HEADER_BYTE_0) {
                            continue;
                        } else {
                            buffer[bufferIndex++] = incomingBytes[i];
                            receiveState = WAITING_FOR_HEADER_BYTE_1;
                        }
                        break;
                    case WAITING_FOR_HEADER_BYTE_1:
                        if (incomingBytes[i] != Packet.PACKET_HEADER_BYTE_1) {
                            receiveState = WAITING_FOR_HEADER_BYTE_0;
                            bufferIndex = 0;
                            continue;
                        } else {
                            buffer[bufferIndex++] = incomingBytes[i];
                            receiveState = WAITING_FOR_SIZE_BYTE;
                        }
                        break;
                    case WAITING_FOR_SIZE_BYTE:
                        payloadSize = incomingBytes[i];
                        buffer[bufferIndex++] = incomingBytes[i];
                        receiveState = WAITING_FOR_TYPE_BYTE;
                        break;
                    case WAITING_FOR_TYPE_BYTE:
                        buffer[bufferIndex++] = incomingBytes[i];
                        receiveState = WAITING_FOR_DATA;
                        break;
                    case WAITING_FOR_DATA:
                        if (payloadBytesRead >= payloadSize) {
                            complete = true;
                            break;
                        }
                        buffer[bufferIndex++] = incomingBytes[i];
                        payloadBytesRead++;
                        break;
                }

                if (complete || bufferIndex >= Packet.PACKET_SIZE) {
                    break;
                }
            }
        }
        logger.debug("packet complete, read payload bytes " + payloadBytesRead + ": " + Util.hexString(buffer));

        return Packet.decodePacket(buffer);
    }
}
