package cjohannsen.protocol;

/** Constants for inbound and outbound message IDs.
 */
public class MessageType {

    /** Command packets.
        These packet types are used for both inbound and outbound messages.
     */
    public enum Command {
        /// Sync message. Used for handshaking.
        SYNC_MESSAGE(0),
        /// Echo request. Either end can send this, and an echo response is expected.
        ECHO_REQ_MESSAGE(1),
        /// Echo response. Sent in reply to an echo request.
        ECHO_RESP_MESSAGE(2),

        /** Register to receive messages on a given channel. */
        REGISTER_MESSAGE(8),
        /** Deregister), indicate that no further messages
         for the given channel should be sent. */
        DEREGISTER_MESSAGE(9),
        // Custom action packets activate and deactivate custom action groups
        /** Activate the given Custom Action Group(s). */
        CAGACTIVATE_MESSAGE(10),
        /** Deactivate the given Custom Action Group(s). */
        CAGDEACTIVATE_MESSAGE(11),
        /** Toggle the given Custom Action Group(s) (Active CAGs will
         deactivate), inactive CAGs will activate). */
        CAGTOGGLE_MESSAGE(12),
        /** Activate the given standard Action Group(s).
         Note that *every request* to activate the Stage action group will
         result in the next stage being activated.
         For all other action groups), multiple activate requests will have
         no effect.
         */
        AGACTIVATE_MESSAGE(13),
        /** Deactivate the given standard Action Group(s). */
        AGDEACTIVATE_MESSAGE(14),
        /** Toggle the given standard Action Group(s). */
        AGTOGGLE_MESSAGE(15),
        /** Send vessel rotation commands. */
        ROTATION_MESSAGE(16),
        /** Send vessel translation commands. */
        TRANSLATION_MESSAGE(17),
        /** Send wheel steering/throttle commands. */
        WHEEL_MESSAGE(18),
        /** Send vessel throttle commands. */
        THROTTLE_MESSAGE(19),

        UNDEFINED(Integer.MAX_VALUE);

        public static final Command from(int value) {
            for(Command c : Command.values()) {
                if (c.value == value) {
                    return c;
                }
            }
            return UNDEFINED;
        }

        private final int value;
        Command(int value) { this.value = value; }
        public int getValue() { return value; }
    }

    /** Datagram packets.
        IDs for packets that go from the game to devices.
     */
    public enum Datagram {
        /// Sync message. Used for handshaking.
        SYNC_MESSAGE(0),
        /// Echo request. Either end can send this, and an echo response is expected.
        ECHO_REQ_MESSAGE(1),
        /// Echo response. Sent in reply to an echo request.
        ECHO_RESP_MESSAGE(2),
        /// Scene change packets are sent by the plugin when
        /// entering or leaving the flight scene.
        SCENE_CHANGE_MESSAGE(3),
        /** Sea level and surface altitude.
         MessageType on this channel contain an altitudeMessage.
         */
        ALTITUDE_MESSAGE(8),
        /** Apoapsis and periapsis.
         MessageType on this channel contain an apsidesMessage.
         */
        APSIDES_MESSAGE(9),

        // Resources:
        /** Liquid fuel in the vessel.
         MessageType on this channel contain a resourceMessage. */
        LF_MESSAGE(10),
        /** Liquid fuel in the current stage.
         MessageType on this channel contain a resourceMessage. */
        LF_STAGE_MESSAGE(11),
        /** Oxidizer in the vessel.
         MessageType on this channel contain a resourceMessage. */
        OX_MESSAGE(12),
        /** Oxidizer in the current stage.
         MessageType on this channel contain a resourceMessage. */
        OX_STAGE_MESSAGE(13),
        /** Solid fuel in the vessel.
         MessageType on this channel contain a resourceMessage. */
        SF_MESSAGE(14),
        /** Solid fuel in the current stage.
         MessageType on this channel contain a resourceMessage. */
        SF_STAGE_MESSAGE(15),
        /** Monoprollent in the vessel.
         MessageType on this channel contain a resourceMessage. */
        MONO_MESSAGE(16),
        /** Electic Charge in the vessel.
         MessageType on this channel contain a resourceMessage. */
        ELECTRIC_MESSAGE(17),
        /** EVA propellant. Only available for Kerbals on EVA.
         MessageType on this channel contain a resourceMessage. */
        EVA_MESSAGE(18),
        /** Ore in the vessel.
         MessageType on this channel contain a resourceMessage. */
        ORE_MESSAGE(19),
        /** Ablator in the vessel.
         MessageType on this channel contain a resourceMessage. */
        AB_MESSAGE(20),
        /** Ablator in the current stage.
         MessageType on this channel contain a resourceMessage. */
        AB_STAGE_MESSAGE(21),
        /** Vessel velocity.
         MessageType on this channel contain a velocityMessage. */
        VELOCITY_MESSAGE(22),

