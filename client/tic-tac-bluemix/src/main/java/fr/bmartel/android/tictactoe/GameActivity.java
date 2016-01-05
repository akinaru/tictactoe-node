package fr.bmartel.android.tictactoe;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import fr.bmartel.android.tictactoe.constant.BroadcastFilters;
import fr.bmartel.android.tictactoe.request.ResponseParser;

/**
 * Game Activity
 *
 * @author Bertrand Martel
 */
public class GameActivity extends Activity {

    private static final String TAG = GameActivity.class.getSimpleName();

    private GameSingleton singleton = null;

    private int gameStatePlayer = 0;
    private int gameStateOpponent = 0;

    private TextView turn_message = null;

    private TextView score = null;

    private boolean isRestartTurn = false;

    private int gameWonCount = 0;
    private int gameLostCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity);

        singleton = GameSingleton.getInstance(this);

        Log.i(TAG, "GameActivity started");

        turn_message = (TextView) findViewById(R.id.turn_message);
        score = (TextView) findViewById(R.id.score);

        score.setText("You : " + gameWonCount + " / Opponent : " + gameLostCount);

        if (singleton.isPlayerTurn()) {

            turn_message.setText("Your turn");

        } else {

            turn_message.setText("Opponent turn ( " + GameSingleton.getInstance(GameActivity.this).getOpponentSign() + " )");

        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastFilters.EVENT_USERNAME);
        intentFilter.addAction(BroadcastFilters.EVENT_DEVICE_LIST);
        intentFilter.addAction(BroadcastFilters.EVENT_MESSAGE);

        registerReceiver(eventReceiver, intentFilter);
    }

    private BroadcastReceiver eventReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, Intent intent) {

            String action = intent.getAction();

            if (BroadcastFilters.EVENT_MESSAGE.equals(action)) {

                Log.i(TAG, "event message received");

                MessageObject message = ResponseParser.parseMessage(intent);

                if (message != null) {

                    if (message instanceof PlayRequest) {

                        final PlayRequest playRequest = (PlayRequest) message;

                        Log.i(TAG, "play by opponent : " + playRequest.getPlay() + ". Now this is your turn");

                        singleton.setPlayerTurn(true);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                final int resourceId = GameActivity.this.getResources().getIdentifier("pos" + playRequest.getPlay(), "id", GameActivity.this.getPackageName());
                                Button segment = (Button) findViewById(resourceId);
                                segment.setText(singleton.getOpponentSign());

                                int filter = 0x01 << (playRequest.getPlay() - 1);
                                gameStateOpponent = gameStateOpponent + filter;

                                Log.i(TAG, "new game state opponent" + gameStateOpponent);

                                isRestartTurn = true;

                                if (checkOpponentWin()) {
                                    turn_message.setText("You lost !");
                                    gameLostCount++;
                                    score.setText("You : " + gameWonCount + " / Opponent : " + gameLostCount);
                                    displayRestartDialog();
                                } else if (checkDraw()) {
                                    turn_message.setText("This is a draw ! ");
                                    displayRestartDialog();
                                } else {
                                    turn_message.setText("Your turn ( " + GameSingleton.getInstance(GameActivity.this).getSign() + " )");
                                }
                            }
                        });
                    }
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(eventReceiver);
    }

    public void segmentClick(View v) {

        String ressouceNme = getResources().getResourceEntryName(v.getId());

        final int segmentNum = Integer.parseInt(ressouceNme.substring(3));

        if (singleton.isPlayerTurn()) {

            int filter = 0x01 << (segmentNum - 1);

            int gameState = gameStatePlayer + gameStateOpponent;

            Log.i(TAG, "former game state " + gameState);

            Log.i(TAG, "set filter " + filter + " for " + (gameState & filter));

            if ((gameState & filter) == 0) {

                singleton.setPlayerTurn(false);

                gameStatePlayer = gameStatePlayer + filter;

                Log.i(TAG, "new game state palyer " + gameStatePlayer);

                isRestartTurn = false;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final int resourceId = GameActivity.this.getResources().getIdentifier("pos" + segmentNum, "id", GameActivity.this.getPackageName());
                        Button segment = (Button) findViewById(resourceId);
                        segment.setText(singleton.getSign());

                        if (checkPlayerWin()) {
                            turn_message.setText("You win !");
                            gameWonCount++;
                            score.setText("You : " + gameWonCount + " / Opponent : " + gameLostCount);
                            displayRestartDialog();
                        } else if (checkDraw()) {
                            turn_message.setText("This is a draw ! ");
                            displayRestartDialog();
                        } else {
                            turn_message.setText("Opponent turn ( " + GameSingleton.getInstance(GameActivity.this).getOpponentSign() + " )");
                        }
                    }
                });

                singleton.sendNextTurn(segmentNum, singleton.getChallengerId());
            } else {
                Log.i(TAG, "unavailable segment");
                Toast.makeText(GameActivity.this, "Unavailable", Toast.LENGTH_LONG).show();
            }

        } else {
            Log.i(TAG, "This is not your turn");
            Toast.makeText(GameActivity.this, "Wait for opponent to play", Toast.LENGTH_LONG).show();
        }
    }

    private void displayRestartDialog() {

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        gameStatePlayer = 0;
                        gameStateOpponent = 0;

                        for (int i = 1; i < 10; i++) {
                            final int resourceId = GameActivity.this.getResources().getIdentifier("pos" + i, "id", GameActivity.this.getPackageName());
                            Button segment = (Button) findViewById(resourceId);
                            segment.setText("");
                        }

                        if (isRestartTurn) {
                            turn_message.setText("Your turn ( " + GameSingleton.getInstance(GameActivity.this).getSign() + " )");
                        } else {
                            turn_message.setText("Opponent turn");
                        }

                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        finish();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
        builder.setMessage("Do you want to restart ?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    private boolean checkOpponentWin() {
        return checkWin(gameStateOpponent);
    }

    private boolean checkPlayerWin() {
        return checkWin(gameStatePlayer);
    }

    private boolean checkWin(int gameState) {

        if (checkMatch(gameState, 0b000000000111)) {
            Log.i(TAG, "case1");
            return true;
        } else if (checkMatch(gameState, 0b000000111000)) {
            Log.i(TAG, "case2");
            return true;
        } else if (checkMatch(gameState, 0b000111000000)) {
            Log.i(TAG, "case3");
            return true;
        } else if (checkMatch(gameState, 0b000001001001)) {
            Log.i(TAG, "case4");
            return true;
        } else if (checkMatch(gameState, 0b000010010010)) {
            Log.i(TAG, "case5");
            return true;
        } else if (checkMatch(gameState, 0b000100100100)) {
            Log.i(TAG, "case6");
            return true;
        } else if (checkMatch(gameState, 0b000100010001)) {
            Log.i(TAG, "case7");
            return true;
        } else if (checkMatch(gameState, 0b000001010100)) {
            Log.i(TAG, "case8");
            return true;
        }
        return false;
    }

    private boolean checkMatch(int state, int filter) {
        if (((state & filter) ^ filter) == 0) {
            return true;
        }
        return false;
    }

    private boolean checkDraw() {

        int gameState = gameStateOpponent + gameStatePlayer;

        if ((gameState & 0b000111111111) == 0b000111111111) {
            return true;
        }
        return false;
    }
}
