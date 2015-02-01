package hackwestern.jogwithme;

import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;


public class CountdownActivity extends ActionBarActivity {
    protected static String readyObjId = "";
    TextView countdownText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown);

        // hack to hide action bar
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            readyObjId = extras.getString("readyObjId");
        }

        doCountdown();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static int count = 10;

    public void doCountdown() {
        if (count >= 1) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    TextView countdownText = (TextView)findViewById(R.id.countdownText);
                    count = count - 1;
                    countdownText.setText(String.valueOf(count));
                    doCountdown();
                }
            }, 1000);
        } else {
            Intent openMainActivity =  new Intent(CountdownActivity.this, RunningActivity.class);
            openMainActivity.putExtra("readyObjId", readyObjId);
            startActivity(openMainActivity);
            finish();
        }
    }
}
