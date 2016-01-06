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
package fr.bmartel.android.tictactoe.request;

import org.json.JSONException;
import org.json.JSONObject;

import fr.bmartel.android.tictactoe.datamodel.GameMessageTopic;
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
    public static JSONObject buildConnectionRequest(String deviceId,String username) {

        JSONObject req = new JSONObject();
        try {
            req.put(RequestConstants.DEVICE_ID, deviceId);
            req.put(RequestConstants.DEVICE_NAME, username);
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
