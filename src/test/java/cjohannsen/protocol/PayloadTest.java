package cjohannsen.protocol;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class PayloadTest {

    public static final double MAX_DELTA = 0.01;

    @Test
    public void altitudeMessage_from_passes() {
        float_message_from_passes(Payload.AltitudeMessage::from, 123.456f, 234.567f,
                p -> ((Payload.AltitudeMessage) p).sealevel,
                p -> ((Payload.AltitudeMessage) p).surface);
    }

    @Test(expected = IllegalArgumentException.class)
    public void altitudeMessage_from_fails() {
        float_message_from_fails(Payload.AltitudeMessage::from);
    }

    @Test
    public void altitudeMessage_equals_fails() {
        float_equals_fails(Payload.AltitudeMessage::from);
    }

    @Test
    public void altitudeMessage_equals_passes() {
        float_equals_passes(Payload.AltitudeMessage::from);
    }

    @Test
    public void altitudeMessage_toString_passes() {
        float_toString_passes(Payload.AltitudeMessage::from,"Sea Level: 123.456 m, Surface: 234.567 m");
    }

    @Test
    public void apsidesMessage_from_passes() {
        float_message_from_passes(Payload.ApsidesMessage::from, 123.456f, 234.567f,
                p -> ((Payload.ApsidesMessage) p).periapsis,
                p -> ((Payload.ApsidesMessage) p).apoapsis);
    }

    @Test(expected = IllegalArgumentException.class)
    public void apsidesMessage_from_fails() {
        float_message_from_fails(Payload.ApsidesMessage::from);
    }

    @Test
    public void apsidesMessage_equals_fails() {
        float_equals_fails(Payload.ApsidesMessage::from);
    }

    @Test
    public void apsidesMessage_equals_passes() {
        float_equals_passes(Payload.ApsidesMessage::from);
    }

    @Test
    public void apsidesMessage_toString_passes() {
        float_toString_passes(Payload.AltitudeMessage::from,"Sea Level: 123.456 m, Surface: 234.567 m");
    }

    @Test
    public void apsidesTimeMessage_from_passes() {
        int_message_from_passes(Payload.ApsidesTimeMessage::from, 1, 2,
                p -> ((Payload.ApsidesTimeMessage) p).periapsis,
                p -> ((Payload.ApsidesTimeMessage) p).apoapsis);
    }

    @Test(expected = IllegalArgumentException.class)
    public void apsidesTimeMessage_from_fails() {
        int_message_from_fails(Payload.ApsidesMessage::from);
    }

    @Test
    public void apsidesTimeMessage_equals_fails() {
        int_equals_fails(Payload.ApsidesTimeMessage::from);
    }

    @Test
    public void apsidesTimeMessage_equals_passes() {
        int_equals_passes(Payload.ApsidesTimeMessage::from);
    }

    @Test
    public void apsidesTimeMessage_toString_passes() {
        int_toString_passes(Payload.ApsidesTimeMessage::from,"Periapsis: 1 s, Apoapsis: 2 s");
    }

    interface FloatProvider {
        float provide(Payload p);
    }

    interface IntProvider {
        int provide(Payload p);
    }

    private void float_message_from_passes(Payload.Provider provider, float f1, float f2, FloatProvider fp1, FloatProvider fp2) {
        byte[] bytes = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putFloat(f1).putFloat(f2).array();
        Payload p = provider.provide(bytes);
        assertEquals(f1, fp1.provide(p), MAX_DELTA);
        assertEquals(f2, fp2.provide(p), MAX_DELTA);
    }

    private void int_message_from_passes(Payload.Provider provider, int i1, int i2, IntProvider ip1, IntProvider ip2) {
        byte[] bytes = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(i1).putInt(i2).array();
        Payload p = provider.provide(bytes);
        assertEquals(i1, ip1.provide(p));
        assertEquals(i2, ip2.provide(p));
    }

    private void float_message_from_fails(Payload.Provider provider) {
        final float f = 123.456f;
        byte[] bytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(f).array();
        Payload message = provider.provide(bytes);
    }

    private void int_message_from_fails(Payload.Provider provider) {
        final int i = 1;
        byte[] bytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(i).array();
        Payload message = provider.provide(bytes);
    }

    private void float_equals_fails(Payload.Provider provider) {
        final float f1 = 123.456f;
        final float f2 = 234.567f;
        final float f3 = 345.678f;
        byte[] bytes = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putFloat(f1).putFloat(f2).array();
        byte[] bytes2 = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putFloat(f1).putFloat(f3).array();
        Payload p1 = provider.provide(bytes);
        Payload p2 = provider.provide(bytes2);
        assertNotEquals(p1, p2);
    }

    private void int_equals_fails(Payload.Provider provider) {
        final int i1 = 1;
        final int i2 = 2;
        final int i3 = 3;
        byte[] bytes = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(i1).putInt(i2).array();
        byte[] bytes2 = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(i1).putInt(i3).array();
        Payload p1 = provider.provide(bytes);
        Payload p2 = provider.provide(bytes2);
        assertNotEquals(p1, p2);
    }

    private void float_equals_passes(Payload.Provider provider) {
        final float f1 = 123.456f;
        final float f2 = 234.567f;
        byte[] bytes = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putFloat(f1).putFloat(f2).array();
        Payload p1 = provider.provide(bytes);
        Payload p2 = provider.provide(bytes);
        assertEquals(p1, p2);
    }

    private void int_equals_passes(Payload.Provider provider) {
        final int i1 = 1;
        final int i2 = 2;
        byte[] bytes = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(i1).putInt(i2).array();
        Payload p1 = provider.provide(bytes);
        Payload p2 = provider.provide(bytes);
        assertEquals(p1, p2);
    }

    private void float_toString_passes(Payload.Provider provider, String expected) {
        final float f1 = 123.456f;
        final float f2 = 234.567f;
        byte[] bytes = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putFloat(f1).putFloat(f2).array();
        Payload p = provider.provide(bytes);
        assertEquals(expected, p.toString());
    }

    private void int_toString_passes(Payload.Provider provider, String expected) {
        final int i1 = 1;
        final int i2 = 2;
        byte[] bytes = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(i1).putInt(i2).array();
        Payload p = Payload.ApsidesTimeMessage.from(bytes);
        assertEquals(expected, p.toString());
    }

}