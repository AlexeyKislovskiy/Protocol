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

    private int requestStatusCode;
    private byte player;


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
        byte[] data = new byte[8];
        putVersionAndType(data, PROTOCOL_VERSION_ID, GAME_STATE);
        return null;
    }

    public static ResponseMessage createFinishMessage(byte winner) {
        byte[] data = new byte[9];
        putVersionAndType(data, PROTOCOL_VERSION_ID, FINISH);
        data[8] = winner;
        return new ResponseMessage(data, FINISH, winner);
    }
}
