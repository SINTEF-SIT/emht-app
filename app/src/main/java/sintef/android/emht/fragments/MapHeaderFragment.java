package sintef.android.emht.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.greenrobot.event.EventBus;
import sintef.android.emht.R;
import sintef.android.emht.events.NewAlarmEvent;
import sintef.android.emht.models.Alarm;
import sintef.android.emht.utils.Helper;

/**
 * Created by iver on 08/07/15.
 */
public class MapHeaderFragment extends Fragment {

    private EventBus mEventBus;
    private int numberOfAlarms;
    private TextView numberOfAlarmsTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mEventBus = EventBus.getDefault();
        mEventBus.register(this);
        numberOfAlarms = Helper.getAllUnfinishedAlarms().size();
        View mapHeaderView = inflater.inflate(R.layout.fragment_map_header, container, false);

        numberOfAlarmsTextView = (TextView) mapHeaderView.findViewById(R.id.number_of_alarms);
        numberOfAlarmsTextView.setText(getResources().getString(R.string.number_of_alarms) + " " + Integer.toString(numberOfAlarms));

        return mapHeaderView;
    }

    public void onEvent(NewAlarmEvent newAlarmEvent) {
        Log.w("MHF", "new alarm event");
        numberOfAlarms++;
        numberOfAlarmsTextView.setText(getResources().getString(R.string.number_of_alarms) + " " + Integer.toString(numberOfAlarms));
    }


}
