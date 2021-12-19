package fertdt;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class Main {
    public static void main(String[] args) throws MessageReadingException {
        //пример создания реквест сообщения типа 4 и его чтения
        int[] x = new int[]{1, 2, 2, 1};
        int[] y = new int[]{1, 1, 2, 2};
        RequestMessage message = RequestMessage.createNormalMoveMessage(1, x, y);
        System.out.println(message);
        InputStream is = new ByteArrayInputStream(message.getData());
        System.out.println(RequestMessage.readMessage(is));
    }
}
