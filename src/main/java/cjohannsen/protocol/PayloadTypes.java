package cjohannsen.protocol;

public class PayloadTypes {

    /** An Altitude message. */
    public static class AltitudeMessage {
        float sealevel; /**< Altitude above sea level. */
        float surface; /**< Surface altitude at current position. */
    };

    /** An Apsides message. */
    public static class ApsidesMessage {
        float periapsis; /**< Current vessel's orbital periapsis. */
        float apoapsis; /**< Current vessel's orbital apoapsis. */
    };

    /** An Apsides Time message. */
    public static class ApsidesTimeMessage {
        int periapsis; /** (32-bits) Time until the current vessel's orbital periapsis, in seconds. */
        int apoapsis; /** (32-bits) Time until the current vessel's orbital apoapsis, in seconds. */
    };

    /** A Resource message.
     All resource messages use this public static class for sending data. */
    public static class ResourceMessage {
        float total; /**< Maximum capacity of the resource. */
        float available; /**< Current resource level. */
    };

    /** A Velocity message. */
    public static class VelocityMessage {
        float orbital; /**< Orbital velocity. */
        float surface; /**< Surface velocity. */
        float vertical; /**< Vertical velocity. */
    };

    /** A Target information message. */
    public static class TargetMessage {
        float distance; /**< Distance to target. */
        float velocity; /**< Velocity relative to target. */
    };

    /** An Airspeed information message. */
    public static class AirspeedMessage {
        float IAS; /**< Indicated airspeed. */
        float mach; /**< Mach number. */
    };

    /** A vessel rotation message.
     This public static class contains information about vessel rotation commands. */
    public static class RotationMessage {
        int pitch; /**< (16-bits) Vessel pitch. */
        int roll; /**< (16-bits) Vessel roll. */
        int yaw; /**< (16-bits) Vessel yaw. */
        /** The mask indicates which elements are intentionally set. Unset elements
         should be ignored. It should be one or more of:

         - 1: pitch
         - 2: roll
         - 4: yaw
         */
        byte mask;
    };

    /** A vessel translation message.
     This public static class contains information about vessel translation commands. */
    public static class TranslationMessage {
        int X; /**< (16-bits) Translation along the X axis. */
        int Y; /**< (16-bits) Translation along the Y axis. */
        int Z; /**< (16-bits) Translation along the Z axis. */
        /** The mask indicates which elements are intentionally set. Unset elements
         should be ignored. It should be one or more of:

         - 1: X
         - 2: Y
         - 4: Z
         */
        byte mask;
    };

    /** A wheel control message.
     This sturct contains information about wheel steering and throttle. */
    public static class WheelMessage {
        int steer; /**< (16-bits) Wheel steer. */
        int throttle; /**< (16-bits)  Wheel throttle. */
        /** The mask indicates which elements are intentionally set. Unset elements
         should be ignored. It should be one or more of:

         - 1: steer
         - 2: throttle
         */
        byte mask;
    };

}
