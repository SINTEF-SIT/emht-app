package sintef.android.emht_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

import sintef.android.emht_app.models.Alarm;

/**
 * Created by iver on 15/06/15.
 */
public class AlarmAdapter extends ArrayAdapter<Alarm> {

    public AlarmAdapter(Context context, int resource, List<Alarm> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            LayoutInflater layoutInflater;
            layoutInflater = LayoutInflater.from(getContext());
            view = layoutInflater.inflate(R.layout.listitem_alarm, null);
        }

        Alarm alarm = getItem(position);

        if (alarm != null) {
            TextView name = (TextView) view.findViewById(R.id.assignedAlarmName);
            TextView description = (TextView) view.findViewById(R.id.assignedAlarmDescription);

            if (name != null) {
                name.setText(alarm.getPatient().getName());
            }

            if (description != null) {
                description.setText(alarm.getOccuranceAddress());
            }
        }
        return view;
    }
}
