package hackwestern.jogwithme;

import android.content.Intent;
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
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;


public class MatchingActivity extends ActionBarActivity {
    public static String objId = "";
    public static String objDuration = "";
    public static String objDistance = "";

    long startTime = 0;

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {

            checkReady();
            checkMatches();

            timerHandler.postDelayed(this, 3000);
        }
    };

    public void checkMatches() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Match");

        query.whereNotEqualTo("objectId", objId);
        query.whereEqualTo("duration", objDuration);
        query.whereEqualTo("distance", objDistance);

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> matchList, ParseException e) {
                if (e == null) {
                    if (matchList.size() > 0) {
                        final ParseObject newReadyObj = new ParseObject("Ready");
                        newReadyObj.put("firstUser", ParseUser.getCurrentUser().getUsername());
                        newReadyObj.put("secondUser", "");
                        newReadyObj.put("firstUserStatus", 0);
                        newReadyObj.put("secondUserStatus", 0);
                        newReadyObj.put("duration", objDuration);
                        newReadyObj.put("distance", objDistance);
                        newReadyObj.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    // successful save
                                    String rObjId = newReadyObj.getObjectId();
                                    goToReady(rObjId, "first");
                                }
                            }
                        });
                    }

                } else {
                    Log.d("Matching", "Error: " + e.getMessage());
                }
            }
        });
    }

    public void checkReady() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Ready");

        query.whereEqualTo("secondUser", "");
        query.whereEqualTo("duration", objDuration);
        query.whereEqualTo("distance", objDistance);

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> readyList, ParseException e) {
                if (e == null) {
                    /* I am ready second, therefore, I am the second user */
                    Log.d("Ready", "Retrieved " + readyList.size() + " ready");

                    if (readyList.size() > 0) {
                        final String readyObjId = readyList.get(0).getObjectId();

                        readyList.get(0).put("secondUser", ParseUser.getCurrentUser().getUsername());
                        readyList.get(0).saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    // successful save
                                    String rObjId = readyObjId;
                                    goToReady(rObjId, "second");
                                }
                            }
                        });
                    }
                } else {
                    Log.d("Ready", "Error: " + e.getMessage());
                }
            }
        });
    }

    public void goToReady(String readyObjId, String whichUser) {
        timerHandler.removeCallbacks(timerRunnable);

        Intent openReadyActivity =  new Intent(MatchingActivity.this,
                ReadyActivity.class);
        openReadyActivity.putExtra("readyObjId", readyObjId);
        openReadyActivity.putExtra("whichUser", whichUser);

        startActivity(openReadyActivity);
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matching);

        // hack to hide action bar
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            objId = extras.getString("matchObjId");
            objDuration = extras.getString("matchDuration");
            objDistance = extras.getString("matchDistance");
        }

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
}
