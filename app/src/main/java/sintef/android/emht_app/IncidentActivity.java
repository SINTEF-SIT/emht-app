package sintef.android.emht_app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import sintef.android.emht_app.fragments.EmptyFragment;
import sintef.android.emht_app.fragments.IncidentFragment;

/**
 * Created by iver on 17/06/15.
 */
public class IncidentActivity extends FragmentActivity {

    private final String TAG = this.getClass().getSimpleName();
    private final static String ALARM_ID = "alarm_id";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incident);

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }
            // Create a new Fragment to be placed in the activity layout
            IncidentFragment incidentFragment = new IncidentFragment();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            incidentFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            if (getResources().getBoolean(R.bool.isTablet)) {
                EmptyFragment emptyFragment = new EmptyFragment();
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.incident_fragment, incidentFragment)
                        .add(R.id.empty_fragment, emptyFragment)
                        .commit();
            } else {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, incidentFragment)
                        .commit();
            }

        }
    }

    public void selectAlarm(long alarmId) {
        Log.w(TAG, "selecting alarm: " + alarmId);
        Intent dashboard = new Intent(this, DashboardActivity.class);
        dashboard.putExtra(ALARM_ID, alarmId);
        startActivity(dashboard);
        /*
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        //assessmentFragment = new AssessmentFragment();
        //assessmentFragment.setArguments(getIntent().getExtras());
        registrationFragment = RegistrationFragment.newInstance(alarmId);
        fragmentTransaction.replace(R.id.fragment_container, registrationFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        */
    }
}
