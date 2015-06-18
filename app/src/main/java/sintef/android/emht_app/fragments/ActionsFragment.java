package sintef.android.emht_app.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import sintef.android.emht_app.R;
import sintef.android.emht_app.models.Alarm;

/**
 * Created by iver on 10/06/15.
 */
public class ActionsFragment extends Fragment {


    private static final String ALARM_ID = "alarm_id";
    private long alarmId;
    private final String TAG = this.getClass().getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments().getLong(ALARM_ID) == 0L) return inflater.inflate(R.layout.fragment_empty, container, false);
        alarmId = getArguments().getLong(ALARM_ID);
        Log.w(TAG, "alarm id: " + alarmId);

        Alarm alarm = Alarm.findById(Alarm.class, alarmId);
        View actionsView = inflater.inflate(R.layout.fragment_actions, container, false);

        return actionsView;
    }



}
