package cjohannsen.protocol;

public interface Handler {

    void handle(MessageType.Datagram type, byte[] message);
}
