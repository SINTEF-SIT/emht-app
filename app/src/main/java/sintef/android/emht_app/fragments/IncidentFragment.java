package sintef.android.emht_app.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import sintef.android.emht_app.IncidentActivity;
import sintef.android.emht_app.AlarmAdapter;
import sintef.android.emht_app.R;
import sintef.android.emht_app.events.NewAlarmEvent;
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
        final View incidentView = inflater.inflate(R.layout.fragment_incident, container, false);
        final ListView assignedAlarmsListView = (ListView) incidentView.findViewById(R.id.assignedAlarms);
        assignedAlarms = new ArrayList<Alarm>();
        assignedAlarms.addAll(Alarm.listAll(Alarm.class));
        for (Alarm alarm : Alarm.listAll(Alarm.class)) Log.w(TAG, "alarm: " + alarm.getType());
        assignedAlarmsAdapter = new AlarmAdapter(getActivity(), R.layout.listitem_alarm, assignedAlarms);
        assignedAlarmsListView.setAdapter(assignedAlarmsAdapter);

        assignedAlarmsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.w(TAG, "clicked item: " + position);
                ((IncidentActivity) getActivity()).selectAlarm(((Alarm) parent.getItemAtPosition(position)).getId());
            }
        });

        assignedAlarmsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                final double latitude = ((Alarm) parent.getItemAtPosition(position)).getLatitude();
                final double longitude = ((Alarm) parent.getItemAtPosition(position)).getLongitude();
                final String daddr;
                if (latitude == 0L || longitude == 0L) {
                    daddr = ((Alarm) parent.getItemAtPosition(position)).getOccuranceAddress();
                } else {
                    daddr = latitude + "," + longitude;
                }

                PopupMenu popupMenu = new PopupMenu(getActivity().getApplicationContext(), view);
                popupMenu.getMenuInflater().inflate(R.menu.alarm, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case (R.id.alarm_popup_maps):
                                startActivity(new Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("http://maps.google.com/maps?daddr=" + daddr)
                                ));
                        }
                        return true;
                    }
                });
                popupMenu.show();
                return true;
            }
        });

        ((TextView) incidentView.findViewById(R.id.usernameTextView)).setText("iver"); // should probably not
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

    @SuppressWarnings("unused") // used by EventBus
    public void onEvent(NewAlarmEvent newAlarmEvent) {
        Log.w(TAG, "new alarm broadcasted");
        assignedAlarms.add(Alarm.findById(Alarm.class, newAlarmEvent.getId()));
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                assignedAlarmsAdapter.notifyDataSetChanged();
            }
        });
    }
}
