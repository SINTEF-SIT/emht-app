package sintef.android.emht_app.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import sintef.android.emht_app.R;
import sintef.android.emht_app.models.Alarm;

/**
 * Created by iver on 10/06/15.
 */
public class AssessmentFragment extends Fragment {

    private static final String ALARM_ID = "alarm_id";
    private long alarmId;
    private final String TAG = this.getClass().getSimpleName();

    private LineChart sensorsChart;
    private ArrayList<String> timestamps;
    private ArrayList<Entry> heartRate;
    private ArrayList<Entry> systolicPressure;
    private ArrayList<Entry> diastolicPressure;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments().getLong(ALARM_ID) == 0L) return inflater.inflate(R.layout.fragment_empty, container, false);
        alarmId = getArguments().getLong(ALARM_ID);
        Log.w(TAG, "alarm id: " + alarmId);

        Alarm alarm = Alarm.findById(Alarm.class, alarmId);
        View assessmentView = inflater.inflate(R.layout.fragment_assessment, container, false);

        sensorsChart = (LineChart) assessmentView.findViewById(R.id.sensors_chart);
        buildSensorsChart();

        return assessmentView;
    }

    private void buildSensorsChart() {
        timestamps = new ArrayList<>();
        heartRate = new ArrayList<>();
        diastolicPressure = new ArrayList<>();
        systolicPressure = new ArrayList<>();

        LineDataSet heartRateDataSet = new LineDataSet(heartRate, "Heart rate");
        LineDataSet diastolicPressureDataSet = new LineDataSet(diastolicPressure, "Diastolic pressure");
        LineDataSet systolicPressureDataSet = new LineDataSet(systolicPressure, "Systolic pressure");

        /* test */
        timestamps.add("11");
        heartRateDataSet.addEntry(new Entry(7, 11));
        diastolicPressureDataSet.addEntry(new Entry(6, 11));
        systolicPressureDataSet.addEntry(new Entry(3, 11));

        ArrayList<LineDataSet> sensorsDataSets = new ArrayList<>();
        sensorsDataSets.add(heartRateDataSet);
        sensorsDataSets.add(diastolicPressureDataSet);
        sensorsDataSets.add(systolicPressureDataSet);

        LineData sensorsData = new LineData(timestamps, sensorsDataSets);

        sensorsChart.setData(sensorsData);
        sensorsChart.invalidate();
    }

    public void updateSensors() {

    }

}
