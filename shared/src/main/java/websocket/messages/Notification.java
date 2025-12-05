package websocket.messages;

public class Notification extends ServerMessage {
    private String notificationMessage;

    public Notification(String notificationMessage) {
        super(ServerMessageType.NOTIFICATION);
        this.notificationMessage = notificationMessage;
    }

    public String getNotificationMessage() {
        return notificationMessage;
    }
}
