package hackwestern.jogwithme;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;


public class ReadyActivity extends ActionBarActivity {

    protected static String runObjId = "";
    protected static String readyObjId = "";
    protected static String whichUser = "";
    protected static boolean ready = false;

    long startTime = 0;

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {

            checkIfBothReady();

            timerHandler.postDelayed(this, 3000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ready);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            readyObjId = extras.getString("readyObjId");
            whichUser = extras.getString("whichUser");
        }

        Log.d("Room", "Joined room: " + readyObjId);

        startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);
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

    public void setMyReady(View v) {
        if (ready == false) {
            ready = true;

            Button myReadyButton = (Button)findViewById(R.id.myReady);
            myReadyButton.setBackgroundColor(Color.parseColor("#669900"));
            myReadyButton.setText("READY");
        } else {
            ready = false;

            Button myReadyButton = (Button)findViewById(R.id.myReady);
            myReadyButton.setBackgroundColor(Color.parseColor("#B20000"));
            myReadyButton.setText("NOT READY");
        }

        // update Parse
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Ready");

        Log.d("Ready", "Changing user status");
        Log.d("Ready", "We are: " + whichUser);

        // Retrieve the object by id
        query.getInBackground(readyObjId, new GetCallback<ParseObject>() {
            public void done(ParseObject readyObj, ParseException e) {
                Log.d("Ready", "Found our room");

                if (e == null) {
                    Log.d("Ready", "Updating status");

                    if (whichUser.equals("first")) {
                        readyObj.put("firstUserStatus", ready);
                        readyObj.saveInBackground();
                    } else {
                        readyObj.put("secondUserStatus", ready);
                        readyObj.saveInBackground();
                    }
                }
            }
        });
    }

    public void checkIfBothReady() {
        Log.d("Ready", "Checking if both ready");
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Ready");
        query.whereEqualTo("objectId", readyObjId);

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> readyList, ParseException e) {
                if (e == null) {
                    if (readyList.size() > 0) {
                        Log.d("Ready", "Found our room");
                        boolean bothReady = false;
                        ParseObject readyObj = readyList.get(0);

                        if (whichUser.equals("first")) {
                            Log.d("Ready", "Updating other user, who is second user");
                            boolean isOtherReady = (boolean) readyObj.get("secondUserStatus");

                            Log.d("Ready", "Their status: " + isOtherReady);

                            updateOtherUser(isOtherReady);

                            if (ready && isOtherReady) {
                                bothReady = true;
                            }

                        } else {
                            Log.d("Ready", "Updating other user, who is first user");
                            boolean isOtherReady = (boolean) readyObj.get("firstUserStatus");

                            Log.d("Ready", "Their status: " + isOtherReady);

                            updateOtherUser(isOtherReady);

                            if (ready && isOtherReady) {
                                bothReady = true;
                            }
                        }

                        if (bothReady) {
                            final ParseObject newRunObj = new ParseObject("Run");
                            newRunObj.put("firstUser", readyObj.get("firstUser").toString());
                            newRunObj.put("secondUser", readyObj.get("secondUser").toString());
                            newRunObj.put("duration", readyObj.get("duration").toString());
                            newRunObj.put("firstUserPace", "");
                            newRunObj.put("firstUserDistance", "");
                            newRunObj.put("secondUserPace", "");
                            newRunObj.put("secondUserDistance", "");

                            newRunObj.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        // successful save
                                        runObjId = newRunObj.getObjectId();

                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                Intent openMainActivity = new Intent(
                                                        ReadyActivity.this,
                                                        CountdownActivity.class);

                                                openMainActivity.putExtra("runObjId", runObjId);

                                                startActivity(openMainActivity);
                                                finish();
                                            }
                                        }, 1500);
                                    }
                                }
                            });


                        }
                    }
                } else {
                    Log.d("Ready", "Error: " + e.getMessage());
                }
            }
        });
    }

    public void updateOtherUser(boolean isReady) {
        if (isReady) {
            Button myReadyButton = (Button)findViewById(R.id.theirReady);
            myReadyButton.setBackgroundColor(Color.parseColor("#669900"));
            myReadyButton.setText("READY");
        } else {
            Button myReadyButton = (Button)findViewById(R.id.theirReady);
            myReadyButton.setBackgroundColor(Color.parseColor("#B20000"));
            myReadyButton.setText("NOT READY");
        }
    }
}
