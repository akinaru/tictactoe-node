package fr.bmartel.android.tictactoe.request;

import org.json.JSONException;
import org.json.JSONObject;

import fr.bmartel.android.tictactoe.GameMessageTopic;
import fr.bmartel.android.tictactoe.constant.RequestConstants;

/**
 * @author Bertrand Martel
 */
public class RequestBuilder {

    /**
     * Build a request for API : POST /connect
     *
     * @param deviceId
     * @return
     */
    public static JSONObject buildConnectionRequest(String deviceId) {

        JSONObject req = new JSONObject();
        try {
            req.put(RequestConstants.DEVICE_ID, deviceId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return req;
    }

    /**
     * Build a request for API : POST /username
     *
     * @param deviceId
     * @param username
     * @return
     */
    public static JSONObject buildSetUsernameRequest(String deviceId, String username) {

        JSONObject req = new JSONObject();
        try {
            req.put(RequestConstants.DEVICE_ID, deviceId);
            req.put(RequestConstants.DEVICE_NAME, username);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return req;
    }

    public static JSONObject buildChallengeRequest(String sourceDeviceId, String sourceDeviceName, String targetDeviceId) {

        JSONObject req = new JSONObject();
        try {
            req.put(RequestConstants.DEVICE_MESSAGE_TOPIC, GameMessageTopic.CHALLENGED.getValue());
            req.put(RequestConstants.DEVICE_MESSAGE_CHALLENGER_ID, sourceDeviceId);
            req.put(RequestConstants.DEVICE_MESSAGE_CHALLENGER_NAME, sourceDeviceName);
            req.put(RequestConstants.TARGET_DEVICE_ID, targetDeviceId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return req;
    }

    public static JSONObject buildChallengeResponse(String deviceId, String challengerName, String challengerId) {

        JSONObject req = new JSONObject();
        try {
            req.put(RequestConstants.DEVICE_MESSAGE_CHALLENGER_ID, deviceId);
            req.put(RequestConstants.TARGET_DEVICE_ID, challengerId);
            req.put(RequestConstants.DEVICE_MESSAGE_CHALLENGER_NAME, challengerName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return req;
    }

    public static JSONObject buildNextTurnRequest(String deviceId, int segmentNum, String challengerId) {

        JSONObject req = new JSONObject();
        try {
            req.put(RequestConstants.DEVICE_MESSAGE_CHALLENGER_ID, deviceId);
            req.put(RequestConstants.TARGET_DEVICE_ID, challengerId);
            req.put(RequestConstants.DEVICE_PLAY, segmentNum);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return req;
    }
}
