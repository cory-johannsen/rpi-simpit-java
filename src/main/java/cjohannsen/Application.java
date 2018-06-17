package cjohannsen;

import cjohannsen.protocol.MessageType;
import cjohannsen.protocol.PacketSource;
import cjohannsen.protocol.PayloadTypes;
import com.fazecast.jSerialComm.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Duration;
import java.util.Arrays;

import static cjohannsen.protocol.MessageType.Datagram.*;

@SpringBootApplication
public class Application {
    static final Logger logger = LoggerFactory.getLogger(Application.class);
    public static final int HEARTBEAT_FREQUENCY_SECONDS = 60;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean SerialPort serialPort() {
        logger.info("*************************");
        logger.info("Opening serial port - ");

        SerialPort comPort = SerialPort.getCommPorts()[0];
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
            packetSource.start();

            final boolean handshakeSuccess = simpitHost.handshake();
            if (!handshakeSuccess) {
                logger.error("Handshaking failed!");
            }
            else {
                logger.info("Handshaking success.");

                logger.info("Registering datagram handlers.");
                simpitHost.registerHandler(ECHO_RESP_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    logger.info("ECHO: " + new String(message).trim());
                    return true;
                });
                simpitHost.registerHandler(ALTITUDE_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    PayloadTypes.AltitudeMessage altitudeMessage = PayloadTypes.AltitudeMessage.from(message);
                    logger.info("Altitude - Sea Level: " + altitudeMessage.sealevel + ", Surface: " + altitudeMessage.surface);
                    return true;
                });
                simpitHost.registerHandler(APSIDES_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    PayloadTypes.ApsidesMessage apsidesMessage = PayloadTypes.ApsidesMessage.from(message);
                    logger.info("Apsides - Periapsis: " + apsidesMessage.periapsis + ", apoapsis: " + apsidesMessage.apoapsis);
                    return true;
                });
                simpitHost.registerHandler(LF_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    logger.info("LF_MESSAGE: " + Util.hexString(message));
                    return true;
                });
                simpitHost.registerHandler(LF_STAGE_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    logger.info("LF_STAGE_MESSAGE: " + Util.hexString(message));
                    return true;
                });
                simpitHost.registerHandler(OX_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    logger.info("OX_MESSAGE: " + Util.hexString(message));
                    return true;
                });
                simpitHost.registerHandler(OX_STAGE_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    logger.info("OX_STAGE_MESSAGE: " + Util.hexString(message));
                    return true;
                });
                simpitHost.registerHandler(SF_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    logger.info("SF_MESSAGE: " + Util.hexString(message));
                    return true;
                });
                simpitHost.registerHandler(SF_STAGE_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    logger.info("SF_STAGE_MESSAGE: " + Util.hexString(message));
                    return true;
                });
                simpitHost.registerHandler(MONO_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    logger.info("MONO_MESSAGE: " + Util.hexString(message));
                    return true;
                });
                simpitHost.registerHandler(ELECTRIC_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    logger.info("ELECTRIC_MESSAGE: " + Util.hexString(message));
                    return true;
                });
                simpitHost.registerHandler(EVA_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    logger.info("EVA_MESSAGE: " + Util.hexString(message));
                    return true;
                });
                simpitHost.registerHandler(ORE_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    logger.info("ORE_MESSAGE: " + Util.hexString(message));
                    return true;
                });
                simpitHost.registerHandler(AB_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    logger.info("AB_MESSAGE: " + Util.hexString(message));
                    return true;
                });
                simpitHost.registerHandler(AB_STAGE_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    logger.info("AB_STAGE_MESSAGE: " + Util.hexString(message));
                    return true;
                });
                simpitHost.registerHandler(VELOCITY_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    logger.info("VELOCITY_MESSAGE: " + Util.hexString(message));
                    return true;
                });
                simpitHost.registerHandler(ACTIONSTATUS_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    logger.info("ACTIONSTATUS_MESSAGE: " + Util.hexString(message));

                    return true;
                });
                simpitHost.registerHandler(APSIDESTIME_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    logger.info("APSIDESTIME_MESSAGE: " + Util.hexString(message));
                    return true;
                });
                simpitHost.registerHandler(TARGETINFO_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    logger.info("TARGETINFO_MESSAGE: " + Util.hexString(message));

                    return true;
                });
                simpitHost.registerHandler(SOI_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    logger.info("SOI_MESSAGE: " + Util.hexString(message));
                    return true;
                });
                simpitHost.registerHandler(AIRSPEED_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    logger.info("AIRSPEED_MESSAGE: " + Util.hexString(message));
                    return true;
                });

                logger.info("Subscribing to message channels.");
                Arrays.stream(MessageType.Datagram.values()).forEach(simpitHost::disableChannel);
                simpitHost.enableChannel(MessageType.Datagram.ALTITUDE_MESSAGE);
                simpitHost.enableChannel(MessageType.Datagram.APSIDES_MESSAGE);
                    logger.info("ECHO: " + new String(message));
                    return true;
                });
                simpitHost.registerHandler(ALTITUDE_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    logger.info("ALTITUDE_MESSAGE: " + Util.hexString(message));
                    return true;
                });
