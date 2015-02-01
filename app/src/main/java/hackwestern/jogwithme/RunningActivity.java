package hackwestern.jogwithme;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.getpebble.android.kit.PebbleKit.PebbleAckReceiver;
import com.getpebble.android.kit.PebbleKit.PebbleNackReceiver;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;
import java.util.UUID;


public class RunningActivity extends ActionBarActivity {
    protected static int encouragement = -1;
    protected static String runObjId = "";

    protected static String whichUser = "";

    public static int limit = 1;
    public static int pollTime = 5;
    TextView runningDuration;
    long startTime = 0;

    public static int minutes = 0;
    public static int seconds = 0;

    private final static UUID PEBBLE_APP_UUID =
            UUID.fromString("47ec6b04-dc7a-4de5-acdc-b5d1ce836359");
    private static final int KEY_DATA = 0;

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            seconds = (int)(millis / 1000);
            minutes = seconds / 60;
            seconds = seconds % 60;

            if (minutes >= limit && seconds > 0) {
                // stop the timer
                timerHandler.removeCallbacks(timerRunnable);
                killPebble();

                Intent openMainActivity =  new Intent(RunningActivity.this, AfterRunStatsActivity.class);
                startActivity(openMainActivity);
                finish();

                return;
            }

            if (seconds % pollTime == 0) {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Run");

                // Retrieve the object by id
                query.getInBackground(runObjId, new GetCallback<ParseObject>() {
                    public void done(ParseObject runObj, ParseException e) {
                        Log.d("Run", "Found our run");

                        // set data
                        if (encouragement >= 0 ) {
                            runObj.put("encouragement", encouragement);
                            encouragement = -1;
                        }
                    }
                });
            }

            runningDuration.setText(String.format("%d:%02d", minutes, seconds));

            PebbleDictionary data = new PebbleDictionary();
            // Add a key of 0, and a string value.
            data.addString(0, String.format("%s:%s", minutes, seconds));
            PebbleKit.sendDataToPebble(getApplicationContext(), PEBBLE_APP_UUID, data);

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

        doPebble();
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

    public void doPebble() {
        boolean connected = PebbleKit.isWatchConnected(getApplicationContext());
        Log.i(getLocalClassName(), "Pebble is " + (connected ? "connected" : "not connected"));
        if (connected) {
            // Launching my app
            PebbleKit.startAppOnPebble(getApplicationContext(), PEBBLE_APP_UUID);

            // Handler for Pebble to send data to Android. Will sendAckToPebble upon receipt to acknolwedge.
            final Handler handler = new Handler();
            PebbleKit.registerReceivedDataHandler(this,
                    new PebbleKit.PebbleDataReceiver(PEBBLE_APP_UUID) {

                @Override
                public void receiveData(final Context context, final int transactionId,
                                        final PebbleDictionary data) {
                    Log.i(getLocalClassName(), "Received value=" + data.getString(0) + " for key: 0");


                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            /* Update your UI here. */
                            // Send message to parse to other user
                        }
                    });
                    PebbleKit.sendAckToPebble(getApplicationContext(), transactionId);
                }

            });

            //
            PebbleKit.registerReceivedAckHandler(getApplicationContext(), new PebbleAckReceiver(PEBBLE_APP_UUID) {
                @Override
                public void receiveAck(Context context, int transactionId) {
                    Log.i(getLocalClassName(), "Received ack for transaction " + transactionId);
                }
            });

            PebbleKit.registerReceivedNackHandler(getApplicationContext(), new PebbleNackReceiver(PEBBLE_APP_UUID) {
                @Override
                public void receiveNack(Context context, int transactionId) {
                    Log.i(getLocalClassName(), "Received nack for transaction " + transactionId);
                }
            });
            /*
            // Receive messages on the Pebble from Android
            while(true) {
                try {
                    Thread.sleep(1000);
                }catch(Exception e) {
                    Log.i(getLocalClassName(), "EXCEPTION THROWN BY THREAD.SLEEP");
                }
                PebbleKit.sendDataToPebble(getApplicationContext(), PEBBLE_APP_UUID, data);
            }*/
        }
    }

    public void killPebble() {
        PebbleKit.startAppOnPebble(getApplicationContext(), PEBBLE_APP_UUID);
    }

    /* encouragements */
    public void encourageBoost(View v) {
        encouragement = 0;
    }

    public void encourageRetain(View v) {
        encouragement = 1;
    }

    public void encourageExcel(View v) {
        encouragement = 2;
    }

    public void doEncouragement() {
        switch (encouragement) {
            case 0:
                Toast.makeText(getApplicationContext(), "You can do it!", Toast.LENGTH_SHORT).show();
            case 1:
                Toast.makeText(getApplicationContext(), "Keep going!", Toast.LENGTH_SHORT).show();
            case 2:
                Toast.makeText(getApplicationContext(), "You\'re doing great!", Toast.LENGTH_SHORT).show();
        }
        encouragement = -1;
    }
}
