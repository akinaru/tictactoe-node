package fr.bmartel.android.tictactoe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fr.bmartel.android.tictactoe.constant.BroadcastFilters;
import fr.bmartel.android.tictactoe.constant.RequestConstants;
import fr.bmartel.android.tictactoe.gcm.QuickstartPreferences;
import fr.bmartel.android.tictactoe.gcm.RegistrationIntentService;
import fr.bmartel.android.tictactoe.request.RequestBuilder;
import fr.bmartel.android.tictactoe.utils.RandomGen;

/**
 * @author Bertrand Martel
 */
public class GameSingleton {

    private static final String TAG = GameSingleton.class.getSimpleName();
    public static String DEVICE_ID = "";

    private static GameSingleton mInstance = null;

    private final static int DEVICE_ID_SIZE = 20;

    private String deviceName = "";

    private Context context = null;

    private RequestQueue queue = null;

    private ExecutorService executor = null;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private boolean playerTurn = false;
    private String sign = "X";

    private String challengerId = "";

    private GameSingleton(Context context) {

        this.context = context.getApplicationContext();
        this.executor = Executors.newFixedThreadPool(1);

        queue = Volley.newRequestQueue(context.getApplicationContext());

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        //load device id from shared preference
        DEVICE_ID = sharedPreferences.getString(RequestConstants.DEVICE_ID, "");
        deviceName = sharedPreferences.getString(RequestConstants.DEVICE_NAME, RequestConstants.DEFAULT_USERNAME);

        if (DEVICE_ID.equals("")) {
            //register deviceId in shared preference
            SharedPreferences.Editor editor = sharedPreferences.edit();
            DEVICE_ID = new RandomGen(DEVICE_ID_SIZE).nextString();
            editor.putString(RequestConstants.DEVICE_ID, DEVICE_ID);
            editor.commit();
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (BuildConfig.APP_ROUTE + "/connect", RequestBuilder.buildConnectionRequest(DEVICE_ID), new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "response received connect : " + response.toString());
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        error.printStackTrace();
                    }
                });
        jsObjRequest.setShouldCache(false);
        queue.add(jsObjRequest);

        Log.i(TAG, "device id " + DEVICE_ID + " initialized");

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(context, RegistrationIntentService.class);
            context.startService(intent);
        }
    }

    public static GameSingleton getInstance(Context context) {

        if (mInstance == null) {
            mInstance = new GameSingleton(context);
        }
        return mInstance;
    }

    public void clean() {
    }

    public void changeUserName(final String username) {

        Log.i(TAG, "setting username : " + RequestBuilder.buildSetUsernameRequest(DEVICE_ID, username));

        deviceName = username;

        JSONObject req = new JSONObject();
        try {
            req.put(RequestConstants.DEVICE_ID, DEVICE_ID);
            req.put(RequestConstants.DEVICE_NAME, username);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i(TAG, "length : " + req.toString().length());

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (BuildConfig.APP_ROUTE + "/username", req, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        Log.i(TAG, "response received username : " + response.toString());

                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(RequestConstants.DEVICE_NAME, deviceName);
                        editor.commit();

                        //broadcast username change
                        try {

                            JSONObject object = new JSONObject();
                            object.put(RequestConstants.DEVICE_NAME, deviceName);

                            ArrayList<String> eventItem = new ArrayList<>();
                            eventItem.add(object.toString());

                            broadcastUpdateStringList(BroadcastFilters.EVENT_USERNAME, eventItem);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        error.printStackTrace();
                    }
                });

        jsObjRequest.setShouldCache(false);
        queue.add(jsObjRequest);
    }

    public void requestDeviceList(String deviceId) {

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (BuildConfig.APP_ROUTE + "/devices", null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        Log.i(TAG, "response received devices : " + response.toString());

                        ArrayList<String> eventItem = new ArrayList<>();
                        eventItem.add(response.toString());

                        broadcastUpdateStringList(BroadcastFilters.EVENT_DEVICE_LIST, eventItem);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        error.printStackTrace();
                    }
                });
        jsObjRequest.setShouldCache(false);
        queue.add(jsObjRequest);
    }

    public void broadcastUpdateStringList(String action, ArrayList<String> valueList) {

        String valueName = "";
        final Intent intent = new Intent(action);
        intent.putStringArrayListExtra(valueName, valueList);
        context.sendBroadcast(intent);
    }

    public RequestQueue getQueue() {
        return queue;
    }

    public void challengeOpponent(final String deviceName, String deviceId) {

        Log.i(TAG, "send challenge request : " + RequestBuilder.buildChallengeRequest(DEVICE_ID, this.deviceName, deviceId));

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (BuildConfig.APP_ROUTE + "/challenge", RequestBuilder.buildChallengeRequest(DEVICE_ID, this.deviceName, deviceId), new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        Log.i(TAG, "response received challenge : " + response.toString());

                        Toast.makeText(context, "waiting for response of user " + deviceName, Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        error.printStackTrace();
                    }
                });
        jsObjRequest.setShouldCache(false);
        queue.add(jsObjRequest);

    }

    public void acceptChallenge(final String challengerId, String challengerName) {

        Log.i(TAG, "send accept challenge request : " + RequestBuilder.buildChallengeResponse(DEVICE_ID, challengerName, challengerId));

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (BuildConfig.APP_ROUTE + "/accept_challenge", RequestBuilder.buildChallengeResponse(DEVICE_ID, challengerName, challengerId), new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        Log.i(TAG, "response received accept challenge : " + response.toString());

                        // call to start game activity
                        playerTurn = false;
                        GameSingleton.this.challengerId = challengerId;
                        setSecondSign();
                        Intent intent2 = new Intent(context, GameActivity.class);
                        intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        context.startActivity(intent2);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        jsObjRequest.setShouldCache(false);
        queue.add(jsObjRequest);
    }

    public void declineChallenge(String challengerId, String challengerName) {

        Log.i(TAG, "send decline challenge request : " + RequestBuilder.buildChallengeResponse(DEVICE_ID, challengerName, challengerId));

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (BuildConfig.APP_ROUTE + "/decline_challenge", RequestBuilder.buildChallengeResponse(DEVICE_ID, challengerName, challengerId), new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        Log.i(TAG, "response received decline challenge : " + response.toString());
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        error.printStackTrace();
                    }
                });
        jsObjRequest.setShouldCache(false);
        queue.add(jsObjRequest);
    }

    private BroadcastReceiver mRegistrationBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.i(TAG, "registration status received");
        }
    };

    public void onResume() {
        LocalBroadcastManager.getInstance(context).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
    }

    public void onPause() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mRegistrationBroadcastReceiver);
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            Log.i(TAG, "This device is not supported.");
            return false;
        }
        return true;
    }

    public boolean isPlayerTurn() {
        return playerTurn;
    }

    public void setPlayerTurn(boolean turn) {
        playerTurn = turn;
    }

    public void sendNextTurn(int segmentNum, String challengerId) {

        Log.i(TAG, "send next turn request : " + RequestBuilder.buildNextTurnRequest(DEVICE_ID, segmentNum, challengerId));

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (BuildConfig.APP_ROUTE + "/play", RequestBuilder.buildNextTurnRequest(DEVICE_ID, segmentNum, challengerId), new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        Log.i(TAG, "response received next turn : " + response.toString());
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        error.printStackTrace();
                    }
                });
        jsObjRequest.setShouldCache(false);
        queue.add(jsObjRequest);
    }

    public String getSign() {
        return sign;
    }

    public void setFirstSign() {
        sign = "X";
    }

    public void setSecondSign() {
        sign = "O";
    }

    public String getChallengerId() {
        return challengerId;
    }

    public void setChallengerId(String challengerId) {
        this.challengerId = challengerId;
    }

    public String getOpponentSign() {
        if (sign.equals("X")) {
            return "O";
        }
        return "X";
    }
}
