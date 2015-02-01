package hackwestern.jogwithme;

import android.app.Application;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseCrashReporting;

public class JogWithMeApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Crash Reporting.
        ParseCrashReporting.enable(this);

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        // Add your initialization code here
        Parse.initialize(this, "OY6mLguDHv5dFzkR6v3BTb7eVD2X7mrPjLBWgwdF",
                "aGdh6ucb8E2BYn7nbw4vQNbp4hM45jrVXRdD2u8o");
    }
}
