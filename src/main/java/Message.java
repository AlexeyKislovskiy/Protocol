import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public abstract class Message {
    public static final int PROTOCOL_VERSION_ID = 1;

    protected byte[] data;
    protected int messageType;

    protected static void putVersionAndType(byte[] data, int version, int type) {
        putInt(data, 0, version);
        putInt(data, 4, type);
    }

    protected static void putInt(byte[] data, int current, int value) {
        byte[] valueBytes = ByteBuffer.allocate(4).putInt(value).array();
        System.arraycopy(valueBytes, 0, data, current, 4);
    }

    protected static void putIntArray(byte[] data, int current, int[] value) {
        for (int i = 0; i < value.length; i++) {
            putInt(data, current + i * 4, value[i]);
        }
    }

    protected static void putAnyArraysWithLength(byte[] data, int current, int[]... value) {
        int totalLength = 0;
        for (int[] array : value) {
            putInt(data, current + totalLength, array.length * 4);
            totalLength += 4;
            putIntArray(data, current + totalLength, array);
            totalLength += 4 * array.length;
        }
    }

    protected static int[] readArray(DataInputStream dis) throws MessageReadingException {
        try {
            int length = dis.readInt() / 4;
            if (length < 0) throw new MessageReadingException("Negative array length");
            int[] data = new int[length];
            for (int i = 0; i < length; i++) {
                data[i] = dis.readInt();
            }
            return data;
        } catch (IOException e) {
            throw new MessageReadingException("Can't read message", e);
        }
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    @Override
    public String toString() {
        return "RequestMassage{" +
                "data=" + Arrays.toString(data) +
                ", messageType=" + messageType +
                '}';
    }
}
