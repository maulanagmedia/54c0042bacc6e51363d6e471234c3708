package gmedia.net.id.restauranttakingorder;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashScreen extends AppCompatActivity {

    private static boolean splashLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        /*boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        if (!tabletSize) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }*/

        if (!splashLoaded) {

            int secondsDelayed = 2;
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    //startActivity(new Intent(SplashScreen.this, DaftarVideo.class));
                    startActivity(new Intent(SplashScreen.this, AccountActivity.class));
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }, secondsDelayed * 1000);

            //splashLoaded = true;
        }
        else {

            //Intent goToMainActivity = new Intent(SplashScreen.this, DaftarVideo.class);
            Intent goToMainActivity = new Intent(SplashScreen.this, AccountActivity.class);
            goToMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(goToMainActivity);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }
}
