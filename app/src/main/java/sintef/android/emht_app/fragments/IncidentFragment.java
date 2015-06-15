package sintef.android.emht_app.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import sintef.android.emht_app.AlarmAdapter;
import sintef.android.emht_app.DashboardActivity;
import sintef.android.emht_app.R;
import sintef.android.emht_app.models.Alarm;

/**
 * Created by iver on 10/06/15.
 */
public class IncidentFragment extends Fragment {

    private final String TAG = this.getClass().getSimpleName();
    private ArrayAdapter<Alarm> assignedAlarmsAdapter;
    private List<Alarm> assignedAlarms;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View incidentView = inflater.inflate(R.layout.fragment_incident, container, false);
        final ListView assignedAlarmsListView = (ListView) incidentView.findViewById(R.id.assignedAlarms);
        assignedAlarms = new ArrayList<Alarm>();
        assignedAlarmsAdapter = new AlarmAdapter(getActivity(), R.layout.listitem_alarm, assignedAlarms);
        assignedAlarmsListView.setAdapter(assignedAlarmsAdapter);

        assignedAlarmsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.w(TAG, "clicked item: " + position);
                ((DashboardActivity) getActivity()).openAlarm((Alarm) parent.getItemAtPosition(position));
            }
        });

        return incidentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(Alarm alarm) {
        Log.w(TAG, "new alarm broadcasted");
        assignedAlarms.add(alarm);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                assignedAlarmsAdapter.notifyDataSetChanged();
            }
        });
    }
}
