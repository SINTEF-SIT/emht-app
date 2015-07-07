package sintef.android.emht;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import de.greenrobot.event.EventBus;
import sintef.android.emht.account.BoundServiceListener;
import sintef.android.emht.events.SyncEvent;
import sintef.android.emht.fragments.ActionsFragment;
import sintef.android.emht.fragments.AssessmentFragment;
import sintef.android.emht.fragments.DrawerFragment;
import sintef.android.emht.fragments.IncidentFragment;
import sintef.android.emht.fragments.RegistrationFragment;
import sintef.android.emht.models.Alarm;
import sintef.android.emht.models.Assessment;
import sintef.android.emht.models.NMI;
import sintef.android.emht.sync.ServerSync;

/**
 * Created by iver on 10/06/15.
 */
public class DashboardActivity extends FragmentActivity {

    private final String TAG = this.getClass().getSimpleName();
    private IncidentFragment incidentFragment;
    private static AssessmentFragment assessmentFragment;
    private RegistrationFragment registrationFragment;
    ViewPager viewPager;
    private final String ALARM_ID = "alarm_id";
    private ServerSync mServerSync;
    private boolean mBound = false;
    private ActionBarDrawerToggle mDrawerToggle;
    private SlidingUpPanelLayout slidingUpPanelLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        if (getResources().getBoolean(R.bool.isTablet)) {
            RegistrationFragment registrationFragment = new RegistrationFragment();
            registrationFragment.setArguments(getIntent().getExtras());
            assessmentFragment = new AssessmentFragment();
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

        slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        findViewById(R.id.sliding_layout).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.w(TAG, "on touch");
                if (slidingUpPanelLayout != null &&
                        (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
                    slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                }
                return false;
            }
        });

        ((EditText) getWindow().getDecorView().findViewById(R.id.notes)).setText(
                Alarm.findById(Alarm.class, getIntent().getExtras().getLong(ALARM_ID)).getNotes()
        );
//        Intent serverSync = new Intent(this, ServerSync.class);
//        serverSync.putExtra("account_id", getIntent().getExtras().getInt("account_id"));
//        serverSync.putExtra("auth_token_type", "dummytoken");
//        startService(serverSync);
    }

    public static void setAssessmentFragment(AssessmentFragment fragment) {
        assessmentFragment = fragment;
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ServerSync.LocalBinder binder = (ServerSync.LocalBinder) service;
            mServerSync = binder.getService();
            mBound = true;

            binder.setListener(new BoundServiceListener() {
                @Override
                public void showGooglePlayServicesErrorDialog(int errorCode) {
                    buildGooglePlayServicesErrorDialog(errorCode);
                }

                @Override
                public void showAlarmTransmitComplete() {
                    // show toast with message?
                }

                @Override
                public void updateSensors() {
                    //assessmentFragment.updateSensors();
                }
            });

            //if (getIntent().getExtras().getLong(ALARM_ID) != 0L) mServerSync.updateSensors(getIntent().getExtras().getLong(ALARM_ID));
            if (getIntent().getExtras().getLong(ALARM_ID) != 0L) mServerSync.updateSensors(6L);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private void buildGooglePlayServicesErrorDialog(int errorCode) {
        if (GooglePlayServicesUtil.isUserRecoverableError(errorCode)) {
            GooglePlayServicesUtil.getErrorDialog(errorCode, this, 9000, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    DashboardActivity.this.finish();
                }
            }).show();
        }
    }


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
    protected void onResume() {
        super.onResume();
        if (mBound) mServerSync.startSensorPolling();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBound) mServerSync.stopSensorPolling();
    }

    @Override
    public void onBackPressed() {
        if (slidingUpPanelLayout != null &&
                (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
        }
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

        alarm.setNotes(((EditText) view.findViewById(R.id.notes)).getText().toString());

        Assessment fieldAssessment = new Assessment();
        NMI fieldNmi = new NMI();

        fieldNmi.setConscious(getRadioGroupAnswer(view, R.id.radioAssessmentQuestion1Yes, R.id.radioAssessmentQuestion1No));
        fieldNmi.setBreathing(getRadioGroupAnswer(view, R.id.radioAssessmentQuestion2Yes, R.id.radioAssessmentQuestion2No));
        fieldNmi.setMovement(getRadioGroupAnswer(view, R.id.radioAssessmentQuestion3Yes, R.id.radioAssessmentQuestion3No));
        fieldNmi.setStanding(getRadioGroupAnswer(view, R.id.radioAssessmentQuestion4Yes, R.id.radioAssessmentQuestion4No));
        fieldNmi.setTalking(getRadioGroupAnswer(view, R.id.radioAssessmentQuestion5Yes, R.id.radioAssessmentQuestion5No));
        fieldAssessment.setPatientInformationChecked(true);
        fieldAssessment.setSensorsChecked(true);

        fieldNmi.save();
        fieldAssessment.setNmi(fieldNmi);
        fieldAssessment.save();
        alarm.setFieldAssessment(fieldAssessment);
        alarm.save();
        alarm.setActive(false);
        alarm.setAddToUploadQueue(true);
        alarm.save();
        EventBus.getDefault().post(new SyncEvent());
    }

    private Boolean getRadioGroupAnswer(View view, int yesId, int noId) {
        Boolean result;
        if (((RadioButton) view.findViewById(yesId)).isChecked()) return true;
        if (((RadioButton) view.findViewById(noId)).isChecked()) return false;
        return null;
    }

    public void selectAlarm(long alarmId) {
        Log.w(TAG, "selecting alarm: " + alarmId);
        Intent dashboard = new Intent(this, DashboardActivity.class);
        dashboard.putExtra(ALARM_ID, alarmId);
        finish();
        startActivity(dashboard);
    }

}
