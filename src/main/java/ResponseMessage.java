import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;

public class ResponseMessage extends Message {
    public static final int REQUEST_STATUS = 1;
    public static final int START = 2;
    public static final int START_TURN = 3;
    public static final int GAME_STATE = 4;
    public static final int FINISH = 5;


    public static final int OK = 1;
    public static final int INCORRECT_REQUEST = 2;

    public static final byte YOU = 1;
    public static final byte OPPONENT = 2;
    public static final byte DRAW = 3;

    private int requestStatusCode;
    private byte player;
    private int[][] blockStateMy, blockStateOpponent, x, y, charactersMy, charactersOpponent;
    private int[] pointsMy, pointsOpponent;


    private ResponseMessage(byte[] data, int messageType) {
        this.data = data;
        this.messageType = messageType;
    }

    private ResponseMessage(byte[] data, int messageType, int requestStatusCode) {
        this(data, messageType);
        this.requestStatusCode = requestStatusCode;
    }

    private ResponseMessage(byte[] data, int messageType, byte player) {
        this(data, messageType);
        this.player = player;
    }

    private ResponseMessage(byte[] data, int messageType, int[][] blockStateMy, int[][] blockStateOpponent, int[][] x,
                            int[][] y, int[][] charactersMy, int[][] charactersOpponent, int[] pointsMy, int[] pointsOpponent) {
        this(data, messageType);
        this.blockStateMy = blockStateMy;
        this.blockStateOpponent = blockStateOpponent;
        this.x = x;
        this.y = y;
        this.charactersMy = charactersMy;
        this.charactersOpponent = charactersOpponent;
        this.pointsMy = pointsMy;
        this.pointsOpponent = pointsOpponent;
    }

    public static ResponseMessage createRequestStatusMessage(int requestStatusCode) {
        byte[] data = new byte[12];
        putVersionAndType(data, PROTOCOL_VERSION_ID, REQUEST_STATUS);
        putInt(data, 8, requestStatusCode);
        return new ResponseMessage(data, REQUEST_STATUS, requestStatusCode);
    }

    public static ResponseMessage createStartMessage() {
        byte[] data = new byte[8];
        putVersionAndType(data, PROTOCOL_VERSION_ID, START);
        return new ResponseMessage(data, START);
    }

    public static ResponseMessage createStartTurnMessage(byte turn) {
        byte[] data = new byte[9];
        putVersionAndType(data, PROTOCOL_VERSION_ID, START_TURN);
        data[8] = turn;
        return new ResponseMessage(data, START_TURN, turn);
    }

    public static ResponseMessage createGameStateMessage(int[][] blockStateMy, int[][] blockStateOpponent, int[][] x,
                                                         int[][] y, int[][] charactersMy, int[][] charactersOpponent,
                                                         int[] pointsMy, int[] pointsOpponent) {
        byte[] data = new byte[16 + (pointsMy.length + pointsOpponent.length) * 4 + twoDimensionalArrayLength(blockStateMy) +
                twoDimensionalArrayLength(blockStateOpponent) + twoDimensionalArrayLength(x) + twoDimensionalArrayLength(y) +
                twoDimensionalArrayLength(charactersMy) + twoDimensionalArrayLength(charactersOpponent)];
        putVersionAndType(data, PROTOCOL_VERSION_ID, GAME_STATE);
        putAnyArraysWithLength(data, 8, pointsMy, pointsOpponent);
        putAnyTwoDimensionalArrays(data, 16 + (pointsMy.length + pointsOpponent.length) * 4, blockStateMy, blockStateOpponent,
                x, y, charactersMy, charactersOpponent);
        return new ResponseMessage(data, GAME_STATE, blockStateMy, blockStateOpponent, x, y, charactersMy, charactersOpponent, pointsMy, pointsOpponent);
    }

    public static ResponseMessage createFinishMessage(byte winner) {
        byte[] data = new byte[9];
        putVersionAndType(data, PROTOCOL_VERSION_ID, FINISH);
        data[8] = winner;
        return new ResponseMessage(data, FINISH, winner);
    }

