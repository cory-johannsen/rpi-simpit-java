package cjohannsen.protocol;

public class Packet {

    public static final int MESSAGE_HEADER_SIZE = 4;

    public static final byte PACKET_HEADER_BYTE_0 = (byte) 0xAA;
    public static final byte PACKET_HEADER_BYTE_1 = (byte) 0x50;

    public static final int MESSAGE_TYPE_INDEX = 3;

    private Messages.Type type;
    private byte[] payload;

    public Messages.Type getType() {
        return type;
    }

    public byte[] getPayload() {
        return payload;
    }

    private Packet(final Messages.Type type, final byte[] payload) {
        this.type = type;
        this.payload = payload;
    };

    public static final byte[] encodePacket(Messages.Type messageType, final byte payload) {
        byte[] bytes = {payload};
        return encodePacket(messageType, bytes);
    }

    public static final byte[] encodePacket(Messages.Type messageType, final byte[] payload) {
        //   0xAA
        //   0x50
        //   MESSAGE_SIZE
        //   MESSAGE_TYPE
        //   PAYLOAD
        final byte[] message = new byte[payload.length + MESSAGE_HEADER_SIZE];
        int i = 0;
        message[i++] = PACKET_HEADER_BYTE_0;
        message[i++] = PACKET_HEADER_BYTE_1;
        message[i++] = (byte) (payload.length);
        message[i++] = (byte) messageType.ordinal();
        for(byte b : payload) {
            message[i++] = b;
        }
        return message;
    }

    public static final Packet decodePacket(final byte[] payload) {
        //   0xAA
        //   0x50
        //   MESSAGE_SIZE
        //   MESSAGE_TYPE
        //   PAYLOAD
        final byte messageType = payload[MESSAGE_TYPE_INDEX];
        final byte[] message = new byte[payload.length - MESSAGE_HEADER_SIZE];
        for(int i = MESSAGE_HEADER_SIZE; i < payload.length; i++) {
            message[i - MESSAGE_HEADER_SIZE] = payload[i];
        }
        return new Packet(Messages.Type.from(messageType), message);
    }
}
