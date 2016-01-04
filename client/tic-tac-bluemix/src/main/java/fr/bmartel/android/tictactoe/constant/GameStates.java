package fr.bmartel.android.tictactoe.constant;

/**
 * @author Bertrand Martel
 */
public enum GameStates {

    NONE(0),
    CONNECTED(1),
    CHALLENGED(2),
    GAME_STARTING(3),
    PLAYING(4),
    GAME_ENDED(5);

    private int value = 0;

    private GameStates(int value) {
        this.value = value;
    }

    public static GameStates getState(int value) {

        switch (value) {
            case 0:
                return NONE;
            case 1:
                return CONNECTED;
            case 2:
                return CHALLENGED;
            case 3:
                return GAME_STARTING;
            case 4:
                return PLAYING;
            case 5:
                return GAME_ENDED;
        }
        return NONE;
    }
}
