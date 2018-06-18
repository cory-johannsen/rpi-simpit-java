package cjohannsen;

import cjohannsen.protocol.MessageType;
import cjohannsen.protocol.PacketSource;
import cjohannsen.protocol.Payload;
import com.fazecast.jSerialComm.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static cjohannsen.protocol.MessageType.Datagram.*;

@SpringBootApplication
public class Application {
    static final Logger logger = LoggerFactory.getLogger(Application.class);
    public static final int HEARTBEAT_FREQUENCY_SECONDS = 60;
    public static final int BAUD_RATE = 9600;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean SerialPort serialPort() {
        logger.info("*************************");
        logger.info("Retrieving serial port - ");

        SerialPort comPort = SerialPort.getCommPorts()[0];
        comPort.setBaudRate(BAUD_RATE);
        logger.info("Identified serial port " + comPort.getDescriptivePortName());
        logger.info("Port description: " + comPort.getPortDescription());
        logger.info("System comPort name: " + comPort.getSystemPortName());
        logger.info("Baud rate: " + comPort.getBaudRate());
        logger.info("Data bits: " + comPort.getNumDataBits());
        logger.info("Stop bits " + comPort.getNumStopBits());
        logger.info("Parity " + comPort.getParity());

        logger.info("*************************");

        logger.info("Opening serial port - ");
        final boolean isOpen = comPort.openPort();
        if (!isOpen) {
            logger.error("Serial port failed to open. Exiting.");
            System.exit(-1);
        }
        logger.info("Serial port open.");
        return comPort;
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            logger.info("Initializing SimpitHost...");

            PacketSource packetSource = ctx.getBean(PacketSource.class);
            SimpitHost simpitHost = ctx.getBean(SimpitHost.class);
            ApplicationState applicationState = ctx.getBean(ApplicationState.class);
            packetSource.start();

            final boolean handshakeSuccess = simpitHost.handshake();
            if (!handshakeSuccess) {
                logger.error("Handshaking failed!");
            }
            else {
                logger.info("Handshaking success.");

                Payload.AltitudeMessage altitudeMessage = new Payload.AltitudeMessage(0, 0);
                Payload.ApsidesMessage apsidesMessage = new Payload.ApsidesMessage(0, 0);


                logger.info("Registering datagram handlers.");
                simpitHost.registerHandler(ECHO_RESP_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    logger.debug(MessageFormat.format("{0}: {1}", type.printableString(), new String(message).trim()));
                });
                simpitHost.registerHandler(ALTITUDE_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    Payload.AltitudeMessage incomingAltitudeMessage = Payload.AltitudeMessage.from(message);
                    Optional<Payload> cachedDatagram = applicationState.getCachedDatagram(ALTITUDE_MESSAGE);
                    if (!cachedDatagram.isPresent() || !cachedDatagram.get().equals(incomingAltitudeMessage)) {
                        applicationState.setCachedDatagram(ALTITUDE_MESSAGE, incomingAltitudeMessage);
                        logger.info(incomingAltitudeMessage.toString());
                    }
                });
                simpitHost.registerHandler(APSIDES_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    Payload.ApsidesMessage incomingApsidesMessage = Payload.ApsidesMessage.from(message);
                    Optional<Payload> cachedDatagram = applicationState.getCachedDatagram(APSIDES_MESSAGE);
                    if (!cachedDatagram.isPresent() || !cachedDatagram.get().equals(incomingApsidesMessage)) {
                        applicationState.setCachedDatagram(APSIDES_MESSAGE, incomingApsidesMessage);
                        logger.info(incomingApsidesMessage.toString());
                    }
                });
                simpitHost.registerHandler(LF_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    updateResourceDatagram(applicationState, type, message);
                });
                simpitHost.registerHandler(LF_STAGE_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    updateResourceDatagram(applicationState, type, message);
                });
                simpitHost.registerHandler(OX_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    updateResourceDatagram(applicationState, type, message);
                });
                simpitHost.registerHandler(OX_STAGE_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    updateResourceDatagram(applicationState, type, message);
                });
                simpitHost.registerHandler(SF_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    updateResourceDatagram(applicationState, type, message);
                });
                simpitHost.registerHandler(SF_STAGE_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    updateResourceDatagram(applicationState, type, message);
                });
                simpitHost.registerHandler(MONO_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    updateResourceDatagram(applicationState, type, message);
                });
                simpitHost.registerHandler(ELECTRIC_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    updateResourceDatagram(applicationState, type, message);
                });
                simpitHost.registerHandler(EVA_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    updateResourceDatagram(applicationState, type, message);
                });
                simpitHost.registerHandler(ORE_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    updateResourceDatagram(applicationState, type, message);
                });
                simpitHost.registerHandler(AB_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    updateResourceDatagram(applicationState, type, message);
                });
                simpitHost.registerHandler(AB_STAGE_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    updateResourceDatagram(applicationState, type, message);
                });
                simpitHost.registerHandler(VELOCITY_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    Payload.VelocityMessage incomingVelocityMessage = Payload.VelocityMessage.from(message);
                    Optional<Payload> cachedDatagram = applicationState.getCachedDatagram(VELOCITY_MESSAGE);
                    if (!cachedDatagram.isPresent() || !cachedDatagram.get().equals(incomingVelocityMessage)) {
                        applicationState.setCachedDatagram(VELOCITY_MESSAGE, incomingVelocityMessage);
                        logger.info(incomingVelocityMessage.toString());
                    }
                });
                simpitHost.registerHandler(ACTIONSTATUS_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    Payload.ActionGroupMessage incomingActionGroupMessage = Payload.ActionGroupMessage.from(message);
                    Optional<Payload> cachedDatagram = applicationState.getCachedDatagram(ACTIONSTATUS_MESSAGE);
                    if (!cachedDatagram.isPresent() || !cachedDatagram.get().equals(incomingActionGroupMessage)) {
                        applicationState.setCachedDatagram(ACTIONSTATUS_MESSAGE, incomingActionGroupMessage);
                        logger.info(incomingActionGroupMessage.toString());
                    }
                });
                simpitHost.registerHandler(APSIDESTIME_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    Payload.ApsidesTimeMessage incomingApsidesTimesMessage = Payload.ApsidesTimeMessage.from(message);
                    Optional<Payload> cachedDatagram = applicationState.getCachedDatagram(APSIDESTIME_MESSAGE);
                    if(!cachedDatagram.isPresent() || !cachedDatagram.get().equals(incomingApsidesTimesMessage)) {
                        applicationState.setCachedDatagram(APSIDESTIME_MESSAGE, incomingApsidesTimesMessage);
                        logger.info(incomingApsidesTimesMessage.toString());
                    }
                });
                simpitHost.registerHandler(TARGETINFO_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    Payload.TargetMessage targetMessage = Payload.TargetMessage.from(message);
                    logger.info(MessageFormat.format("Target - distance: {0} velocity: {1}", targetMessage.distance, targetMessage.velocity));
                });
                simpitHost.registerHandler(SOI_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    Payload.SphereOfInfluenceMessage sphereOfInfluenceMessage = Payload.SphereOfInfluenceMessage.from(message);
                    Optional<Payload> cachedDatagram = applicationState.getCachedDatagram(SOI_MESSAGE);
                    if(!cachedDatagram.isPresent() || !cachedDatagram.get().equals(sphereOfInfluenceMessage)) {
                        applicationState.setCachedDatagram(SOI_MESSAGE, sphereOfInfluenceMessage);
                        logger.info(sphereOfInfluenceMessage.toString());
                    }
                });
                simpitHost.registerHandler(AIRSPEED_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    Payload.AirspeedMessage airspeedMessage = Payload.AirspeedMessage.from(message);
                    Optional<Payload> cachedDatagram = applicationState.getCachedDatagram(AIRSPEED_MESSAGE);
                    if(!cachedDatagram.isPresent() || !cachedDatagram.get().equals(airspeedMessage)) {
                        applicationState.setCachedDatagram(AIRSPEED_MESSAGE, airspeedMessage);
                        logger.info(airspeedMessage.toString());
                    }
                    logger.info(MessageFormat.format("Airspeed - indicated:{0} mach: {1}", airspeedMessage.indicatedAirSpeed, airspeedMessage.mach));
                });

                logger.info("Subscribing to message channels.");
                Arrays.stream(MessageType.Datagram.values()).filter((d) -> d != MessageType.Datagram.UNDEFINED).forEach(simpitHost::enableChannel);

                logger.info("Initiating echo heartbeat.");
                Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> simpitHost.sendEchoRequest("rpi-simpit heartbeat"), 0, HEARTBEAT_FREQUENCY_SECONDS, TimeUnit.SECONDS);

                Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> logger.info(applicationState.toString()), 0, 10, TimeUnit.SECONDS);

            }
        };
    }

    private void updateResourceDatagram(final ApplicationState applicationState, final MessageType.Datagram type, final byte[] message) {
        Payload.ResourceMessage resourceMessage = Payload.ResourceMessage.from(message);
        Optional<Payload> cachedDatagram = applicationState.getCachedDatagram(type);
        if (!cachedDatagram.isPresent() || !cachedDatagram.get().equals(resourceMessage)) {
            applicationState.setCachedDatagram(type, resourceMessage);
            logger.info(MessageFormat.format("{0}: {1}", type, resourceMessage));
        }
    }

}