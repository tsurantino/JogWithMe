package hackwestern.jogwithme;

import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;


public class RunningActivity extends ActionBarActivity {
    protected static String runObjId = "";

    public static int limit = 1;
    public static int pollTime = 5;
    TextView runningDuration;
    long startTime = 0;

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int)(millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            if (minutes >= limit && seconds > 0) {
                // stop the timer
                timerHandler.removeCallbacks(timerRunnable);
                return;
            }

            if (seconds % pollTime == 0) {

            }

            runningDuration.setText(String.format("%d:%02d", minutes, seconds));
            timerHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            runObjId = extras.getString("runObjId");
        }

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Run");
        query.whereEqualTo("objectId", runObjId);

        query.findInBackground(new FindCallback<ParseObject>() {
               public void done(List<ParseObject> runList, ParseException e) {
                   if (e == null) {
                       if (runList.size() > 0) {
                            limit = Integer.parseInt(runList.get(0).get("duration").toString());
                       }
                   }
               }
           }
        );

        startTime = System.currentTimeMillis();
        runningDuration = (TextView) findViewById(R.id.runningDuration);
        timerHandler.postDelayed(timerRunnable, 0);
    }


    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item){
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
}
