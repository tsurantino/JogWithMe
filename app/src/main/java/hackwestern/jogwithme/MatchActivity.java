package hackwestern.jogwithme;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;


public class MatchActivity extends ActionBarActivity {
    public static String objId = "";
    public static String objDuration = "";
    public static String objDistance = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            loadLoginView();
        }

        Log.d("User logged in", currentUser.getUsername());

        setContentView(R.layout.activity_match);
    }

    private void loadLoginView() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
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

        switch (id) {
            case R.id.action_logout:
                ParseUser.logOut();
                loadLoginView();
                break;
            case R.id.action_settings:
                Log.d("Selection", "Selected Settings!");
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void createMatch(View v) {
        String distance = ((Spinner)findViewById(R.id.distance)).getSelectedItem().toString();
        String duration = ((EditText)findViewById(R.id.duration)).getText().toString();
        String climate = ((Spinner)findViewById(R.id.climate)).getSelectedItem().toString();
        boolean filterByNetwork = ((CheckBox)findViewById(R.id.filter_network)).isChecked();
        boolean filterByGender = ((CheckBox)findViewById(R.id.filter_gender)).isChecked();
        boolean filterByProximity = ((CheckBox)findViewById(R.id.filter_proximity)).isChecked();

        Log.d("Create Match", "Distance: " + distance);
        Log.d("Create Match", "Duration: " + duration);
        Log.d("Create Match", "Climate: " + climate);
        Log.d("Create Match", "Filter Network: " + filterByNetwork);
        Log.d("Create Match", "Filter Gender: " + filterByGender);
        Log.d("Create Match", "Filter Proximity: " + filterByProximity);

        final ParseObject newMatch = new ParseObject("Match");
        newMatch.put("owner", ParseUser.getCurrentUser().getUsername());
        newMatch.put("distance", distance);
        newMatch.put("duration", duration);
        newMatch.put("climate", climate);
        newMatch.put("filter_network", filterByNetwork);
        newMatch.put("filter_gender", filterByGender);
        newMatch.put("filter_proximity", filterByProximity);

        newMatch.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    // successful save
                    objId = newMatch.getObjectId();
                    objDistance = newMatch.get("distance").toString();
                    objDuration = newMatch.get("duration").toString();
                    beginMatching();
                }
            }
        });
    }

    public void beginMatching() {
        Intent openMainActivity =  new Intent(MatchActivity.this, MatchingActivity.class);
        openMainActivity.putExtra("matchObjId", objId);
        openMainActivity.putExtra("matchDistance", objDistance);
        openMainActivity.putExtra("matchDuration", objDuration);
        startActivity(openMainActivity);
        finish();
    }


}
