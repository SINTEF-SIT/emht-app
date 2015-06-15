package sintef.android.emht_app;

import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import sintef.android.emht_app.fragments.AssessmentFragment;
import sintef.android.emht_app.fragments.IncidentFragment;
import sintef.android.emht_app.models.Alarm;

/**
 * Created by iver on 10/06/15.
 */
public class DashboardActivity extends FragmentActivity {

    private final String TAG = this.getClass().getSimpleName();
    private IncidentFragment incidentFragment;
    private AssessmentFragment assessmentFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

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
            incidentFragment = new IncidentFragment();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            incidentFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, incidentFragment).commit();
        }
    }

    public void openAlarm(Alarm alarm) {
        Log.w(TAG, "opening alarm: " + alarm.getId());
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        assessmentFragment = new AssessmentFragment();
        assessmentFragment.setArguments(getIntent().getExtras());
        fragmentTransaction.replace(R.id.fragment_container, assessmentFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
