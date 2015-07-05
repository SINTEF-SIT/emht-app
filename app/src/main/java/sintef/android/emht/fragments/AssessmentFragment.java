package sintef.android.emht.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import sintef.android.emht.R;
import sintef.android.emht.models.Alarm;

/**
 * Created by iver on 10/06/15.
 */
public class AssessmentFragment extends Fragment {

    private static final String ALARM_ID = "alarm_id";
    private long alarmId;
    private final String TAG = this.getClass().getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments().getLong(ALARM_ID) == 0L) return inflater.inflate(R.layout.fragment_empty, container, false);
        alarmId = getArguments().getLong(ALARM_ID);
        Log.w(TAG, "alarm id: " + alarmId);

        Alarm alarm = Alarm.findById(Alarm.class, alarmId);
        View assessmentView = inflater.inflate(R.layout.fragment_assessment, container, false);

        setNmiLogQuestion(assessmentView, alarm.getAssessment().getNmi().isConscious(), R.id.assessmentLogAnswer1);
        setNmiLogQuestion(assessmentView, alarm.getAssessment().getNmi().isBreathing(), R.id.assessmentLogAnswer2);
        setNmiLogQuestion(assessmentView, alarm.getAssessment().getNmi().isMovement(), R.id.assessmentLogAnswer3);
        setNmiLogQuestion(assessmentView, alarm.getAssessment().getNmi().isStanding(), R.id.assessmentLogAnswer4);
        setNmiLogQuestion(assessmentView, alarm.getAssessment().getNmi().isTalking(), R.id.assessmentLogAnswer5);

        if (alarm.getAssessment().isPatientInformationChecked()) ((TextView) assessmentView.findViewById(R.id.assessmentLogPatientInformationCheckedAnswer)).setText(getResources().getString(R.string.yes));
        if (alarm.getAssessment().isSensorsChecked()) ((TextView) assessmentView.findViewById(R.id.assessmentLogSensorsCheckedAnswer)).setText(getResources().getString(R.string.yes));

        ((TextView) assessmentView.findViewById(R.id.cautionLog)).setText(alarm.getPatient().getObs());

        return assessmentView;
    }

    private void setNmiLogQuestion(View view, Boolean answer, int id) {
        TextView textView = (TextView) view.findViewById(id);
        if (answer == null) return;
        if (answer) textView.setText(getResources().getString(R.string.yes));
        if (!answer) textView.setText(getResources().getString(R.string.no));

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getView() == null) return;
        LinearLayout chartView = (LinearLayout) getView().findViewById(R.id.sensors_chart);
        if (chartView == null) return;

        new SensorsChart(getActivity(), chartView);
    }
}
