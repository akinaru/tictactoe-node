package fr.bmartel.android.tictactoe;

/**
 * @author Bertrand Martel
 */
public abstract class MessageObject {

    private GameMessageTopic messageType = GameMessageTopic.NONE;

    public MessageObject(GameMessageTopic messageType) {
        this.messageType = messageType;
    }

    public GameMessageTopic getMessageType() {
        return messageType;
    }
}
