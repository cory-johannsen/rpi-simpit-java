package cjohannsen;

public class Util {

    public static final String hexString(final byte[] buffer) {
        StringBuilder sb = new StringBuilder();
        for (byte b : buffer) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }
}
