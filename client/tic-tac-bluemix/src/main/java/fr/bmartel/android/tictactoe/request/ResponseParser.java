package fr.bmartel.android.tictactoe.request;

import android.content.Intent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fr.bmartel.android.tictactoe.datamodel.ChallengeMessage;
import fr.bmartel.android.tictactoe.datamodel.ChallengeResponse;
import fr.bmartel.android.tictactoe.datamodel.DeviceItem;
import fr.bmartel.android.tictactoe.datamodel.GameMessageTopic;
import fr.bmartel.android.tictactoe.datamodel.MessageObject;
import fr.bmartel.android.tictactoe.datamodel.PlayRequest;
import fr.bmartel.android.tictactoe.constant.GameStates;
import fr.bmartel.android.tictactoe.constant.RequestConstants;

/**
 * @author Bertrand Martel
 */
public class ResponseParser {

    public static String parseUsernameEvent(Intent intent) {

        ArrayList<String> actionsStr = intent.getStringArrayListExtra("");

        if (actionsStr.size() > 0) {
            try {
                JSONObject mainObject = new JSONObject(actionsStr.get(0));
                if (mainObject.has(RequestConstants.DEVICE_NAME)) {

                    return mainObject.get(RequestConstants.DEVICE_NAME).toString();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public static MessageObject parseMessage(Intent intent) {

        ArrayList<String> actionsStr = intent.getStringArrayListExtra("");

        if (actionsStr.size() > 0) {
            try {
                JSONObject mainObject = new JSONObject(actionsStr.get(0));
                if (mainObject.has(RequestConstants.DEVICE_MESSAGE_TOPIC)) {

                    GameMessageTopic topic = GameMessageTopic.getTopic(mainObject.getInt(RequestConstants.DEVICE_MESSAGE_TOPIC));

                    switch (topic) {

                        case CHALLENGED: {

                            if (mainObject.has(RequestConstants.DEVICE_MESSAGE_CHALLENGER_ID) && mainObject.has(RequestConstants.DEVICE_MESSAGE_CHALLENGER_NAME)) {
                                return new ChallengeMessage(topic, mainObject.getString(RequestConstants.DEVICE_MESSAGE_CHALLENGER_ID), mainObject.getString(RequestConstants.DEVICE_MESSAGE_CHALLENGER_NAME));
                            }
                            return null;
                        }
                        case ACCEPTED: {
                            if (mainObject.has(RequestConstants.DEVICE_MESSAGE_CHALLENGER_ID) && mainObject.has(RequestConstants.DEVICE_MESSAGE_CHALLENGER_NAME)) {
                                return new ChallengeResponse(topic, true, mainObject.getString(RequestConstants.DEVICE_MESSAGE_CHALLENGER_ID), mainObject.getString(RequestConstants.DEVICE_MESSAGE_CHALLENGER_NAME));
                            }
                            return null;
                        }
                        case DECLINED: {
                            if (mainObject.has(RequestConstants.DEVICE_MESSAGE_CHALLENGER_ID) && mainObject.has(RequestConstants.DEVICE_MESSAGE_CHALLENGER_NAME)) {
                                return new ChallengeResponse(topic, false, mainObject.getString(RequestConstants.DEVICE_MESSAGE_CHALLENGER_ID), mainObject.getString(RequestConstants.DEVICE_MESSAGE_CHALLENGER_NAME));
                            }
                            return null;
                        }
                        case GAME_STOPPED: {
                            break;
                        }
                        case PLAY: {
                            if (mainObject.has(RequestConstants.DEVICE_PLAY)) {
                                return new PlayRequest(topic, mainObject.getInt(RequestConstants.DEVICE_PLAY));
                            }
                            return null;
                        }
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static List<DeviceItem> parseDeviceList(Intent intent, String excludedDeviceId) {

        ArrayList<String> actionsStr = intent.getStringArrayListExtra("");

        List<DeviceItem> deviceList = new ArrayList<>();

        if (actionsStr.size() > 0) {
            try {
                JSONObject mainObject = new JSONObject(actionsStr.get(0));
                if (mainObject.has(RequestConstants.DEVICE_ITEMS)) {

                    JSONArray devices = mainObject.getJSONArray(RequestConstants.DEVICE_ITEMS);

                    for (int i = 0; i < devices.length(); i++) {

                        JSONObject item = (JSONObject) devices.get(i);

                        JSONObject docItem = item.getJSONObject("doc");

                        if (docItem.has("_id") && docItem.has(RequestConstants.DEVICE_NAME) && docItem.has(RequestConstants.DEVICE_STATE)) {

                            if (!docItem.getString("_id").equals(excludedDeviceId)) {

                                deviceList.add(new DeviceItem(docItem.getString("_id"),
                                        docItem.getString(RequestConstants.DEVICE_NAME),
                                        GameStates.getState(docItem.getInt(RequestConstants.DEVICE_STATE))));
                            }
                            /*
                            deviceList.add(new DeviceItem(docItem.getString("_id"),
                                    docItem.getString(RequestConstants.DEVICE_NAME),
                                    GameStates.getState(docItem.getInt(RequestConstants.DEVICE_STATE))));
                                    */
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return deviceList;
    }
}
