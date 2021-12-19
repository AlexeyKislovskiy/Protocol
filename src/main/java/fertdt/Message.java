package fertdt;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

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

    protected static void putTwoDimensionalArray(byte[] data, int current, int[][] value) {
        putInt(data, current, value.length);
        putAnyArraysWithLength(data, current + 4, value);
    }

    protected static void putAnyTwoDimensionalArrays(byte[] data, int current, int[][]... value) {
        int totalLength = 0;
        for (int[][] array : value) {
            putTwoDimensionalArray(data, current + totalLength, array);
            totalLength += twoDimensionalArrayLength(array);
        }
    }

    protected static int twoDimensionalArrayLength(int[][] array) {
        int totalLength = 4;
        for (int[] innerArray : array) {
            totalLength += (1 + innerArray.length) * 4;
        }
        return totalLength;
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

    protected static int[][] readTwoDimensionalArray(DataInputStream dis) throws MessageReadingException {
        try {
            int length = dis.readInt();
            if (length < 0) throw new MessageReadingException("Negative array length");
            int[][] data = new int[length][];
            for (int i = 0; i < length; i++) {
                data[i] = readArray(dis);
            }
            return data;
        } catch (IOException e) {
            throw new MessageReadingException("Can't read message", e);
        }
    }

    protected static InputStream listByteToInputStream(List<Byte> list){
        byte[] data=new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            data[i] = list.get(i);
        }
        return new ByteArrayInputStream(data);
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
