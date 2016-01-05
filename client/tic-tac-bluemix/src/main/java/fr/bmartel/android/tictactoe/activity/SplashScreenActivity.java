package fr.bmartel.android.tictactoe.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import fr.bmartel.android.tictactoe.GameSingleton;
import fr.bmartel.android.tictactoe.R;

/**
 * @author Bertrand Martel
 */
public class SplashScreenActivity extends Activity {

    private String TAG = SplashScreenActivity.class.getSimpleName();

    private static int SPLASH_TIME_OUT = 2000;

    private Handler handler = new Handler();

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(SplashScreenActivity.this, DeviceListActivity.class);
            startActivity(intent);
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GameSingleton.getInstance(getApplicationContext());
        setContentView(R.layout.splash_activity);
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume Splash");

        handler.postDelayed(runnable, SPLASH_TIME_OUT);

        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause Splash");

        handler.removeCallbacks(runnable);

        super.onPause();
    }
}
