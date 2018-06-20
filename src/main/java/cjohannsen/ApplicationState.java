package cjohannsen;

import cjohannsen.protocol.MessageType;
import cjohannsen.protocol.Payload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class ApplicationState {
    private final Map<MessageType.Datagram, Payload> dataCache = new ConcurrentHashMap<>();

    private AtomicBoolean stageEnabled = new AtomicBoolean(false);
    private AtomicBoolean rcsEnabled = new AtomicBoolean(false);

    @Autowired
    public ApplicationState() {
    }

    public Optional<Payload> getCachedDatagram(MessageType.Datagram type) {
        return Optional.ofNullable(dataCache.get(type));
    }

    public void setCachedDatagram(MessageType.Datagram type, Payload payload) {
        dataCache.put(type, payload);
    }

    public boolean isStageEnabled() {
        return stageEnabled.get();
    }

    public void setStageEnabled(boolean stageEnabled) {
        this.stageEnabled.set(stageEnabled);
    }

    public boolean isRcsEnabled() {
        return rcsEnabled.get();
    }

    public void setRcsEnabled(boolean rcsEnabled) {
        this.rcsEnabled.set(rcsEnabled);
    }

    public String toString() {
        if (dataCache.isEmpty()) {
            return "Status: Uninitialized";
        }
        StringBuilder b = new StringBuilder();
        b.append("Status:\r\n");
        dataCache.forEach((d,p) -> b.append(d.printableString() + " - " + p.toString() + "\r\n"));
        return b.toString();
    }
}
