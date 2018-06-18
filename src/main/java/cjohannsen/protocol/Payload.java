package cjohannsen.protocol;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.MessageFormat;

public abstract class Payload {
    private final byte[] bytes;

    public abstract boolean equals(final Payload p);

    public byte[] getBytes() {
        return bytes;
    }


    private Payload(final byte[] bytes) {
        this.bytes = bytes;

    }

    /** An Altitude message. */
    public static class AltitudeMessage extends Payload {
        public final float sealevel; /**< Altitude above sea level. */
        public final float surface;  /**< Surface altitude at current position. */

        public AltitudeMessage(float seaLevel, float surface) {
            super(ByteBuffer.allocate(8).putFloat(seaLevel).putFloat(seaLevel).array());
            this.sealevel = seaLevel;
            this.surface = surface;
        }

        public AltitudeMessage(float seaLevel, float surface, byte[] bytes) {
            super(bytes);
            this.sealevel = seaLevel;
            this.surface = surface;
        }

        public static AltitudeMessage from(byte[] bytes) {
            float seaLevel = ByteBuffer.wrap(bytes, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            float surfaceAlt = ByteBuffer.wrap(bytes, 4, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            return new AltitudeMessage(seaLevel, surfaceAlt, bytes);
        }

        public boolean equals(final Payload p) {
            if (p instanceof AltitudeMessage) {
                return sealevel == ((AltitudeMessage)p).sealevel && surface == ((AltitudeMessage)p).surface;
            }
            return false;
        }

        public String toString() {
            return MessageFormat.format("Altitude - Sea Level: {0}, Surface: {1}", sealevel, surface);
        }
    }

    /** An Apsides message. */
    public static class ApsidesMessage extends Payload {
        public final float periapsis; /**< Current vessel's orbital periapsis. */
        public final float apoapsis; /**< Current vessel's orbital apoapsis. */

        public ApsidesMessage(float periapsis, float apoapsis) {
            super(ByteBuffer.allocate(8).putFloat(periapsis).putFloat(apoapsis).array());
            this.periapsis = periapsis;
            this.apoapsis = apoapsis;
        }

        public ApsidesMessage(float periapsis, float apoapsis, byte[] bytes) {
            super(bytes);
            this.periapsis = periapsis;
            this.apoapsis = apoapsis;
        }

        public static ApsidesMessage from(byte[] bytes) {
            float periapsis = ByteBuffer.wrap(bytes, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            float apoapsis = ByteBuffer.wrap(bytes, 4, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            return new ApsidesMessage(periapsis, apoapsis, bytes);
        }

        @Override
        public boolean equals(Payload p) {
            if (p instanceof ApsidesMessage) {
                return periapsis == ((ApsidesMessage)p).periapsis && apoapsis == ((ApsidesMessage)p).apoapsis;
            }
            return false;
        }

        public String toString() {
            return MessageFormat.format("Apsides - Periapsis: {0}, apoapsis: {1}", periapsis, apoapsis);
        }
    }

    /** An Apsides Time message. */
    public static class ApsidesTimeMessage extends Payload {
        public final int periapsis; /** (32-bits) Time until the current vessel's orbital periapsis, in seconds. */
        public final int apoapsis; /** (32-bits) Time until the current vessel's orbital apoapsis, in seconds. */

        public ApsidesTimeMessage(final int periapsis, final int apoapsis) {
            super(ByteBuffer.allocate(8).putFloat(periapsis).putFloat(apoapsis).array());
            this.periapsis = periapsis;
            this.apoapsis = apoapsis;
        }

        public ApsidesTimeMessage(final int periapsis, final int apoapsis, final byte[] bytes) {
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
        public boolean equals(Payload p) {
            if (p instanceof ApsidesTimeMessage) {
                return periapsis == ((ApsidesTimeMessage)p).periapsis && apoapsis == ((ApsidesTimeMessage)p).apoapsis;
            }
            return false;
        }

        public String toString() {
            return MessageFormat.format("Apsides Time - Periapsis: {0}, apoapsis: {1}", periapsis, apoapsis);
        }
    }

    /** A Resource message.
     All resource messages use this public static class for sending data. */
    public static class ResourceMessage extends Payload {
        public final float total; /**< Maximum capacity of the resource. */
        public final float available; /**< Current resource level. */

        public ResourceMessage(float total, float available) {
            super(ByteBuffer.allocate(8).putFloat(total).putFloat(available).array());
            this.total = total;
            this.available = available;
        }

        public ResourceMessage(float total, float available, final byte[] bytes) {
            super(bytes);
            this.total = total;
            this.available = available;
        }

        public static ResourceMessage from(byte[] bytes) {
            float total = ByteBuffer.wrap(bytes, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            float available = ByteBuffer.wrap(bytes, 4, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            return new ResourceMessage(total, available);
        }

        @Override
        public boolean equals(Payload p) {
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

        public VelocityMessage(float orbital, float surface, float vertical) {
            super(ByteBuffer.allocate(8).putFloat(orbital).putFloat(surface).putFloat(vertical).array());
            this.orbital = orbital;
            this.surface = surface;
            this.vertical = vertical;
        }

        public VelocityMessage(float orbital, float surface, float vertical, final byte[] bytes) {
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
        public boolean equals(Payload p) {
            if (p instanceof VelocityMessage) {
                return orbital == ((VelocityMessage)p).orbital && surface == ((VelocityMessage)p).surface && vertical == ((VelocityMessage)p).vertical;
            }
            return false;
        }

        @Override
        public String toString() {
            return "Velocity - orbital: " + orbital + " surface: " + surface + " vertical: " + vertical;
        }
    }

    /** A Target information message. */
    public static class TargetMessage extends Payload {
        public final float distance; /**< Distance to target. */
        public final float velocity; /**< Velocity relative to target. */

        public TargetMessage(float distance, float velocity) {
            super(ByteBuffer.allocate(8).putFloat(distance).putFloat(velocity).array());
            this.distance = distance;
            this.velocity = velocity;
        }

        public TargetMessage(float distance, float velocity, final byte[] bytes) {
            super(bytes);
            this.distance = distance;
            this.velocity = velocity;
        }

        @Override
        public String toString() {
            return MessageFormat.format("Target - distance: {0} velocity: {1}", distance, velocity);
        }

        @Override
        public boolean equals(Payload p) {
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

        public AirspeedMessage(float indicatedAirSpeed, float mach) {
            super(ByteBuffer.allocate(8).putFloat(indicatedAirSpeed).putFloat(mach).array());
            this.indicatedAirSpeed = indicatedAirSpeed;
            this.mach = mach;
        }

        public AirspeedMessage(float indicatedAirSpeed, float mach, final byte[] bytes) {
            super(bytes);
            this.indicatedAirSpeed = indicatedAirSpeed;
            this.mach = mach;
        }

        @Override
        public String toString() {
            return MessageFormat.format("Air speed - Indicated: {0} MACH: {1}", indicatedAirSpeed, mach);
        }

        @Override
        public boolean equals(Payload p) {
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
        private final byte actionGroupStatus;

        public ActionGroupMessage(byte actionGroupStatus, byte[] bytes) {
            super(bytes);
            this.actionGroupStatus = actionGroupStatus;
        }

        @Override
        public String toString() {
            boolean stage = (actionGroupStatus & MessageType.ActionGroupIndexes.STAGE_ACTION.getValue()) > 0;
            boolean gear = (actionGroupStatus & MessageType.ActionGroupIndexes.GEAR_ACTION.getValue()) > 0;
            boolean lights = (actionGroupStatus & MessageType.ActionGroupIndexes.LIGHT_ACTION.getValue()) > 0;
            boolean rcs = (actionGroupStatus & MessageType.ActionGroupIndexes.RCS_ACTION.getValue()) > 0;
            boolean sas = (actionGroupStatus & MessageType.ActionGroupIndexes.SAS_ACTION.getValue()) > 0;
            boolean brakes = (actionGroupStatus & MessageType.ActionGroupIndexes.BRAKES_ACTION.getValue()) > 0;
            boolean abort = (actionGroupStatus & MessageType.ActionGroupIndexes.ABORT_ACTION.getValue()) > 0;
            return MessageFormat.format("Action Group Status: stage: {0} gear: {1} lights: {2} rcs: {3} sas: {4} brakes: {5} abort: {6}", stage, gear, lights, rcs, sas, brakes, abort);
        }

        @Override
        public boolean equals(Payload p) {
            return (p instanceof ActionGroupMessage) && (actionGroupStatus == ((ActionGroupMessage) p).actionGroupStatus);
        }

        public static ActionGroupMessage from(byte[] bytes) {
            return new ActionGroupMessage(bytes[0], bytes);
        }
    }

    public static class SphereOfInfluenceMessage extends Payload {
        private final String sphereOfInfluenceMessage;

        public SphereOfInfluenceMessage(String sphereOfInfluenceMessage, byte[] bytes) {
            super(bytes);
            this.sphereOfInfluenceMessage = sphereOfInfluenceMessage;
        }

        @Override
        public String toString() {
            return MessageFormat.format("Sphere of Influence: {0}", sphereOfInfluenceMessage);
        }

        @Override
        public boolean equals(Payload p) {
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

        public RotationMessage(final int pitch, final int roll, int yaw, byte mask) {
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

        public TranslationMessage(final int x, final int y, int z, byte mask) {
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

        public WheelMessage(final int steer, final int throttle, byte mask) {
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
