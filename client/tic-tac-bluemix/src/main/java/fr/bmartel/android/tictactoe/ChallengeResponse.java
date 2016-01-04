package fr.bmartel.android.tictactoe;

/**
 * @author Bertrand Martel
 */
public class ChallengeResponse extends MessageObject {

    private String challengerId = "";

    private String challengerName = "";

    private boolean responseStatus = false;

    public ChallengeResponse(GameMessageTopic messageType, boolean status, String challengerId, String challengerName) {
        super(messageType);
        this.responseStatus = status;
        this.challengerId = challengerId;
        this.challengerName = challengerName;
    }

    public String getChallengerId() {
        return challengerId;
    }

    public String getChallengerName() {
        return challengerName;
    }

    public boolean isResponseStatus() {
        return responseStatus;
    }
}
