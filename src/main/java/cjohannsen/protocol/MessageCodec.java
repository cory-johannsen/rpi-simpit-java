package cjohannsen.protocol;

import java.util.Optional;

public class MessageCodec {

    public MessageCodec() {
        
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
