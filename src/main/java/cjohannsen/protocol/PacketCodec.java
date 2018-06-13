package cjohannsen.protocol;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PacketCodec {

    public static final int MESSAGE_HEADER_SIZE = 4;
    public static final int MESSAGE_BODY_PADDING = 2;

    public static final byte MESSAGE_HEADER_BYTE_0 = (byte) 0xAA;
    public static final byte MESSAGE_HEADER_BYTE_1 = (byte) 0x50;

    public static final byte MESSAGE_INITIATOR_BYTE = (byte) 0x00;
    public static final byte MESSAGE_TERMINATOR_BYTE = (byte) 0x00;

    @Autowired
    public PacketCodec() {
        
    }

    public byte[] encodePacket(Messages.Common messageType, final byte payload) {
        byte[] bytes = {payload};
        return encodePacket(messageType, bytes);
    }

    public byte[] encodePacket(Messages.Common messageType, final byte[] payload) {
        //   0xAA
        //   0x50
        //   MESSAGE_SIZE
        //   MESSAGE_TYPE
        //   PAYLOAD
        final byte[] message = new byte[payload.length + MESSAGE_HEADER_SIZE + MESSAGE_BODY_PADDING];
        int i = 0;
        message[i++] = MESSAGE_HEADER_BYTE_0;
        message[i++] = MESSAGE_HEADER_BYTE_1;
        message[i++] = (byte) (payload.length + MESSAGE_BODY_PADDING);
        message[i++] = (byte) messageType.getValue();
        message[i++] = MESSAGE_INITIATOR_BYTE;
        for(byte b : payload) {
            message[i++] = b;
        }
        message[i] = MESSAGE_TERMINATOR_BYTE;
        return message;
    }

    public byte[] decodePacket(final byte[] payload) {
        //   0xAA
        //   0x50
        //   MESSAGE_SIZE
        //   MESSAGE_TYPE
        //   PAYLOAD
        final byte[] message = new byte[payload.length - (MESSAGE_HEADER_SIZE + MESSAGE_BODY_PADDING)];
        for(int i = MESSAGE_HEADER_SIZE + 1; i < payload.length - MESSAGE_BODY_PADDING; i++) {
            message[i - MESSAGE_HEADER_SIZE + 1] = payload[i];
        }
        return message;
    }
    

    // Message parsing functions

    /** Parse a message containing Altitude data.
     @param msg The byte array of the message body.
     @returns altitudeMessage A formatted altitudeMessage public class.
     */
    public Optional<PayloadTypes.AltitudeMessage> parseAltitude(byte msg[]) {
        return Optional.empty();
    }

    /** Parse a message containing Apsides data.
     @returns apsidesMessage A formatted apsidesMessage public class.
     */
    public Optional<PayloadTypes.ApsidesMessage> parseApsides(byte msg[]) {
        return Optional.empty();
    }

    /** Parse a message containing Apsides Time data.
     @returns apsidesTimeMessage A formatted apsidesTimeMessage public class.
     */
    public Optional<PayloadTypes.ApsidesTimeMessage> parseApsidesTime(byte msg[]) {
        return Optional.empty();
    }

    /** Parse a message countaining Resource data.
     @returns resourceMessage A formatted resourceMessage public class.
     */
    public Optional<PayloadTypes.ResourceMessage> parseResource(byte msg[]) {
        return Optional.empty();
    }

    /** Parse a message containing Velocity data.
     @returns velocityMessage A formatted velocityMessage public class.
     */
    public Optional<PayloadTypes.VelocityMessage> parseVelocity(byte msg[]) {
        return Optional.empty();
    }

    /** Parse a message containing Target data.
     @returns targetMessage A formatted targetMessage public class.
     */
    public Optional<PayloadTypes.TargetMessage> parseTarget(byte msg[]) {
        return Optional.empty();
    }

    /** Parse a message containing Airspeed data.
     @returns airspeedMessage a formatted airspeedMessage public class.
     */
    public Optional<PayloadTypes.AirspeedMessage> parseAirspeed(byte msg[]) {
        return Optional.empty();
    }
}
