package hackwestern.jogwithme;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

public class SplashActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*
         * Currently we redirect arbitrarily after 5 seconds
         * We'll change this with a check with Google authentication
         */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // hack to hide action bar
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent openMainActivity =  new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(openMainActivity);
                finish();
            }
        }, 5000);
    }
}
