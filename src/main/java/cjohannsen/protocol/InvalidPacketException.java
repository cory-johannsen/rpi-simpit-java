package cjohannsen.protocol;

public class InvalidPacketException extends ProtocolException {
    public InvalidPacketException() {
        super();
    }

    public InvalidPacketException(String message) {
        super(message);
    }
}
