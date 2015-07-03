package sintef.android.emht_app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

import sintef.android.emht_app.models.Alarm;
import sintef.android.emht_app.utils.Helper;

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
            ImageView icon = (ImageView) view.findViewById(R.id.assignedAlarmIcon);
            TextView name = (TextView) view.findViewById(R.id.assignedAlarmName);
            TextView description = (TextView) view.findViewById(R.id.assignedAlarmDescription);

            if (icon != null) {
                icon.setImageResource(alarm.getImageResourceForType());
            }

            if (name != null) {
                name.setText(Helper.dateToPrettyString(alarm.getDispatchingTime()));
            }

            if (description != null) {
                description.setText(alarm.getOccuranceAddress());
            }

            if (alarm.isActive()) {
                view.setBackgroundColor(Color.rgb(245, 245, 245));
            }
        }
        return view;
    }
}