        /** Action groups.
         MessageType on this channel contain a single byte representing the
         currently active action groups. A given action group can be checked
         by performing a bitwise AND with the message. For example:

         \code
         if (msg & SAS_ACTION) {
         // code to execute if SAS is active
         }
         \endcode

         Possible action groups are:

         - STAGE_ACTION
         - GEAR_ACTION
         - LIGHT_ACTION
         - RCS_ACTION
         - SAS_ACTION
         - BRAKES_ACTION
         - ABORT_ACTION
         */
        ACTIONSTATUS_MESSAGE(23),

        /** Time to the next apoapsis and periapsis.
         MessageType on this channel contain an apsidesTimeMessage. */
        APSIDESTIME_MESSAGE(24),

        /** Information about targetted object.
         This channel delivers messages about the object targetted by the
         active vessel. MessageType on this channel contain a targetInfoMessage. */
        TARGETINFO_MESSAGE(25),

        /** Name of current Sphere of Influence.
         This channel delivers an ASCII string containing the name of the body
         the active vessel is currently orbiting. Note that this is always the
         English name), regardless of the language the game is currently set to. */
        SOI_MESSAGE(26),

        /** Information about airspeed.
         This channel delivers messages containing indicated airspeed and
         mach number for the active vessel. */
        AIRSPEED_MESSAGE(27),

        UNDEFINED(Integer.MAX_VALUE);

        public static final Datagram from(int value) {
            for(Datagram d : Datagram.values()) {
                if (d.value == value) {
                    return d;
                }
            }
            return UNDEFINED;
        }

        private final int value;
        Datagram(int value) { this.value = value; }
        public int getValue() { return value; }
        public boolean equals(Datagram d) {
            return d.value == this.value;
        }
        public String printableString() {
            switch (this) {
                case SYNC_MESSAGE:
                    return "SYNC";
                case ECHO_REQ_MESSAGE:
                    return "ECHO request";
                case ECHO_RESP_MESSAGE:
                    return "ECHO response";
                case SCENE_CHANGE_MESSAGE:
                    return "Scene change";
                case ALTITUDE_MESSAGE:
                    return "Altitude";
                case APSIDES_MESSAGE:
                    return "Apsides";
                case LF_MESSAGE:
                    return "Liquid Fuel";
                case LF_STAGE_MESSAGE:
                    return "Liquid Fuel (stage)";
                case OX_MESSAGE:
                    return "Oxidizer";
                case OX_STAGE_MESSAGE:
                    return "Oxidizer (stage)";
                case SF_MESSAGE:
                    return "Solid Fuel";
                case SF_STAGE_MESSAGE:
                    return "Solid Fuel (stage)";
                case MONO_MESSAGE:
                    return "Monopropellant";
                case ELECTRIC_MESSAGE:
                    return "Electric Charge";
                case EVA_MESSAGE:
                    return "EVA Monopropellant";
                case ORE_MESSAGE:
                    return "Ore";
                case AB_MESSAGE:
                    return "Ablator";
                case AB_STAGE_MESSAGE:
                    return "Ablator (stage)";
                case VELOCITY_MESSAGE:
                    return "Velocity";
                case ACTIONSTATUS_MESSAGE:
                    return "Actiongroup Status";
                case APSIDESTIME_MESSAGE:
                    return "Apsides Time";
                case TARGETINFO_MESSAGE:
                    return "Target";
                case SOI_MESSAGE:
                    return "Sphere of Influence";
                case AIRSPEED_MESSAGE:
                    return "Airspeed";
                default:
                    return "Undefined";
            }
        }
    }

    /** Action Group Indexes
        These are used to mask out elements of an ACTIONSTATUS_MESSAGE.
     */
    public enum ActionGroupIndex {
        /** Bitmask for the Stage action group. */
        STAGE_ACTION(1),
        /** Bitmask for the Gear action group. */
        GEAR_ACTION(2),
        /** Bitmask for the Light action group. */
        LIGHT_ACTION(4),
        /** Bitmask for the RCS action group. */
        RCS_ACTION(8),
        /** Bitmask for the SAS action group. */
        SAS_ACTION(16),
        /** Bitmask for the Brakes action group. */
        BRAKES_ACTION(32),
        /** Bitmask for the Abort action group. */
        ABORT_ACTION(64);

        private final int id;
        ActionGroupIndex(int id) { this.id = id; }
        public int getValue() { return id; }
    }

}