/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2016 Bertrand Martel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fr.bmartel.android.tictactoe.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.UiModeManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import fr.bmartel.android.tictactoe.GameSingleton;
import fr.bmartel.android.tictactoe.R;
import fr.bmartel.android.tictactoe.adapter.DeviceAdapter;
import fr.bmartel.android.tictactoe.constant.BroadcastFilters;
import fr.bmartel.android.tictactoe.constant.GameStates;
import fr.bmartel.android.tictactoe.constant.RequestConstants;
import fr.bmartel.android.tictactoe.datamodel.ChallengeMessage;
import fr.bmartel.android.tictactoe.datamodel.ChallengeResponse;
import fr.bmartel.android.tictactoe.datamodel.ClientConnectionEvent;
import fr.bmartel.android.tictactoe.datamodel.DeviceItem;
import fr.bmartel.android.tictactoe.datamodel.MessageObject;
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

    private AlertDialog challengeDialog = null;

    private boolean init = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_list_activity);

        Log.i(TAG, "onCreate DeviceListActivity");

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

        usernameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(usernameEditText.getWindowToken(), 0);
                    GameSingleton.getInstance(DeviceListActivity.this).changeUserName(usernameEditText.getText().toString());

                    Toast.makeText(DeviceListActivity.this, "username changed", Toast.LENGTH_LONG).show();
                    return true;
                }
                return false;
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
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onResume() {
        super.onResume();
        if (challengeDialog != null) {
            challengeDialog.cancel();
            challengeDialog.dismiss();
        }
        GameSingleton.getInstance(DeviceListActivity.this).onResume();

        if (GameSingleton.pendingChallenge && GameSingleton.pendingChallengeMessage != null) {
            showChallengeDialog(GameSingleton.pendingChallengeMessage.getChallengerName(), GameSingleton.pendingChallengeMessage.getChallengerId());
        }

        UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
        if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
            EditText editTxt = (EditText) findViewById(R.id.username_edittext);
            editTxt.requestFocus();
        }
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
        Log.i(TAG, "onDestroy DeviceListActivity");
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
                    } else if (message instanceof ClientConnectionEvent) {

                        final ClientConnectionEvent event = (ClientConnectionEvent) message;

                        Log.i(TAG, "client has connected");

                        runOnUiThread(new Runnable() {
                            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                            @Override
                            public void run() {

                                if (deviceAdapter != null) {

                                    boolean found = false;

                                    for (int i = 0; i < deviceAdapter.getDeviceList().size(); i++) {
                                        if (deviceAdapter.getDeviceList().get(i).getDeviceId().equals(event.getDeviceId())) {
                                            deviceAdapter.getDeviceList().set(i, new DeviceItem(event.getDeviceId(), event.getDeviceName(), GameStates.NONE));
                                            found = true;
                                            break;
                                        }
                                    }

                                    if (!found) {
                                        deviceAdapter.add(new DeviceItem(event.getDeviceId(), event.getDeviceName(), GameStates.NONE));
                                    }
                                    deviceAdapter.notifyDataSetChanged();
                                }
                            }
                        });
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

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        GameSingleton.getInstance(DeviceListActivity.this).acceptChallenge(challengerId, challengerName);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        GameSingleton.getInstance(DeviceListActivity.this).declineChallenge(challengerId, challengerName);

                        if (challengeDialog != null) {
                            challengeDialog.cancel();
                            challengeDialog.dismiss();
                        }
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceListActivity.this);

        builder.setMessage("You have been challenged by " + challengerName + " ! ").setPositiveButton("accept", dialogClickListener)
                .setNegativeButton("decline", dialogClickListener).show();

    }
}
