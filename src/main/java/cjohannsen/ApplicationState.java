package cjohannsen;

import cjohannsen.protocol.MessageType;
import cjohannsen.protocol.Payload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ApplicationState {
    private final Map<MessageType.Datagram, Payload> dataCache = new ConcurrentHashMap<>();

    @Autowired
    public ApplicationState() {
    }

    public Optional<Payload> getCachedDatagram(MessageType.Datagram type) {
        return Optional.ofNullable(dataCache.get(type));
    }

    public void setCachedDatagram(MessageType.Datagram type, Payload payload) {
        dataCache.put(type, payload);
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("Status:\r\n");
        dataCache.forEach((d,p) -> b.append(d.printableString() + " - " + p.toString() + "\r\n"));
        return b.toString();
    }
}
