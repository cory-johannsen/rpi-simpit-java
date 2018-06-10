package cjohannsen;

import java.util.Arrays;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

            System.out.println("Let's inspect the beans provided by Spring Boot:");

            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (String beanName : beanNames) {
                System.out.println(beanName);
            }

            System.out.println("*************************");
            System.out.println("Opening serial port - ");

            SerialPort comPort = SerialPort.getCommPorts()[0];
            System.out.println("Identified serial port " + comPort.getDescriptivePortName());
            System.out.println("Port description: " + comPort.getPortDescription());
            System.out.println("System comPort name: " + comPort.getSystemPortName());
            System.out.println("Baud rate: " + comPort.getBaudRate());
            System.out.println("Data bits: " + comPort.getNumDataBits());
            System.out.println("Stop bits " + comPort.getNumStopBits());
            System.out.println("Parity " + comPort.getParity());

            System.out.println("*************************");

            System.out.println("Opening serial port - ");
            final boolean isOpen = comPort.openPort();
            if (!isOpen) {
                System.out.println("Serial port failed to open. Exiting.");
                System.exit(-1);
            }
            System.out.println("Serial port open. Registering data listener - ");


            comPort.addDataListener(new SerialPortDataListener() {
                @Override
                public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }
                @Override
                public void serialEvent(SerialPortEvent event)
                {
                    String eventType = "UNKNOWN";
                    switch (event.getEventType()) {
                        case SerialPort.LISTENING_EVENT_DATA_AVAILABLE:
                            eventType = "DATA_AVAILABLE";
                            break;
                        case SerialPort.LISTENING_EVENT_DATA_RECEIVED:
                            eventType = "DATA_RECEIVED";
                            break;
                        case SerialPort.LISTENING_EVENT_DATA_WRITTEN:
                            eventType = "DATA_WRITTEN";
                            break;
                    }
                    System.out.println("New event of type: " + eventType);
                    if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                        return;
                    final int bytesAvailable = comPort.bytesAvailable();
                    System.out.println(bytesAvailable + " bytes available.");
                    final byte[] newData = new byte[bytesAvailable];
                    int numRead = comPort.readBytes(newData, newData.length);
                    System.out.println("Read " + numRead + " bytes.");
                    StringBuilder sb = new StringBuilder();
                    for (byte b : newData) {
                        sb.append(String.format("%02X ", b));
                    }
                    System.out.println(sb.toString());
                }
            });
            System.out.println("Data listener registered.  Running.");
        };
    }

}