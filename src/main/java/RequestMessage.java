import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class RequestMessage extends Message {
    public static final int START_REQUEST = 1;
    public static final int STOP_WAITING_START_REQUEST = 2;
    public static final int CHARACTERS_AND_SKILLS_SELECT = 3;
    public static final int NORMAL_MOVE = 4;
    public static final int NORMAL_SKILL = 5;
    public static final int SPECIAL_SKILL = 6;
    public static final int EXIT_REQUEST = 7;

    private int character, roomId;
    private int[] charactersMy, skills, xMy, yMy, xOpponent, yOpponent, charactersOpponent;

    private RequestMessage(byte[] data, int messageType) {
        this.data = data;
        this.messageType = messageType;
    }

    private RequestMessage(byte[] data, int messageType, int roomId) {
        this(data, messageType);
        this.roomId = roomId;
    }

    private RequestMessage(byte[] data, int messageType, int[] characters, int[] skills) {
        this(data, messageType);
        this.charactersMy = characters;
        this.skills = skills;
    }

    private RequestMessage(byte[] data, int messageType, int character, int[] x, int[] y) {
        this(data, messageType);
        this.character = character;
        this.xMy = x;
        this.yMy = y;
    }

    private RequestMessage(byte[] data, int messageType, int character, int[] xMy, int[] yMy, int[] xOpponent, int[] yOpponent, int[] charactersMy, int[] charactersOpponent) {
        this(data, messageType, character, xMy, yMy);
        this.xOpponent = xOpponent;
        this.yOpponent = yOpponent;
        this.charactersMy = charactersMy;
        this.charactersOpponent = charactersOpponent;
    }

    public static RequestMessage createStartRequestMessage(int roomId) {
        byte[] data = new byte[12];
        putVersionAndType(data, PROTOCOL_VERSION_ID, START_REQUEST);
        putInt(data, 8, roomId);
        return new RequestMessage(data, START_REQUEST, roomId);
    }

    public static RequestMessage createStopWaitingStartRequestMessage() {
        byte[] data = new byte[8];
        putVersionAndType(data, PROTOCOL_VERSION_ID, STOP_WAITING_START_REQUEST);
        return new RequestMessage(data, STOP_WAITING_START_REQUEST);
    }

    public static RequestMessage createCharacterAndSkillSelectMessage(int[] characters, int[] skills) {
        byte[] data = new byte[16 + (characters.length + skills.length) * 4];
        putVersionAndType(data, PROTOCOL_VERSION_ID, CHARACTERS_AND_SKILLS_SELECT);
        putAnyArraysWithLength(data, 8, characters, skills);
        return new RequestMessage(data, CHARACTERS_AND_SKILLS_SELECT, characters, skills);
    }

    public static RequestMessage createNormalMoveMessage(int character, int[] x, int[] y) {
        byte[] data = new byte[20 + (x.length + y.length) * 4];
        putVersionAndType(data, PROTOCOL_VERSION_ID, NORMAL_MOVE);
        putInt(data, 8, character);
        putAnyArraysWithLength(data, 12, x, y);
        return new RequestMessage(data, NORMAL_MOVE, character, x, y);
    }

    private static RequestMessage createSkillMessage(int messageType, int character, int[] xMy, int[] yMy, int[] xOpponent,
                                                     int[] yOpponent, int[] charactersMy, int[] charactersOpponent) {
        byte[] data = new byte[36 + (xMy.length + yMy.length + xOpponent.length + yOpponent.length + charactersMy.length
                + charactersOpponent.length) * 4];
        putVersionAndType(data, PROTOCOL_VERSION_ID, messageType);
        putInt(data, 8, character);
        putAnyArraysWithLength(data, 12, xMy, yMy, xOpponent, yOpponent, charactersMy, charactersOpponent);
        return new RequestMessage(data, messageType, character, xMy, yMy, xOpponent, yOpponent, charactersMy, charactersOpponent);
    }

    public static RequestMessage createNormalSkillMessage(int character, int[] xMy, int[] yMy, int[] xOpponent,
                                                          int[] yOpponent, int[] charactersMy, int[] charactersOpponent) {
        return createSkillMessage(NORMAL_SKILL, character, xMy, yMy, xOpponent, yOpponent, charactersMy, charactersOpponent);
    }

    public static RequestMessage createSpecialSkillMessage(int character, int[] xMy, int[] yMy, int[] xOpponent,
                                                           int[] yOpponent, int[] charactersMy, int[] charactersOpponent) {
        return createSkillMessage(SPECIAL_SKILL, character, xMy, yMy, xOpponent, yOpponent, charactersMy, charactersOpponent);
    }

    public static RequestMessage ExitRequestMessage() {
        byte[] data = new byte[8];
        putVersionAndType(data, PROTOCOL_VERSION_ID, EXIT_REQUEST);
        return new RequestMessage(data, EXIT_REQUEST);
    }

    public static RequestMessage readMessage(InputStream is) throws MessageReadingException {
        try (DataInputStream dis = new DataInputStream(is)) {
            int version = dis.readInt();
            if (version != PROTOCOL_VERSION_ID) throw new MessageReadingException("Incorrect protocol version");
            int messageType = dis.readInt();
            if (messageType == START_REQUEST) {
                int roomId = dis.readInt();
                return createStartRequestMessage(roomId);
            } else if (messageType == STOP_WAITING_START_REQUEST || messageType == EXIT_REQUEST) {
                byte[] data = new byte[8];
                putVersionAndType(data, version, messageType);
                return new RequestMessage(data, messageType);
            } else if (messageType == CHARACTERS_AND_SKILLS_SELECT) {
                int[] characters = readArray(dis), skills = readArray(dis);
                if (characters.length != skills.length) throw new MessageReadingException("Incorrect message");
                return createCharacterAndSkillSelectMessage(characters, skills);
            } else if (messageType == NORMAL_MOVE) {
                int character = dis.readInt();
                int[] x = readArray(dis), y = readArray(dis);
                if (x.length != y.length) throw new MessageReadingException("Incorrect message");
                return createNormalMoveMessage(character, x, y);
            } else if (messageType == NORMAL_SKILL || messageType == SPECIAL_SKILL) {
                int character = dis.readInt();
                int[] xMy = readArray(dis), yMy = readArray(dis), xOpponent = readArray(dis), yOpponent = readArray(dis),
                        charactersMy = readArray(dis), charactersOpponent = readArray(dis);
                if (xMy.length != yMy.length || xOpponent.length != yOpponent.length)
                    throw new MessageReadingException("Incorrect message");
                return createSkillMessage(messageType, character, xMy, yMy, xOpponent, yOpponent, charactersMy, charactersOpponent);
            } else throw new MessageReadingException("Incorrect message type");
        } catch (IOException e) {
            throw new MessageReadingException("Can't read message", e);
        }
    }

    public int getCharacter() {
        return character;
    }

    public void setCharacter(int character) {
        this.character = character;
    }

    public int[] getCharactersMy() {
        return charactersMy;
    }

    public void setCharactersMy(int[] charactersMy) {
        this.charactersMy = charactersMy;
    }

    public int[] getSkills() {
        return skills;
    }

    public void setSkills(int[] skills) {
        this.skills = skills;
    }

    public int[] getXMy() {
        return xMy;
    }

    public void setXMy(int[] xMy) {
        this.xMy = xMy;
    }

    public int[] getYMy() {
        return yMy;
    }

    public void setYMy(int[] yMy) {
        this.yMy = yMy;
    }

    public int[] getXOpponent() {
        return xOpponent;
    }

    public void setXOpponent(int[] xOpponent) {
        this.xOpponent = xOpponent;
    }

    public int[] getYOpponent() {
        return yOpponent;
    }

    public void setYOpponent(int[] yOpponent) {
        this.yOpponent = yOpponent;
    }

    public int[] getCharactersOpponent() {
        return charactersOpponent;
    }

    public void setCharactersOpponent(int[] charactersOpponent) {
        this.charactersOpponent = charactersOpponent;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    @Override
    public String toString() {
        return "RequestMessage{" +
                "data=" + Arrays.toString(data) +
                ", messageType=" + messageType +
                ", character=" + character +
                ", roomId=" + roomId +
                ", charactersMy=" + Arrays.toString(charactersMy) +
                ", skills=" + Arrays.toString(skills) +
                ", xMy=" + Arrays.toString(xMy) +
                ", yMy=" + Arrays.toString(yMy) +
                ", xOpponent=" + Arrays.toString(xOpponent) +
                ", yOpponent=" + Arrays.toString(yOpponent) +
                ", charactersOpponent=" + Arrays.toString(charactersOpponent) +
                '}';
    }
}