    public static ResponseMessage readMessage(InputStream is) throws MessageReadingException {
        try (DataInputStream dis = new DataInputStream(is)) {
            int version = dis.readInt();
            if (version != PROTOCOL_VERSION_ID) throw new MessageReadingException("Incorrect protocol version");
            int messageType = dis.readInt();
            if (messageType == REQUEST_STATUS) {
                int status = dis.readInt();
                if (status != OK && status != INCORRECT_REQUEST)
                    throw new MessageReadingException("Incorrect request status");
                return createRequestStatusMessage(status);
            } else if (messageType == START) {
                byte[] data = new byte[8];
                putVersionAndType(data, version, messageType);
                return new ResponseMessage(data, messageType);
            } else if (messageType == START_TURN || messageType == FINISH) {
                byte[] data = new byte[9];
                putVersionAndType(data, version, messageType);
                byte b = dis.readByte();
                data[8] = b;
                if (!(b == YOU || b == OPPONENT || b == DRAW && messageType == FINISH))
                    throw new MessageReadingException("Incorrect value");
                return new ResponseMessage(data, messageType, b);
            } else if (messageType == GAME_STATE) {
                int[] pointsMy = readArray(dis), pointsOpponent = readArray(dis);
                if (pointsMy.length != pointsOpponent.length) throw new MessageReadingException("Incorrect value");
                int[][] blockStateMy = readTwoDimensionalArray(dis), blockStateOpponent = readTwoDimensionalArray(dis),
                        x = readTwoDimensionalArray(dis), y = readTwoDimensionalArray(dis), charactersMy = readTwoDimensionalArray(dis),
                        charactersOpponent = readTwoDimensionalArray(dis);
                if (x.length != y.length || blockStateMy.length != blockStateOpponent.length || charactersMy.length != charactersOpponent.length)
                    throw new MessageReadingException("Incorrect value");
                return createGameStateMessage(blockStateMy, blockStateOpponent, x, y, charactersMy, charactersOpponent, pointsMy, pointsOpponent);
            } else throw new MessageReadingException("Incorrect message type");
        } catch (IOException e) {
            throw new MessageReadingException("Can't read message", e);
        }
    }

    public int getRequestStatusCode() {
        return requestStatusCode;
    }

    public void setRequestStatusCode(int requestStatusCode) {
        this.requestStatusCode = requestStatusCode;
    }

    public byte getPlayer() {
        return player;
    }

    public void setPlayer(byte player) {
        this.player = player;
    }

    public int[][] getBlockStateMy() {
        return blockStateMy;
    }

    public void setBlockStateMy(int[][] blockStateMy) {
        this.blockStateMy = blockStateMy;
    }

    public int[][] getBlockStateOpponent() {
        return blockStateOpponent;
    }

    public void setBlockStateOpponent(int[][] blockStateOpponent) {
        this.blockStateOpponent = blockStateOpponent;
    }

    public int[][] getX() {
        return x;
    }

    public void setX(int[][] x) {
        this.x = x;
    }

    public int[][] getY() {
        return y;
    }

    public void setY(int[][] y) {
        this.y = y;
    }

    public int[][] getCharactersMy() {
        return charactersMy;
    }

    public void setCharactersMy(int[][] charactersMy) {
        this.charactersMy = charactersMy;
    }

    public int[][] getCharactersOpponent() {
        return charactersOpponent;
    }

    public void setCharactersOpponent(int[][] charactersOpponent) {
        this.charactersOpponent = charactersOpponent;
    }

    public int[] getPointsMy() {
        return pointsMy;
    }

    public void setPointsMy(int[] pointsMy) {
        this.pointsMy = pointsMy;
    }

    public int[] getPointsOpponent() {
        return pointsOpponent;
    }

    public void setPointsOpponent(int[] pointsOpponent) {
        this.pointsOpponent = pointsOpponent;
    }


    @Override
    public String toString() {
        return "ResponseMessage{" +
                "data=" + Arrays.toString(data) +
                ", messageType=" + messageType +
                ", requestStatusCode=" + requestStatusCode +
                ", player=" + player +
                ", blockStateMy=" + Arrays.deepToString(blockStateMy) +
                ", blockStateOpponent=" + Arrays.deepToString(blockStateOpponent) +
                ", x=" + Arrays.deepToString(x) +
                ", y=" + Arrays.deepToString(y) +
                ", charactersMy=" + Arrays.deepToString(charactersMy) +
                ", charactersOpponent=" + Arrays.deepToString(charactersOpponent) +
                ", pointsMy=" + Arrays.toString(pointsMy) +
                ", pointsOpponent=" + Arrays.toString(pointsOpponent) +
                '}';


    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResponseMessage that)) return false;
        return requestStatusCode == that.requestStatusCode && player == that.player && Arrays.deepEquals(blockStateMy,
                that.blockStateMy) && Arrays.deepEquals(blockStateOpponent, that.blockStateOpponent) && Arrays.deepEquals
                (x, that.x) && Arrays.deepEquals(y, that.y) && Arrays.deepEquals(charactersMy, that.charactersMy) &&
                Arrays.deepEquals(charactersOpponent, that.charactersOpponent) && Arrays.equals(pointsMy, that.pointsMy)
                && Arrays.equals(pointsOpponent, that.pointsOpponent);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(requestStatusCode, player);
        result = 31 * result + Arrays.deepHashCode(blockStateMy);
        result = 31 * result + Arrays.deepHashCode(blockStateOpponent);
        result = 31 * result + Arrays.deepHashCode(x);
        result = 31 * result + Arrays.deepHashCode(y);
        result = 31 * result + Arrays.deepHashCode(charactersMy);
        result = 31 * result + Arrays.deepHashCode(charactersOpponent);
        result = 31 * result + Arrays.hashCode(pointsMy);
        result = 31 * result + Arrays.hashCode(pointsOpponent);
        return result;
    }
}
