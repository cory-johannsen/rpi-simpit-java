package cjohannsen.protocol;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.Assert.assertEquals;

public class PayloadTest {

    public static final double MAX_DELTA = 0.01;

    @Test
    public void altitudeMessage_from_passes() {
        final float sealevel = 123.456f;
        final float surface = 234.567f;
        byte[] bytes = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putFloat(sealevel).putFloat(surface).array();
        Payload.AltitudeMessage message = Payload.AltitudeMessage.from(bytes);
        assertEquals(sealevel, message.sealevel, MAX_DELTA);
        assertEquals(surface, message.surface, MAX_DELTA);
    }

    @Test
    public void altitudeMessage_equals_passes() {
        final float sealevel = 123.456f;
        final float surface = 234.567f;
        byte[] bytes = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putFloat(sealevel).putFloat(surface).array();
        Payload.AltitudeMessage messageA = Payload.AltitudeMessage.from(bytes);
        Payload.AltitudeMessage messageB = Payload.AltitudeMessage.from(bytes);
        assertEquals(messageA, messageB);
    }
}