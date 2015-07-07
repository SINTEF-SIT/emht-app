package sintef.android.emht.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import sintef.android.emht.R;
import sintef.android.emht.models.Alarm;

/**
 * Created by iver on 10/06/15.
 */
public class RegistrationFragment extends Fragment{

    private static final String ALARM_ID = "alarm_id";
    private long alarmId;
    private final String TAG = this.getClass().getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments().getLong(ALARM_ID) == 0L) return inflater.inflate(R.layout.fragment_empty, container, false);
        alarmId = getArguments().getLong(ALARM_ID);
        Log.w(TAG, "alarm id: " + alarmId);

        Alarm alarm = Alarm.findById(Alarm.class, alarmId);

        View registrationView = inflater.inflate(R.layout.fragment_registration, container, false);

        TextView calleeName = (TextView) registrationView.findViewById(R.id.calleeName);
        TextView calleeAddress = (TextView) registrationView.findViewById(R.id.calleeAddress);
        TextView calleePhoneNumber = (TextView) registrationView.findViewById(R.id.calleePhoneNumber);
        TextView patientName = (TextView) registrationView.findViewById(R.id.patientName);
        TextView patientAddress = (TextView) registrationView.findViewById(R.id.patientAddress);
        TextView patientPersonalNumber = (TextView) registrationView.findViewById(R.id.patientPersonalNumber);
        TextView patientPhoneNumber = (TextView) registrationView.findViewById(R.id.patientPhoneNumber);
        TextView patientAge = (TextView) registrationView.findViewById(R.id.patientAge);
        TextView patientIncidentLocation = (TextView) registrationView.findViewById(R.id.patientIncidentLocation);
        TextView patientLog = (TextView) registrationView.findViewById(R.id.patientLog);

        calleeName.setText(alarm.getCallee().getName());
        calleeAddress.setText(alarm.getCallee().getAddress());
        calleePhoneNumber.setText(alarm.getCallee().getPhoneNumber());
        if (alarm.getPatient() != null) {
            patientName.setText(alarm.getPatient().getName());
            patientAddress.setText(alarm.getPatient().getAddress());
            patientPersonalNumber.setText(alarm.getPatient().getPersonalNumber());
            patientPhoneNumber.setText(alarm.getPatient().getPhoneNumber());
            patientAge.setText(Integer.toString(alarm.getPatient().getAge()));
            patientIncidentLocation.setText(alarm.getOccuranceAddress());
            patientLog.setText(alarm.getPatient().getObs());
        }

        return registrationView;
    }

    public static RegistrationFragment newInstance(long alarmId) {
        RegistrationFragment fragment = new RegistrationFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ALARM_ID, alarmId);
        fragment.setArguments(bundle);
        return fragment;
    }
}
