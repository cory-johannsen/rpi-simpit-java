package cjohannsen.protocol;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.MessageFormat;

/**
 * KerbalSimpit datagram payload types.  Each of these classes directly maps to an encoded message type published
 * by the KebalSimpit plugin.
 */
public abstract class Payload {

    /**
     * A functional interface for a Payload provider - an object that can provide a Payload given a raw array of bytes.
     */
    public interface Provider {
        Payload provide(byte[] bytes);
    }

    public static final double MAX_DELTA = 0.000001;

    private final byte[] bytes;

    @Override
    public boolean equals(final Object o) {
        return this.equals((Payload) o);
    }

    abstract boolean equals(final Payload p);

    public abstract String toString();

    /**
     * Return the low-level byte array that backs this payload.
     * @return the low-level byte array that backs this payload.
     */
    public byte[] getBytes() {
        return bytes;
    }

    private Payload(final byte[] bytes) {
        this.bytes = bytes;
    }

    private static final boolean equalWithDelta(float a, float b) {
        return equalWithDelta(a, b, MAX_DELTA)
;    }

    private static final boolean equalWithDelta(float a, float b, double delta) {
        return Math.abs(a - b) <= MAX_DELTA;
    }

    /** An Altitude message. */
    public static class AltitudeMessage extends Payload {
        public final float sealevel; /**< Altitude above sea level. 4 bytes. */
        public final float surface;  /**< Surface altitude at current position. 4 bytes. */

        @Override
        boolean equals(final Payload p) {
            if (p instanceof AltitudeMessage) {
                boolean sealevelEqual = equalWithDelta(sealevel, ((AltitudeMessage)p).sealevel);
                boolean surfaceEqual = equalWithDelta(surface, ((AltitudeMessage)p).surface);
                return sealevelEqual && surfaceEqual;
            }
            return false;
        }

        @Override
        public String toString() {
            return MessageFormat.format("Sea Level: {0}, Surface: {1}", sealevel, surface);
        }

        private AltitudeMessage(float seaLevel, float surface, byte[] bytes) {
            super(bytes);
            this.sealevel = seaLevel;
            this.surface = surface;
        }

