package cjohannsen.protocol;

public interface Handler {

    boolean handle(MessageType.Datagram type, byte[] message);
}
