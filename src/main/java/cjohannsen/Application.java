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

                logger.info("Registering datagram handlers.");
                simpitHost.registerHandler(ECHO_RESP_MESSAGE, (b) ->  null, (MessageType.Datagram type, byte[] message, Payload.Provider provider) -> {
                    logger.debug(MessageFormat.format("{0}: {1}", type.printableString(), new String(message).trim()));
                });
                simpitHost.registerHandler(ALTITUDE_MESSAGE, Payload.AltitudeMessage::from, (t, b, p) -> updateDatagram(applicationState, t, b, p));
                simpitHost.registerHandler(APSIDES_MESSAGE, Payload.ApsidesMessage::from, (t, b, p) -> updateDatagram(applicationState, t, b, p));
                simpitHost.registerHandler(LF_MESSAGE, Payload.ResourceMessage::from, (t, b, p) -> updateDatagram(applicationState, t, b, p));
                simpitHost.registerHandler(LF_STAGE_MESSAGE, Payload.ResourceMessage::from, (t, b, p) -> updateDatagram(applicationState, t, b, p));
                simpitHost.registerHandler(OX_MESSAGE, Payload.ResourceMessage::from, (t, b, p) -> updateDatagram(applicationState, t, b, p));
                simpitHost.registerHandler(OX_STAGE_MESSAGE, Payload.ResourceMessage::from, (t, b, p) -> updateDatagram(applicationState, t, b, p));
                simpitHost.registerHandler(SF_MESSAGE, Payload.ResourceMessage::from, (t, b, p) -> updateDatagram(applicationState, t, b, p));
                simpitHost.registerHandler(SF_STAGE_MESSAGE, Payload.ResourceMessage::from, (t, b, p) -> updateDatagram(applicationState, t, b, p));
                simpitHost.registerHandler(MONO_MESSAGE, Payload.ResourceMessage::from, (t, b, p) -> updateDatagram(applicationState, t, b, p));
                simpitHost.registerHandler(ELECTRIC_MESSAGE, Payload.ResourceMessage::from, (t, b, p) -> updateDatagram(applicationState, t, b, p));
                simpitHost.registerHandler(EVA_MESSAGE, Payload.ResourceMessage::from, (t, b, p) -> updateDatagram(applicationState, t, b, p));
                simpitHost.registerHandler(ORE_MESSAGE, Payload.ResourceMessage::from, (t, b, p) -> updateDatagram(applicationState, t, b, p));
                simpitHost.registerHandler(AB_MESSAGE, Payload.ResourceMessage::from, (t, b, p) -> updateDatagram(applicationState, t, b, p));
                simpitHost.registerHandler(AB_STAGE_MESSAGE, Payload.ResourceMessage::from, (t, b, p) -> updateDatagram(applicationState, t, b, p));
                simpitHost.registerHandler(VELOCITY_MESSAGE, Payload.VelocityMessage::from, (t, b, p) -> updateDatagram(applicationState, t, b, p));
                simpitHost.registerHandler(ACTIONSTATUS_MESSAGE, Payload.ActionGroupMessage::from, (t, b, p) -> updateDatagram(applicationState, t, b, p));
                simpitHost.registerHandler(APSIDESTIME_MESSAGE, Payload.ApsidesTimeMessage::from, (t, b, p) -> updateDatagram(applicationState, t, b, p));
                simpitHost.registerHandler(TARGETINFO_MESSAGE, Payload.TargetMessage::from, (t, b, p) -> updateDatagram(applicationState, t, b, p));
                simpitHost.registerHandler(SOI_MESSAGE, Payload.SphereOfInfluenceMessage::from, (t, b, p) -> updateDatagram(applicationState, t, b, p));
                simpitHost.registerHandler(AIRSPEED_MESSAGE, Payload.AirspeedMessage::from, (t, b, p) -> updateDatagram(applicationState, t, b, p));

                logger.info("Subscribing to message channels.");
                Arrays.stream(MessageType.Datagram.values()).filter((d) -> d != MessageType.Datagram.UNDEFINED).forEach(simpitHost::enableChannel);

                logger.info("Initiating echo heartbeat.");
                Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> simpitHost.sendEchoRequest("rpi-simpit heartbeat"), 0, HEARTBEAT_FREQUENCY_SECONDS, TimeUnit.SECONDS);

                Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> logger.info(applicationState.toString()), 0, 10, TimeUnit.SECONDS);

            }
        };
    }

    private void updateDatagram(final ApplicationState applicationState, final MessageType.Datagram type, final byte[] message, final Payload.Provider provider) {
        Payload datagram = provider.provide(message);
        Optional<Payload> cachedDatagram = applicationState.getCachedDatagram(type);
        if (!cachedDatagram.isPresent() || !cachedDatagram.get().equals(datagram)) {
            applicationState.setCachedDatagram(type, datagram);
            logger.info(datagram.toString());
        }

    }

}