        public static AltitudeMessage from(byte[] bytes) {
            if (bytes.length < 8) {
                throw new IllegalArgumentException("Not enough bytes.  8 required. " + bytes.length + " sent.");
            }
            float seaLevel = ByteBuffer.wrap(bytes, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            float surfaceAlt = ByteBuffer.wrap(bytes, 4, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            return new AltitudeMessage(seaLevel, surfaceAlt, bytes);
        }
    }

    /** An Apsides message. */
    public static class ApsidesMessage extends Payload {
        public final float periapsis; /**< Current vessel's orbital periapsis. */
        public final float apoapsis; /**< Current vessel's orbital apoapsis. */


        @Override
        boolean equals(Payload p) {
            if (p instanceof ApsidesMessage) {
                return periapsis == ((ApsidesMessage)p).periapsis && apoapsis == ((ApsidesMessage)p).apoapsis;
            }
            return false;
        }

        @Override
        public String toString() {
            return MessageFormat.format("Periapsis: {0}, apoapsis: {1}", periapsis, apoapsis);
        }

        private ApsidesMessage(float periapsis, float apoapsis, byte[] bytes) {
            super(bytes);
            this.periapsis = periapsis;
            this.apoapsis = apoapsis;
        }

        public static ApsidesMessage from(byte[] bytes) {
            float periapsis = ByteBuffer.wrap(bytes, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            float apoapsis = ByteBuffer.wrap(bytes, 4, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            return new ApsidesMessage(periapsis, apoapsis, bytes);
        }
    }

    /** An Apsides Time message. */
    public static class ApsidesTimeMessage extends Payload {
        public final int periapsis; /** (32-bits) Time until the current vessel's orbital periapsis, in seconds. */
        public final int apoapsis; /** (32-bits) Time until the current vessel's orbital apoapsis, in seconds. */

        private ApsidesTimeMessage(final int periapsis, final int apoapsis, final byte[] bytes) {
            super(bytes);
            this.periapsis = periapsis;
            this.apoapsis = apoapsis;
        }

        public static ApsidesTimeMessage from(byte[] bytes) {
            int periapsis = ByteBuffer.wrap(bytes, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
            int apoapsis = ByteBuffer.wrap(bytes, 4, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
            return new ApsidesTimeMessage(periapsis, apoapsis, bytes);
        }

        @Override
        boolean equals(Payload p) {
            if (p instanceof ApsidesTimeMessage) {
                return periapsis == ((ApsidesTimeMessage)p).periapsis && apoapsis == ((ApsidesTimeMessage)p).apoapsis;
            }
            return false;
        }

        @Override
        public String toString() {
            return MessageFormat.format("Periapsis: {0}, apoapsis: {1}", periapsis, apoapsis);
        }
    }

    /** A Resource message.
     All resource messages use this public static class for sending data. */
    public static class ResourceMessage extends Payload {
        public final float total; /**< Maximum capacity of the resource. */
        public final float available; /**< Current resource level. */

        private ResourceMessage(float total, float available, final byte[] bytes) {
            super(bytes);
            this.total = total;
            this.available = available;
        }

        public static ResourceMessage from(byte[] bytes) {
            float total = ByteBuffer.wrap(bytes, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            float available = ByteBuffer.wrap(bytes, 4, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            return new ResourceMessage(total, available, bytes);
        }

        @Override
        boolean equals(Payload p) {
            if (p instanceof ResourceMessage) {
                return total == ((ResourceMessage)p).total && available == ((ResourceMessage)p).available;
            }
            return false;
        }

        @Override
        public String toString() {
            return MessageFormat.format("{0} / {1}", available, total);
        }
    }

    /** A Velocity message. */
    public static class VelocityMessage extends Payload {
        public final float orbital; /**< Orbital velocity. */
        public final float surface; /**< Surface velocity. */
        public final float vertical; /**< Vertical velocity. */

        private VelocityMessage(float orbital, float surface, float vertical, final byte[] bytes) {
            super(bytes);
            this.orbital = orbital;
            this.surface = surface;
            this.vertical = vertical;
        }

        public static VelocityMessage from(byte[] bytes) {
            float orbital = ByteBuffer.wrap(bytes, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            float surface = ByteBuffer.wrap(bytes, 4, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            float vertical = ByteBuffer.wrap(bytes, 8, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            return new VelocityMessage(orbital, surface, vertical, bytes);
        }

        @Override
        boolean equals(Payload p) {
            if (p instanceof VelocityMessage) {
                return orbital == ((VelocityMessage)p).orbital && surface == ((VelocityMessage)p).surface && vertical == ((VelocityMessage)p).vertical;
            }
            return false;
        }

        @Override
        public String toString() {
            return "orbital: " + orbital + " surface: " + surface + " vertical: " + vertical;
        }
    }

    /** A Target information message. */
    public static class TargetMessage extends Payload {
        public final float distance; /**< Distance to target. */
        public final float velocity; /**< Velocity relative to target. */

        private TargetMessage(float distance, float velocity, final byte[] bytes) {
            super(bytes);
            this.distance = distance;
            this.velocity = velocity;
        }

        @Override
        public String toString() {
            return MessageFormat.format("distance: {0} velocity: {1}", distance, velocity);
        }

        @Override
        boolean equals(Payload p) {
            if (p instanceof TargetMessage) {
                return distance == ((TargetMessage)p).distance && velocity == ((TargetMessage)p).velocity;
            }
            return false;
        }

        public static TargetMessage from(byte[] bytes) {
            float distance = ByteBuffer.wrap(bytes, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            float velocity = ByteBuffer.wrap(bytes, 4, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            return new TargetMessage(distance, velocity, bytes);
        }
    }

    /** An Airspeed information message. */
    public static class AirspeedMessage extends Payload {
        public final float indicatedAirSpeed; /**< Indicated airspeed. */
        public final float mach; /**< Mach number. */

        private AirspeedMessage(float indicatedAirSpeed, float mach, final byte[] bytes) {
            super(bytes);
            this.indicatedAirSpeed = indicatedAirSpeed;
            this.mach = mach;
        }

        @Override
        public String toString() {
            return MessageFormat.format("Indicated: {0} MACH: {1}", indicatedAirSpeed, mach);
        }

        @Override
        boolean equals(Payload p) {
            if (p instanceof AirspeedMessage) {
                return indicatedAirSpeed == ((AirspeedMessage)p).indicatedAirSpeed && indicatedAirSpeed == ((AirspeedMessage)p).mach;
            }
            return false;
        }

        public static AirspeedMessage from(byte[] bytes) {
            float ias = ByteBuffer.wrap(bytes, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            float mach = ByteBuffer.wrap(bytes, 4, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            return new AirspeedMessage(ias, mach, bytes);
        }
    }

    public static class ActionGroupMessage extends Payload {
        public final byte actionGroupStatus;

        private ActionGroupMessage(byte actionGroupStatus, byte[] bytes) {
            super(bytes);
            this.actionGroupStatus = actionGroupStatus;
        }

        @Override
        public String toString() {
            boolean stage = (actionGroupStatus & MessageType.ActionGroupIndex.STAGE_ACTION.getValue()) > 0;
            boolean gear = (actionGroupStatus & MessageType.ActionGroupIndex.GEAR_ACTION.getValue()) > 0;
            boolean lights = (actionGroupStatus & MessageType.ActionGroupIndex.LIGHT_ACTION.getValue()) > 0;
            boolean rcs = (actionGroupStatus & MessageType.ActionGroupIndex.RCS_ACTION.getValue()) > 0;
            boolean sas = (actionGroupStatus & MessageType.ActionGroupIndex.SAS_ACTION.getValue()) > 0;
            boolean brakes = (actionGroupStatus & MessageType.ActionGroupIndex.BRAKES_ACTION.getValue()) > 0;
            boolean abort = (actionGroupStatus & MessageType.ActionGroupIndex.ABORT_ACTION.getValue()) > 0;
            return MessageFormat.format("stage: {0} gear: {1} lights: {2} rcs: {3} sas: {4} brakes: {5} abort: {6}",
                    stage ? "ON" : "OFF",
                    gear ? "ON" : "OFF",
                    lights ? "ON" : "OFF",
                    rcs ? "ON" : "OFF",
                    sas ? "ON" : "OFF",
                    brakes ? "ON" : "OFF",
                    abort ? "ON" : "OFF");
        }

        @Override
        boolean equals(Payload p) {
            return (p instanceof ActionGroupMessage) && (actionGroupStatus == ((ActionGroupMessage) p).actionGroupStatus);
        }

        public static ActionGroupMessage from(byte[] bytes) {
            return new ActionGroupMessage(bytes[0], bytes);
        }
    }

    public static class SphereOfInfluenceMessage extends Payload {
        private final String sphereOfInfluenceMessage;

        private SphereOfInfluenceMessage(String sphereOfInfluenceMessage, byte[] bytes) {
            super(bytes);
            this.sphereOfInfluenceMessage = sphereOfInfluenceMessage;
        }

        @Override
        public String toString() {
            return sphereOfInfluenceMessage;
        }

        @Override
        boolean equals(Payload p) {
            return (p instanceof SphereOfInfluenceMessage) && (sphereOfInfluenceMessage.equals(((SphereOfInfluenceMessage) p).sphereOfInfluenceMessage));
        }

        public static SphereOfInfluenceMessage from(byte[] bytes) {
            return new SphereOfInfluenceMessage(new String(bytes).trim(), bytes);
        }
    }

    /** A vessel rotation message.
     This class contains information about vessel rotation commands. */
    public static class RotationMessage {
        public final int pitch; /**< (16-bits) Vessel pitch. */
        public final int roll; /**< (16-bits) Vessel roll. */
        public final int yaw; /**< (16-bits) Vessel yaw. */
        /** The mask indicates which elements are intentionally set. Unset elements
         should be ignored. It should be one or more of:

         - 1: pitch
         - 2: roll
         - 4: yaw
         */
        public final byte mask;

        private RotationMessage(final int pitch, final int roll, int yaw, byte mask) {
            this.pitch = pitch;
            this.roll = roll;
            this.yaw = yaw;
            this.mask = mask;
        }

        public static RotationMessage from(byte[] bytes) {
            int pitch = ByteBuffer.wrap(bytes, 0, 2).order(ByteOrder.LITTLE_ENDIAN).getInt();
            int roll = ByteBuffer.wrap(bytes, 2, 2).order(ByteOrder.LITTLE_ENDIAN).getInt();
            int yaw = ByteBuffer.wrap(bytes, 4, 2).order(ByteOrder.LITTLE_ENDIAN).getInt();
            byte mask = bytes[bytes.length - 1];
            return new RotationMessage(pitch, roll, yaw, mask);
        }
    }

    /** A vessel translation message.
     This class contains information about vessel translation commands. */
    public static class TranslationMessage {
        public final int x; /**< (16-bits) Translation along the X axis. */
        public final int y; /**< (16-bits) Translation along the Y axis. */
        public final int z; /**< (16-bits) Translation along the Z axis. */
        /** The mask indicates which elements are intentionally set. Unset elements
         should be ignored. It should be one or more of:

         - 1: X
         - 2: Y
         - 4: Z
         */
        public final byte mask;

        private TranslationMessage(final int x, final int y, int z, byte mask) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.mask = mask;
        }

        public static TranslationMessage from(byte[] bytes) {
            int x = ByteBuffer.wrap(bytes, 0, 2).order(ByteOrder.LITTLE_ENDIAN).getInt();
            int y = ByteBuffer.wrap(bytes, 2, 2).order(ByteOrder.LITTLE_ENDIAN).getInt();
            int z = ByteBuffer.wrap(bytes, 4, 2).order(ByteOrder.LITTLE_ENDIAN).getInt();
            byte mask = bytes[bytes.length - 1];
            return new TranslationMessage(x, y, z, mask);
        }
    }

    /** A wheel control message.
     This class contains information about wheel steering and throttle. */
    public static class WheelMessage {
        public final int steer; /**< (16-bits) Wheel steer. */
        public final int throttle; /**< (16-bits)  Wheel throttle. */
        /** The mask indicates which elements are intentionally set. Unset elements
         should be ignored. It should be one or more of:

         - 1: steer
         - 2: throttle
         */
        public final  byte mask;

        private WheelMessage(final int steer, final int throttle, byte mask) {
            this.steer = steer;
            this.throttle = throttle;
            this.mask = mask;
        }

        public static WheelMessage from(byte[] bytes) {
            int steer = ByteBuffer.wrap(bytes, 0, 2).order(ByteOrder.LITTLE_ENDIAN).getInt();
            int throttle = ByteBuffer.wrap(bytes, 2, 2).order(ByteOrder.LITTLE_ENDIAN).getInt();
            byte mask = bytes[bytes.length - 1];
            return new WheelMessage(steer, throttle, mask);
        }
    }

}
