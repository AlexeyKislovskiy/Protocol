import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class ProtocolTest {
    @Test
    public void requestCorrectNormalMoveMessageTest() {
        int length = (int) (Math.random() * 1000);
        int[] x = new int[length], y = new int[length];
        for (int i = 0; i < length; i++) {
            x[i] = (int) (Math.random() * 1000);
            y[i] = (int) (Math.random() * 1000);
        }
        int character = (int) (Math.random() * 1000);
        RequestMessage message1 = RequestMessage.createNormalMoveMessage(character, x, y);
        InputStream is = new ByteArrayInputStream(message1.getData());
        try {
            RequestMessage message2 = RequestMessage.readMessage(is);
            Assert.assertEquals(message1, message2);
        } catch (MessageReadingException ignored) {
        }
    }

    @Test
    public void requestIncorrectCharacterAndSkillSelectMessageTest() {
        int length = (int) (Math.random() * 1000);
        int[] characters = new int[length], skills = new int[length + 1];
        for (int i = 0; i < length; i++) {
            characters[i] = (int) (Math.random() * 1000);
            skills[i] = (int) (Math.random() * 1000);
        }
        skills[length - 1] = (int) (Math.random() * 1000);
        RequestMessage message1 = RequestMessage.createCharacterAndSkillSelectMessage(characters, skills);
        InputStream is = new ByteArrayInputStream(message1.getData());
        try {
            RequestMessage.readMessage(is);
            Assert.fail();
        } catch (MessageReadingException e) {
            Assert.assertEquals(e.getMessage(), "Incorrect message");
        }
    }

    @Test
    public void responseCorrectGameStateMessageTest() {
        int length = (int) (Math.random() * 1000), lengthI = (int) (Math.random() * 10);
        int[] pointsMy = new int[length], pointsOpponent = new int[length];
        int[][] x = new int[length][lengthI], y = new int[length][lengthI], blockStateMy = new int[length][lengthI],
                blockStateOpponent = new int[length][lengthI], charactersMy = new int[length][lengthI],
                charactersOpponent = new int[length][lengthI];
        for (int i = 0; i < length; i++) {
            pointsMy[i] = (int) (Math.random() * 1000);
            pointsOpponent[i] = (int) (Math.random() * 1000);
            for (int j = 0; j < lengthI; j++) {
                x[i][j] = (int) (Math.random() * 1000);
                y[i][j] = (int) (Math.random() * 1000);
                blockStateMy[i][j] = (int) (Math.random() * 1000);
                blockStateOpponent[i][j] = (int) (Math.random() * 1000);
                charactersMy[i][j] = (int) (Math.random() * 1000);
                charactersOpponent[i][j] = (int) (Math.random() * 1000);
            }
        }
        ResponseMessage message1 = ResponseMessage.createGameStateMessage(blockStateMy, blockStateOpponent, x, y,
                charactersMy, charactersOpponent, pointsMy, pointsOpponent);
        InputStream is = new ByteArrayInputStream(message1.getData());
        try {
            ResponseMessage message2 = ResponseMessage.readMessage(is);
            Assert.assertEquals(message1, message2);
        } catch (MessageReadingException ignored) {
        }
    }
}
