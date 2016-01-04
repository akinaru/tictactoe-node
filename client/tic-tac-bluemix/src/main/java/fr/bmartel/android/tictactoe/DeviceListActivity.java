package fr.bmartel.android.tictactoe;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import fr.bmartel.android.tictactoe.constant.BroadcastFilters;
import fr.bmartel.android.tictactoe.constant.RequestConstants;
import fr.bmartel.android.tictactoe.request.ResponseParser;

/**
 * @author Bertrand Martel
 */
public class DeviceListActivity extends Activity {

    private final static String TAG = DeviceListActivity.class.getSimpleName();

    private EditText usernameEditText = null;

    private ListView deviceListView = null;

    private DeviceAdapter deviceAdapter = null;

    private String deviceId = "";
    private String deviceName = "";

    private Dialog challengeDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_list_activity);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastFilters.EVENT_USERNAME);
        intentFilter.addAction(BroadcastFilters.EVENT_DEVICE_LIST);
        intentFilter.addAction(BroadcastFilters.EVENT_MESSAGE);

        registerReceiver(eventReceiver, intentFilter);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        //load device name from shared preference
        deviceName = sharedPreferences.getString(RequestConstants.DEVICE_NAME, RequestConstants.DEFAULT_USERNAME);
        deviceId = sharedPreferences.getString(RequestConstants.DEVICE_ID, "");

        usernameEditText = (EditText) findViewById(R.id.username_edittext);
        usernameEditText.setText(deviceName);

        final Button change_username = (Button) findViewById(R.id.change_username);

        //change username when button is clicked
        change_username.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                GameSingleton.getInstance(DeviceListActivity.this).changeUserName(usernameEditText.getText().toString());
            }
        });

        deviceListView = (ListView) findViewById(R.id.device_list);

        final ArrayList<DeviceItem> list = new ArrayList<>();

        deviceAdapter = new DeviceAdapter(DeviceListActivity.this,
                android.R.layout.simple_list_item_1, list);

        deviceListView.setAdapter(deviceAdapter);

        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final DeviceItem item = (DeviceItem) parent.getItemAtPosition(position);

                GameSingleton.getInstance(DeviceListActivity.this).challengeOpponent(item.getDeviceName(), item.getDeviceId());
            }
        });

        GameSingleton.getInstance(DeviceListActivity.this).requestDeviceList(deviceId);

        Button refresh = (Button) findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GameSingleton.getInstance(DeviceListActivity.this).requestDeviceList(deviceId);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (challengeDialog != null) {
            challengeDialog.cancel();
            challengeDialog.dismiss();
        }

        GameSingleton.getInstance(DeviceListActivity.this).onResume();
    }

    @Override
    protected void onPause() {
        GameSingleton.getInstance(DeviceListActivity.this).onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(eventReceiver);
    }

    private BroadcastReceiver eventReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (BroadcastFilters.EVENT_USERNAME.equals(action)) {

                Log.i(TAG, "event username broadcast event");

                final String username = ResponseParser.parseUsernameEvent(intent);

                if (!username.equals("")) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            usernameEditText.setText(username);
                        }
                    });
                }

            } else if (BroadcastFilters.EVENT_MESSAGE.equals(action)) {

                Log.i(TAG, "event message received");

                MessageObject message = ResponseParser.parseMessage(intent);

                if (message != null) {

                    if (message instanceof ChallengeMessage) {

                        final ChallengeMessage challengeMessage = (ChallengeMessage) message;

                        Log.i(TAG, "challenged by " + challengeMessage.getChallengerName() + " : " + challengeMessage.getChallengerId());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showChallengeDialog(challengeMessage.getChallengerName(), challengeMessage.getChallengerId());
                            }
                        });
                    } else if (message instanceof ChallengeResponse) {

                        final ChallengeResponse response = (ChallengeResponse) message;

                        if (response.isResponseStatus()) {
                            Toast.makeText(context, "challenged accepted by " + response.getChallengerName(), Toast.LENGTH_LONG).show();
                            //start game activity

                            if (challengeDialog != null) {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        challengeDialog.cancel();
                                        challengeDialog.dismiss();
                                    }
                                });
                            }

                            GameSingleton.getInstance(DeviceListActivity.this).setPlayerTurn(true);
                            GameSingleton.getInstance(DeviceListActivity.this).setFirstSign();
                            GameSingleton.getInstance(DeviceListActivity.this).setChallengerId(response.getChallengerId());

                            Intent intent2 = new Intent(DeviceListActivity.this, GameActivity.class);
                            startActivity(intent2);

                        } else {
                            Toast.makeText(context, "challenged declined by " + response.getChallengerName(), Toast.LENGTH_LONG).show();

                            if (challengeDialog != null) {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        challengeDialog.cancel();
                                        challengeDialog.dismiss();
                                    }
                                });
                            }
                        }
                    }
                }
            } else if (BroadcastFilters.EVENT_DEVICE_LIST.equals(action)) {

                Log.i(TAG, "event device list broadcast event");

                final List<DeviceItem> items = ResponseParser.parseDeviceList(intent, deviceId);

                Log.i(TAG, "items : " + items.size());

                runOnUiThread(new Runnable() {
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                    @Override
                    public void run() {

                        if (deviceAdapter != null) {
                            deviceAdapter.clear();
                            deviceAdapter.addAll(items);
                            deviceAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        }
    };

    private void showChallengeDialog(final String challengerName, final String challengerId) {

        challengeDialog = new Dialog(DeviceListActivity.this);

        challengeDialog.setContentView(R.layout.challenge_dialog);

        challengeDialog.setTitle("Challenged");

        Button accept_btn = (Button) challengeDialog.findViewById(R.id.accept_btn);

        Button decline_btn = (Button) challengeDialog.findViewById(R.id.decline_btn);

        accept_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //send accept, start game
                GameSingleton.getInstance(DeviceListActivity.this).acceptChallenge(challengerId, challengerName);
            }
        });

        decline_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //send decline, nothing to do more
                GameSingleton.getInstance(DeviceListActivity.this).declineChallenge(challengerId, challengerName);

                if (challengeDialog != null) {
                    challengeDialog.cancel();
                    challengeDialog.dismiss();
                }
            }
        });

        TextView challenge_message = (TextView) challengeDialog.findViewById(R.id.challenge_message);

        challenge_message.setText("You have been challenged by " + challengerName);

        challengeDialog.show();
    }
}
