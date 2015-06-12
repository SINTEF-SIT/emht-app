package sintef.android.emht_app.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import sintef.android.emht_app.R;
import sintef.android.emht_app.models.Alarm;

/**
 * Created by iver on 10/06/15.
 */
public class IncidentFragment extends Fragment {

    private EventBus mEventBus;
    private ArrayAdapter<Alarm> assignedAlarmsAdapter;
    private List<Alarm> assignedAlarms;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mEventBus.register(this);
        View incidentView = inflater.inflate(R.layout.fragment_incident, container, false);

        final ListView assignedAlarmsListView = (ListView) incidentView.findViewById(R.id.assignedAlarms);
        assignedAlarms = new ArrayList<Alarm>();
        assignedAlarmsAdapter = new ArrayAdapter<Alarm>(getActivity(), R.layout.listitem_alarm, assignedAlarms);
        assignedAlarmsListView.setAdapter(assignedAlarmsAdapter);

        return incidentView;
    }

    public void onEvent(Alarm alarm) {
        assignedAlarms.add(alarm);
        assignedAlarmsAdapter.notifyDataSetChanged();
    }
}
