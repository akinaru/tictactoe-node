package fr.bmartel.android.tictactoe;

/**
 * @author Bertrand Martel
 */
public class ChallengeMessage extends MessageObject {

    private String challengerId = "";

    private String challengerName = "";

    public ChallengeMessage(GameMessageTopic messageType, String challengerId, String challengerName) {
        super(messageType);
        this.challengerId = challengerId;
        this.challengerName = challengerName;
    }

    public String getChallengerId() {
        return challengerId;
    }

    public String getChallengerName() {
        return challengerName;
    }
}
