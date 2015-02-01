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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.plus.Plus;

import static android.location.LocationManager.GPS_PROVIDER;

public class RunningActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    protected static int received_encouragement = 0;
    protected static int sent_encouragement = 0;
    protected static String readyObjId = "";

    protected static String whichUser = "";

    public static int limit = 1;
    public static int pollTime = 5;
    TextView runningDuration;
    long startTime = 0;

    public static int minutes = 0;
    public static int total_seconds = 0;
    public static int seconds = 0;

    private final static UUID PEBBLE_APP_UUID =
            UUID.fromString("47ec6b04-dc7a-4de5-acdc-b5d1ce836359");
    private static final int KEY_DATA = 0;

    /*
        loc
     */
    private static final String TAG = "LocationServices";
    private static final String KEY_IN_RESOLUTION = "is_in_resolution";
    protected static final int REQUEST_CODE_RESOLUTION = 1;
    private GoogleApiClient mGoogleApiClient;
    private boolean mIsInResolution;
    private LocationController locationController;

    /*
     * sync
     */
    TextView ourPace;
    TextView ourDistance;

    TextView theirPace;
    TextView theirDistance;

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            total_seconds = (int)(millis / 1000);
            minutes = total_seconds / 60;
            seconds = total_seconds % 60;

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
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Ready");

                // Retrieve the object by id
                query.getInBackground(readyObjId, new GetCallback<ParseObject>() {
                    public void done(ParseObject runObj, ParseException e) {
                        Log.d("Run", "Found our ready");

                        String otherUser = "";
                        if (whichUser == "first") {
                            otherUser = "second";
                        } else {
                            otherUser = "first";
                        }

                        double myTempDist = locationController.getDistance();
                        double myTempPace = myTempDist * 1000 / total_seconds / 3600;

                        ourDistance = (TextView)findViewById(R.id.ourDistance);
                        ourPace = (TextView)findViewById(R.id.ourPace);

                        ourDistance.setText(String.format("%.2f m", myTempDist));
                        ourPace.setText(String.format("%.2f km/hr", myTempPace));

                        double theirTempDist = runObj.getNumber(otherUser + "_distance").doubleValue();
                        double theirTempPace = runObj.getNumber(otherUser + "_pace").doubleValue();

                        theirDistance = (TextView)findViewById(R.id.theirDistance);
                        theirPace = (TextView)findViewById(R.id.theirPace);

                        theirDistance.setText(String.format("%s m", theirTempDist));
                        theirPace.setText(String.format("%s km/hr", theirTempPace));

                        received_encouragement = runObj.getInt(otherUser + "_encouragement");
                        Log.d("Saving to Parse", "Encouragement: " + String.valueOf(received_encouragement));
                        doEncouragement();

                        // after the encouragement, it should be reset to 0...
                        received_encouragement = 0;
                        runObj.put(otherUser + "_encouragement", received_encouragement);
                        runObj.put(whichUser + "_encouragement", sent_encouragement);
                        runObj.put(whichUser + "_distance", myTempDist);
                        runObj.put(whichUser + "_pace", myTempPace);
                        runObj.saveInBackground();
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
            readyObjId = extras.getString("readyObjId");
            whichUser = extras.getString("whichUser");

        }

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Run");
        query.whereEqualTo("objectId", readyObjId);

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

        // loc
        if (savedInstanceState != null) {
            mIsInResolution = savedInstanceState.getBoolean(KEY_IN_RESOLUTION, false);
        }
        locationController = new LocationController(this,this);

        // sync

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
                    sent_encouragement = Integer.parseInt(data.getString(0));


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
        Log.d("Encouragement", "Retain!");
        sent_encouragement = 1;
    }

    public void encourageRetain(View v) {
        sent_encouragement = 2;
    }

    public void encourageExcel(View v) {
        sent_encouragement = 3;
    }

    public void doEncouragement() {
        switch (received_encouragement) {
            case 1:
                Toast.makeText(getApplicationContext(), "You can do it!", Toast.LENGTH_SHORT).show();
                break;
            case 2:
                Toast.makeText(getApplicationContext(), "Keep going!", Toast.LENGTH_SHORT).show();
                break;
            case 3:
                Toast.makeText(getApplicationContext(), "You\'re doing great!", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     * Called when the Activity is made visible.
     * A connection to Play Services need to be initiated as
     * soon as the activity is visible. Registers {@code ConnectionCallbacks}
     * and {@code OnConnectionFailedListener} on the
     * activities itself.
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Plus.API)
                    .addScope(Plus.SCOPE_PLUS_LOGIN)
                            // Optionally, add additional APIs and scopes if required.
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        mGoogleApiClient.connect();
//        locationManager.connect();
    }

    /**
     * Called when activity gets invisible. Connection to Play Services needs to
     * be disconnected as soon as an activity is invisible.
     */
    @Override
    protected void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
//        if(locationManager.isConnected()){
//            stopPeriodicUpdates();
//        }
//        locationManager.disconnect();
        super.onStop();
    }

    /**
     * Saves the resolution state.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IN_RESOLUTION, mIsInResolution);
    }

    /**
     * Handles Google Play Services resolution callbacks.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_RESOLUTION:
                retryConnecting();
                break;
        }
    }

    private void retryConnecting() {
        mIsInResolution = false;
        if (!mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    /**
     * Called when {@code mGoogleApiClient} is connected.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "GoogleApiClient connected");
        // TODO: Start making API requests.
        locationController.startRun();
    }

    /**
     * Called when {@code mGoogleApiClient} connection is suspended.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
        retryConnecting();
    }

    /**
     * Called when {@code mGoogleApiClient} is trying to connect but failed.
     * Handle {@code result.getResolution()} if there is a resolution
     * available.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // Show a localized error dialog.
            GooglePlayServicesUtil.getErrorDialog(
                    result.getErrorCode(), this, 0, new OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            retryConnecting();
                        }
                    }).show();
            return;
        }
        // If there is an existing resolution error being displayed or a resolution
        // activity has started before, do nothing and wait for resolution
        // progress to be completed.
        if (mIsInResolution) {
            return;
        }
        mIsInResolution = true;
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
            retryConnecting();
        }
    }
}
