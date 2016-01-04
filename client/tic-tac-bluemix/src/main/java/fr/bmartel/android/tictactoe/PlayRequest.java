package fr.bmartel.android.tictactoe;

/**
 * @author Bertrand Martel
 */
public class PlayRequest extends MessageObject {

    private int play = 0;

    public PlayRequest(GameMessageTopic messageType, int play) {
        super(messageType);
        this.play = play;
    }

    public int getPlay() {
        return play;
    }
}
