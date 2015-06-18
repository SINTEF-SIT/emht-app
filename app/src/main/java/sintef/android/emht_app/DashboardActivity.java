package sintef.android.emht_app;

import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.support.design.widget.TabLayout;
import android.view.View;
import android.widget.RadioButton;

import sintef.android.emht_app.fragments.AssessmentFragment;
import sintef.android.emht_app.fragments.IncidentFragment;
import sintef.android.emht_app.fragments.RegistrationFragment;
import sintef.android.emht_app.fragments.TabsFragment;
import sintef.android.emht_app.models.Alarm;

/**
 * Created by iver on 10/06/15.
 */
public class DashboardActivity extends FragmentActivity {

    private final String TAG = this.getClass().getSimpleName();
    private IncidentFragment incidentFragment;
    private AssessmentFragment assessmentFragment;
    private RegistrationFragment registrationFragment;
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new DashboardFragmentPagerAdapter(getSupportFragmentManager(),
                DashboardActivity.this, getIntent().getExtras().getLong("alarm_id")));

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radioAssessmentQuestion1Yes:
                if (checked)
                    // Pirates are the best
                    break;
            case R.id.radioAssessmentQuestion1No:
                if (checked)
                    // Ninjas rule
                    break;
            case R.id.radioAssessmentQuestion2Yes:
                if (checked)
                    // Pirates are the best
                    break;
            case R.id.radioAssessmentQuestion2No:
                if (checked)
                    // Ninjas rule
                    break;
            case R.id.radioAssessmentQuestion3Yes:
                if (checked)
                    // Pirates are the best
                    break;
            case R.id.radioAssessmentQuestion3No:
                if (checked)
                    // Ninjas rule
                    break;
            case R.id.radioAssessmentQuestion4Yes:
                if (checked)
                    // Pirates are the best
                    break;
            case R.id.radioAssessmentQuestion4No:
                if (checked)
                    // Ninjas rule
                    break;
            case R.id.radioAssessmentQuestion5Yes:
                if (checked)
                    // Pirates are the best
                    break;
            case R.id.radioAssessmentQuestion5No:
                if (checked)
                    // Ninjas rule
                    break;
        }
    }
}
