/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.bmartel.android.tictactoe.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

import fr.bmartel.android.tictactoe.ChallengeMessage;
import fr.bmartel.android.tictactoe.DeviceListActivity;
import fr.bmartel.android.tictactoe.GameMessageTopic;
import fr.bmartel.android.tictactoe.GameSingleton;
import fr.bmartel.android.tictactoe.R;
import fr.bmartel.android.tictactoe.constant.BroadcastFilters;
import fr.bmartel.android.tictactoe.constant.RequestConstants;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {

        String message = data.getString("message");

        if (from.startsWith("/topics/" + GameSingleton.DEVICE_ID)) {
            Log.d(TAG, "Message: " + message);

            try {

                JSONObject object = new JSONObject(message);

                ArrayList<String> eventItem = new ArrayList<>();
                eventItem.add(object.toString());

                broadcastUpdateStringList(BroadcastFilters.EVENT_MESSAGE, eventItem);

                if (!GameSingleton.activityForeground) {

                    if (object.has(RequestConstants.DEVICE_MESSAGE_TOPIC) && object.has(RequestConstants.DEVICE_MESSAGE_CHALLENGER_ID) && object.has(RequestConstants.DEVICE_MESSAGE_CHALLENGER_NAME)) {

                        GameMessageTopic topic = GameMessageTopic.getTopic(object.getInt(RequestConstants.DEVICE_MESSAGE_TOPIC));

                        ChallengeMessage challengeMessage = new ChallengeMessage(topic, object.getString(RequestConstants.DEVICE_MESSAGE_CHALLENGER_ID), object.getString(RequestConstants.DEVICE_MESSAGE_CHALLENGER_NAME));

                        Log.i(TAG, "challenged by " + challengeMessage.getChallengerName() + " : " + challengeMessage.getChallengerId());

                        Intent intent2 = new Intent(this, DeviceListActivity.class);
                        intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent2, PendingIntent.FLAG_ONE_SHOT);
                        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.mipmap.ic_launcher).setContentTitle("Fight!").setContentText("challenged by " + challengeMessage.getChallengerName()).setAutoCancel(true).setSound(defaultSoundUri).setContentIntent(pendingIntent);
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(new Random().nextInt(9999), notificationBuilder.build());

                        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                        boolean isScreenOn = pm.isScreenOn();
                        if (isScreenOn == false) {
                            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "MyLock");
                            wl.acquire(10000);
                            PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyCpuLock");
                            wl_cpu.acquire(10000);
                        }

                        GameSingleton.pendingChallengeMessage = challengeMessage;
                        GameSingleton.pendingChallenge = true;
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();

            }
        }
    }

    public void broadcastUpdateStringList(String action, ArrayList<String> valueList) {

        String valueName = "";
        final Intent intent = new Intent(action);
        intent.putStringArrayListExtra(valueName, valueList);
        Log.i(TAG, "broadcast message");
        sendBroadcast(intent);
    }

}
