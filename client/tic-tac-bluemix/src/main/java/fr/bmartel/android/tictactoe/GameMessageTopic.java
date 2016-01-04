package fr.bmartel.android.tictactoe;

/**
 * @author Bertrand Martel
 */
public enum GameMessageTopic {

    NONE(0),
    CHALLENGED(1),
    ACCEPTED(2),
    DECLINED(3),
    GAME_STOPPED(4),
    PLAY(5);

    private final int value;

    private GameMessageTopic(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static GameMessageTopic getTopic(int value) {

        switch (value) {
            case 0:
                return NONE;
            case 1:
                return CHALLENGED;
            case 2:
                return ACCEPTED;
            case 3:
                return DECLINED;
            case 4:
                return GAME_STOPPED;
            case 5:
                return PLAY;
        }
        return NONE;
    }
}
