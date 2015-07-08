package sintef.android.emht.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import sintef.android.emht.R;
import sintef.android.emht.utils.Helper;

/**
 * Created by iver on 08/07/15.
 */
public class MapHeaderFragment extends Fragment {

    private int numberOfAlarms;
    private TextView numberOfAlarmsTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        numberOfAlarms = Helper.getAllUnfinishedAlarms().size();
        View mapHeaderView = inflater.inflate(R.layout.fragment_map_header, container, false);
        numberOfAlarmsTextView = (TextView) mapHeaderView.findViewById(R.id.number_of_alarms);
        return mapHeaderView;
    }

    @Override
    public void onResume() {
        super.onResume();
        numberOfAlarmsTextView.setText(Integer.toString(Helper.getAllUnfinishedAlarms().size()));
    }
}
