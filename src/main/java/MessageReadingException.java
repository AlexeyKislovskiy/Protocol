public class MessageReadingException extends Exception{
    public MessageReadingException() {
    }

    public MessageReadingException(String message) {
        super(message);
    }

    public MessageReadingException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageReadingException(Throwable cause) {
        super(cause);
    }
}
