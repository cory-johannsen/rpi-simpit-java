package cjohannsen.protocol;

public class Packet {

    public static final int PACKET_SIZE = 64;

    public static final int MESSAGE_HEADER_SIZE = 4;

    public static final byte PACKET_HEADER_BYTE_0 = (byte) 0xAA;
    public static final byte PACKET_HEADER_BYTE_1 = (byte) 0x50;

    public static final int MESSAGE_SIZE_INDEX = 2;
    public static final int MESSAGE_TYPE_INDEX = 3;

    private MessageType.Datagram datagram;
    private byte[] payload;

    public MessageType.Datagram getDatagram() {
        return datagram;
    }

    public byte[] getPayload() {
        return payload;
    }

    private Packet(final MessageType.Datagram datagram, final byte[] payload) {
        this.datagram = datagram;
        this.payload = payload;
    };

    public static final byte[] encodePacket(MessageType.Command command, final byte payload) {
        byte[] bytes = {payload};
        return encodePacket(command, bytes);
    }

    public static final byte[] encodePacket(MessageType.Command command, final byte[] payload) {
        //   0xAA
        //   0x50
        //   MESSAGE_SIZE
        //   MESSAGE_TYPE
        //   PAYLOAD
        //   0x00
        final byte[] message = new byte[PACKET_SIZE];
        int i = 0;
        message[i++] = PACKET_HEADER_BYTE_0;
        message[i++] = PACKET_HEADER_BYTE_1;
        message[i++] = (byte) (payload.length);
        message[i++] = (byte) command.ordinal();
        for(byte b : payload) {
            message[i++] = b;
        }
        return message;
    }

    public static final Packet decodePacket(final byte[] payload) throws InvalidPacketException {
        //   0xAA
        //   0x50
        //   MESSAGE_SIZE
        //   MESSAGE_TYPE
        //   PAYLOAD

        // Sanity check: is the payload long enough for a message?  It must have at least 5 bytes to be a valid message.
        if (payload.length != PACKET_SIZE) {
            throw new InvalidPacketException("Invalid length - packet is " + payload.length + ", not " + PACKET_SIZE + " in size.");
        }

        // First, validate the header
        if (payload[0] != PACKET_HEADER_BYTE_0 || payload[1] != PACKET_HEADER_BYTE_1) {
            throw new InvalidPacketException("Missing header bytes.");
        }

        final int payloadSize = payload[MESSAGE_SIZE_INDEX];
        if (payloadSize > PACKET_SIZE - MESSAGE_HEADER_SIZE) {
            throw new InvalidPacketException("Invalid length - Payload size is " + payloadSize + ", which exceeds the maximum of " + (PACKET_SIZE - MESSAGE_HEADER_SIZE));
        }

        final byte messageType = payload[MESSAGE_TYPE_INDEX];

        final byte[] message = new byte[payload.length - MESSAGE_HEADER_SIZE];
        for(int i = MESSAGE_HEADER_SIZE; i < payload.length; i++) {
            message[i - MESSAGE_HEADER_SIZE] = payload[i];
        }
        return new Packet(MessageType.Datagram.from(messageType), message);
    }
}
