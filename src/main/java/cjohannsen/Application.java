package cjohannsen;

import cjohannsen.protocol.MessageType;
import com.fazecast.jSerialComm.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import static cjohannsen.protocol.MessageType.Datagram.ALTITUDE_MESSAGE;
import static cjohannsen.protocol.MessageType.Datagram.ECHO_REQ_MESSAGE;

@SpringBootApplication
public class Application {
    static final Logger logger = LoggerFactory.getLogger(Application.class);

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

            SimpitHost simpitHost = ctx.getBean(SimpitHost.class);
            final boolean handshakeSuccess = simpitHost.handshake();
            if (!handshakeSuccess) {
                logger.error("Handshaking failed!");
            }
            else {
                logger.info("Handshaking success.");

                logger.info("Registering datagram handlers.");
                simpitHost.registerHandler(ECHO_REQ_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    logger.info("ECHO: " + String.valueOf(message));
                    return true;
                });
                simpitHost.registerHandler(ALTITUDE_MESSAGE, (MessageType.Datagram type, byte[] message) -> {
                    logger.info("ECHO: " + String.valueOf(message));
                    return true;
                });

                // Send an echo request.  A corresponding response should come back via the SimpitHost data listener
                logger.info("Initiating communications with an echo test.");
                while (true) {
                    simpitHost.sendEchoRequest("RPi!");
                    Thread.sleep(2500);
                }
            }
        };
    }

}