//                simpitHost.registerHandler(APSIDES_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
//                    logger.info("APSIDES_MESSAGE: " + Util.hexString(message));
//                    return true;
//                });
//                simpitHost.registerHandler(LF_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
//                    logger.info("LF_MESSAGE: " + Util.hexString(message));
//                    return true;
//                });
//                simpitHost.registerHandler(LF_STAGE_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
//                    logger.info("LF_STAGE_MESSAGE: " + Util.hexString(message));
//                    return true;
//                });
//                simpitHost.registerHandler(OX_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
//                    logger.info("OX_MESSAGE: " + Util.hexString(message));
//                    return true;
//                });
//                simpitHost.registerHandler(OX_STAGE_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
//                    logger.info("OX_STAGE_MESSAGE: " + Util.hexString(message));
//                    return true;
//                });
//                simpitHost.registerHandler(SF_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
//                    logger.info("SF_MESSAGE: " + Util.hexString(message));
//                    return true;
//                });
//                simpitHost.registerHandler(SF_STAGE_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
//                    logger.info("SF_STAGE_MESSAGE: " + Util.hexString(message));
//                    return true;
//                });
//                simpitHost.registerHandler(MONO_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
//                    logger.info("MONO_MESSAGE: " + Util.hexString(message));
//                    return true;
//                });
//                simpitHost.registerHandler(ELECTRIC_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
//                    logger.info("ELECTRIC_MESSAGE: " + Util.hexString(message));
//                    return true;
//                });
//                simpitHost.registerHandler(EVA_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
//                    logger.info("EVA_MESSAGE: " + Util.hexString(message));
//                    return true;
//                });
//                simpitHost.registerHandler(ORE_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
//                    logger.info("ORE_MESSAGE: " + Util.hexString(message));
//                    return true;
//                });
//                simpitHost.registerHandler(AB_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
//                    logger.info("AB_MESSAGE: " + Util.hexString(message));
//                    return true;
//                });
//                simpitHost.registerHandler(AB_STAGE_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
//                    logger.info("AB_STAGE_MESSAGE: " + Util.hexString(message));
//                    return true;
//                });
//                simpitHost.registerHandler(VELOCITY_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
//                    logger.info("VELOCITY_MESSAGE: " + Util.hexString(message));
//                    return true;
//                });

                logger.info("Initiating echo heartbeat.");
                while (true) {
                    // Send an echo request.  A corresponding response should come back via the SimpitHost data listener
                    simpitHost.sendEchoRequest("rpi-simpit heartbeat");
                    Thread.sleep(Duration.ofSeconds(HEARTBEAT_FREQUENCY_SECONDS).toMillis());

                }
            }
        };
    }

}