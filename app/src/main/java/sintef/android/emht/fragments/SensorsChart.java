package sintef.android.emht.fragments;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.tools.ZoomEvent;
import org.achartengine.tools.ZoomListener;

import java.util.Date;
import java.util.HashMap;

import de.greenrobot.event.EventBus;
import sintef.android.emht.models.Patient;
import sintef.android.emht.models.SensorData;

/**
 * Created by iver on 01/07/15.
 */
public class SensorsChart implements View.OnClickListener {

    private final String TAG = this.getClass().getSimpleName();
    private XYMultipleSeriesDataset mDataset;
    private XYMultipleSeriesRenderer mRenderer;
    private HashMap<String, TimeSeries> mSeries;
    private GraphicalView mChartView;
    private Activity mActivity;
    private double mYAxisMin = Double.MAX_VALUE;
    private double mYAxisMax = Double.MIN_VALUE;
    private double mZoomLevel = 1;
    private int mYAxisPadding = 10;
    private static final int XRANGE = 600000; // 10 minutes
    private static final int XRIGHTPADDING = 0;
    private static final String TIME = "H:mm:ss";
    private Long pId;

    private final ZoomListener mZoomListener = new ZoomListener() {
        @Override
        public void zoomReset() {
            mZoomLevel = 1;
            scrollGraph(new Date().getTime());
        }

        @Override
        public void zoomApplied(final ZoomEvent event) {
            if (event.isZoomIn()) {
                mZoomLevel /= 2;
            }
            else {
                mZoomLevel *= 2;
            }
            scrollGraph(new Date().getTime());
        }
    };


    public SensorsChart(Activity activity, ViewGroup chartView, Long pId) {
        this.mActivity = activity;
        this.pId = pId;
        EventBus.getDefault().registerSticky(this);
        mSeries = new HashMap<>();
        mDataset = new XYMultipleSeriesDataset();
        mRenderer = new XYMultipleSeriesRenderer();

        mRenderer.setXLabelsColor(Color.BLACK);
        mRenderer.setYLabelsPadding(25);
        mRenderer.setYLabelsColor(0, Color.BLACK);
        mRenderer.setAxesColor(Color.BLACK);
        mRenderer.setGridColor(Color.DKGRAY);
        mRenderer.setBackgroundColor(Color.WHITE);
        mRenderer.setApplyBackgroundColor(true);
        mRenderer.setMarginsColor(Color.argb(204, 241, 241, 241)); // same as background

        mRenderer.setLegendTextSize(20);
        mRenderer.setLabelsTextSize(20);
        mRenderer.setPointSize(8);
        mRenderer.setMargins(new int[]{60, 60, 60, 60});

        mRenderer.setFitLegend(true);
        mRenderer.setShowGrid(true);
        mRenderer.setZoomEnabled(true);
        mRenderer.setExternalZoomEnabled(true);
        mRenderer.setAntialiasing(true);
        mRenderer.setInScroll(true);

        mChartView = ChartFactory.getTimeChartView(mActivity, mDataset, mRenderer, TIME);
        mChartView.addZoomListener(mZoomListener, true, false);
        chartView.addView(mChartView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        buildSensorsChart();
    }

    public void buildSensorsChart() {
        Log.w(TAG, "buildSensorChart");
        for (SensorData sensorData : SensorData.findWithQuery(SensorData.class, "select * from sensor_data where patient = ? order by ROWID desc limit 50", Long.toString(pId))) {
            Log.w(TAG, "found sensor data in db");
            //updateChart(sensorData);
            //Log.w(TAG, sensorData.getValue().toString());
            if (sensorData == null) Log.w(TAG, "sensor data is null");
            if (sensorData.getValue() == null) Log.w(TAG, "sensor value is null");
            if (sensorData.getValue() != null) updateChart(sensorData);
        }
    }

    @SuppressWarnings("unused") // used by EventBus
    public void onEvent(SensorData sensorData) {
        Log.w(TAG, "eventbus caught new sensor data");
        updateChart(sensorData);
    }

    private void updateChart(SensorData sensorData) {
        if (mSeries.containsKey(sensorData.getReadingTypeInNaturalLanguage())) {
            TimeSeries series = mSeries.get(sensorData.getReadingTypeInNaturalLanguage());
            series.add(sensorData.getDate(), sensorData.getValue());
            updateMinMaxXY(sensorData.getValue());
            Log.w(TAG, "added data to chart series");
        } else {
            TimeSeries series = new TimeSeries(sensorData.getReadingTypeInNaturalLanguage());
            series.add(sensorData.getDate(), sensorData.getValue());
            mSeries.put(sensorData.getReadingTypeInNaturalLanguage(), series);
            mDataset.addSeries(series);
            mRenderer.addSeriesRenderer(getSeriesRenderer(getColorForRenderer(sensorData.getReadingType())));
            Log.w(TAG, "added new chart series");
        }
        scrollGraph(sensorData.getDate().getTime());
        mChartView.repaint();
        Log.w(TAG, "graph repainted");
    }

    private void updateMinMaxXY(Double value) {
        if (mYAxisMin > value) mYAxisMin = value;
        if (mYAxisMax < value) mYAxisMax = value;
    }

    private int getColorForRenderer(String readingType) {
        switch (readingType) {
            case ("heartRate"):
                return Color.RED;
            case ("diastolicPressure"):
                return Color.BLUE;
            case ("systolicPressure"):
                return Color.GREEN;
            case ("battery"):
                return Color.MAGENTA;
            default:
                return Color.BLACK;
        }
    }

    private XYSeriesRenderer getSeriesRenderer(int color) {
        final XYSeriesRenderer r = new XYSeriesRenderer();
        r.setDisplayChartValues(false);
        r.setPointStrokeWidth(0);
        r.setColor(color);
        r.setFillPoints(false);
        r.setLineWidth(4);
        return r;
    }

    private void scrollGraph(final long time) {
        // mZoomLevel is set to 1
        final double[] limits = new double[] {
                time - XRANGE * mZoomLevel, // minX
                time + XRIGHTPADDING * mZoomLevel, // maxX
                mYAxisMin - mYAxisPadding, // minY
                mYAxisMax + mYAxisPadding // maxY
        };
        mRenderer.setRange(limits);
    }

    @Override
    public void onClick(View v) {

    }
}
