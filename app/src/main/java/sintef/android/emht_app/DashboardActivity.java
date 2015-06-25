package sintef.android.emht_app;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.design.widget.TabLayout;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import sintef.android.emht_app.account.ServerSync;
import sintef.android.emht_app.fragments.ActionsFragment;
import sintef.android.emht_app.fragments.AssessmentFragment;
import sintef.android.emht_app.fragments.DrawerFragment;
import sintef.android.emht_app.fragments.IncidentFragment;
import sintef.android.emht_app.fragments.RegistrationFragment;
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
    private final String ALARM_ID = "alarm_id";
    private ServerSync mServerSync;
    private boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        if (getResources().getBoolean(R.bool.isTablet)) {
            RegistrationFragment registrationFragment = new RegistrationFragment();
            registrationFragment.setArguments(getIntent().getExtras());
            AssessmentFragment assessmentFragment = new AssessmentFragment();
            assessmentFragment.setArguments(getIntent().getExtras());
            ActionsFragment actionsFragment = new ActionsFragment();
            actionsFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.registration_fragment, registrationFragment)
                    .add(R.id.assessment_fragment, assessmentFragment)
                    .add(R.id.actions_fragment, actionsFragment)
                    .add(R.id.drawer_fragment, new DrawerFragment())
                    .commit();
        } else {
            // Get the ViewPager and set it's PagerAdapter so that it can display items
            viewPager = (ViewPager) findViewById(R.id.viewpager);
            viewPager.setOffscreenPageLimit(2); // hold all fragments (pages) in memory
            viewPager.setAdapter(new DashboardFragmentPagerAdapter(getSupportFragmentManager(),
                    DashboardActivity.this, getIntent().getExtras().getLong("alarm_id")));

            // Give the TabLayout the ViewPager
            TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
            tabLayout.setupWithViewPager(viewPager);

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.drawer_fragment, new DrawerFragment())
                    .commit();
        }

        //((EditText) getWindow().getDecorView().findViewById(R.id.notes)).setText(
        //        Alarm.findById(Alarm.class, getIntent().getExtras().getLong(ALARM_ID)).getNotes()
        //);
        Intent serverSync = new Intent(this, ServerSync.class);
        serverSync.putExtra("account_id", getIntent().getExtras().getInt("account_id"));
        serverSync.putExtra("auth_token_type", "dummytoken");
        startService(serverSync);
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ServerSync.LocalBinder binder = (ServerSync.LocalBinder) service;
            mServerSync = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to ServerSync
        Intent intent = new Intent(this, ServerSync.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void onBackPressed() {
        dismissDialog();
    }

    private void dismissDialog() {
        new DialogFragment() {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.dialog_dismiss)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });
                // Create the AlertDialog object and return it
                return builder.create();
            }
        }.show(getSupportFragmentManager(), "DismissDialogFragment");
    }

    public void onDismissIncidentButtonClicked(View view) {
        dismissDialog();
    }

    public void onCompleteIncidentButtonClicked(View view) {
        new DialogFragment() {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.dialog_approve)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                updateAlarmBeforeExit();
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });
                // Create the AlertDialog object and return it
                return builder.create();
            }
        }.show(getSupportFragmentManager(), "CompleteDialogFragment");
    }

    private void updateAlarmBeforeExit() {
        // gather entered data and send to server
        // check if data has changed or not
        Alarm alarm = Alarm.findById(Alarm.class, getIntent().getExtras().getLong(ALARM_ID));
        View view = getWindow().getDecorView();

        alarm.setNotes(((EditText)view.findViewById(R.id.notes)).getText().toString());

        alarm.save();
        // send json to some endpoint...

        if (mBound) mServerSync.transmitAlarm(alarm);
        else Log.w(TAG, "serversync not bound!");
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

    public void selectAlarm(long alarmId) {
        Log.w(TAG, "selecting alarm: " + alarmId);
        Intent dashboard = new Intent(this, DashboardActivity.class);
        dashboard.putExtra(ALARM_ID, alarmId);
        finish();
        startActivity(dashboard);
    }
}
