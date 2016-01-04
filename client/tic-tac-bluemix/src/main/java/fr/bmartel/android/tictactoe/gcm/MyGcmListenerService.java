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

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import fr.bmartel.android.tictactoe.GameSingleton;
import fr.bmartel.android.tictactoe.constant.BroadcastFilters;

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
