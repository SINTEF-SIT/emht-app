package sintef.android.emht;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.ui.IconGenerator;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import sintef.android.emht.account.BoundServiceListener;
import sintef.android.emht.events.NewAlarmEvent;
import sintef.android.emht.events.SyncEvent;
import sintef.android.emht.models.Alarm;
import sintef.android.emht.sync.RegistrationIntentService;
import sintef.android.emht.sync.ServerSync;
import sintef.android.emht.utils.CachingUrlTileProvider;
import sintef.android.emht.utils.Constants;
import sintef.android.emht.utils.Helper;

/**
 * Created by iver on 01/07/15.
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private final String TAG = this.getClass().getSimpleName();
    private Map<LatLng, Alarm> markerMap;
    private Marker firstAlarmMarker;
    private ServerSync mServerSync;
    private boolean mBound = false;
    private GoogleMap googleMap;
    private boolean firstAlarmSet = false;
    private int padding;
    private float maxZoom = 17.0f; // to avoid breaching openstreetmaps tile usage policy
    private TileOverlayOptions tileOverlayOptions;
    private MapFragment mapFragment;
    private boolean accountSet = false;

    private ServiceConnection mConnection = new ServiceConnection() {


        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.w(TAG, "service connected");
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ServerSync.LocalBinder binder = (ServerSync.LocalBinder) service;
            mServerSync = binder.getService();
            mBound = true;
            binder.setListener(new BoundServiceListener() {
                @Override
                public void showGooglePlayServicesErrorDialog(int errorCode) {
                    Log.w(TAG, "error with play services");
                    buildGooglePlayServicesErrorDialog(errorCode);
                }

                @Override
                public void showAlarmTransmitComplete() {
                    // show toast with message?
                }

                @Override
                public void updateSensors() {
                    //assessmentFragment.updateSensors();
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private void buildGooglePlayServicesErrorDialog(int errorCode) {
        if (GooglePlayServicesUtil.isUserRecoverableError(errorCode)) {
            Log.w(TAG, "is recoverable");
            GooglePlayServicesUtil.getErrorDialog(errorCode, this, 9000, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    MapsActivity.this.finish();
                }
            }).show();
        } else {
            Toast.makeText(this, R.string.device_incompatible, Toast.LENGTH_SHORT);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
        Intent intent = new Intent(this, ServerSync.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        setContentView(R.layout.activity_maps);

    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
            buildGooglePlayServicesErrorDialog(GooglePlayServicesUtil.isGooglePlayServicesAvailable(this));
        } else {
            if (!accountSet) setAccount();
            if (mapFragment == null) {
                mapFragment = (MapFragment) getFragmentManager()
                        .findFragmentById(R.id.map);
                // add our cached tile provider to map
                mapFragment.getMap().setMapType(GoogleMap.MAP_TYPE_NONE);
                tileOverlayOptions = new CachingUrlTileProvider(this, 256, 256) {
                    @Override
                    public String getTileUrl(int x, int y, int z) {
                        // return null if zoom is too high (in compliance with openstreetmap rules)
                        if (z > maxZoom) return null;
                        return String.format("https://a.tile.openstreetmap.org/%3$s/%1$s/%2$s.png", x, y, z);
                    }
                }.createTileOverlayOptions();
                mapFragment.getMapAsync(this);
                markerMap = new HashMap<>();
                padding = getMapPadding(this);

            }
            firstAlarmSet = false;
            updateMarkers();
            if (googleMap != null) {
                googleMap.clear();
                googleMap.addTileOverlay(tileOverlayOptions);
            }
            // Remove notifications from notification panel
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(Constants.GCM_NEW_ALARM_NOTIFICATION_ID);
        }

    }

    private void setAccount() {
        if (AccountManager.get(this).getAccountsByType(Constants.ACCOUNT_TYPE).length == 0) {
            addNewAccount();
        } else {
            startGcmRegistration();
            EventBus.getDefault().post(new SyncEvent());
        }
        accountSet = true;
    }

    private void addNewAccount() {
        final AccountManagerFuture<Bundle> future = AccountManager.get(this).addAccount(Constants.ACCOUNT_TYPE, null, null, null, this, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    Bundle bnd = future.getResult();
                    Log.w(TAG, "Account was created");
                    Log.d(TAG, "AddNewAccount Bundle is " + bnd);
                    // update our gcm reg id
                    startGcmRegistration();
                    EventBus.getDefault().post(new SyncEvent());

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.w(TAG, e.getMessage());
                }
            }
        }, null);
    }

    private void startGcmRegistration() {
        startService(new Intent(this, RegistrationIntentService.class));
    }

    public void showLogoutDialog(View view) {
        new android.support.v4.app.DialogFragment() {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setMessage(R.string.dialog_logout)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                logout();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });
                // Create the AlertDialog object and return it
                return builder.create();
            }
        }.show(getSupportFragmentManager(), "InfoWindowDialog");
    }

    private void logout() {
        // clear database
        Alarm.deleteAll(Alarm.class);

        // remove account and restart app
        stopService(new Intent(this, ServerSync.class));
        Account account = AccountManager.get(this).getAccountsByType(Constants.ACCOUNT_TYPE)[0];
        if (Build.VERSION.SDK_INT < 22) {
            AccountManager.get(this).removeAccount(account, new AccountManagerCallback<Boolean>() {
                @Override
                public void run(AccountManagerFuture<Boolean> future) {
                    try {
                        future.getResult();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, null);
        } else {
            AccountManager.get(this).removeAccount(account, this, new AccountManagerCallback<Bundle>() {
                @Override
                public void run(AccountManagerFuture<Bundle> future) {
                    try {
                        future.getResult();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, null);
        }
        Intent intent = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finish();
        startActivity(intent);
    }

    private static int getMapPadding(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        int smallest = (width < height) ? width : height;

        return smallest/6; // a sort-of-educated guess
    }


    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;

        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                View view = getLayoutInflater().inflate(R.layout.alarm_info_window, null);
                TextView title = (TextView) view.findViewById(R.id.info_window_title);
                TextView description = (TextView) view.findViewById(R.id.info_window_description);
                TextView dispatchTimeDelta = (TextView) view.findViewById(R.id.info_window_dispatch_time_delta);

                title.setText(marker.getTitle());
                String[] snippets = marker.getSnippet().split(";");
                Log.w(TAG, snippets[0]);
                Log.w(TAG, snippets[1]);
                description.setText(snippets[0]);
                dispatchTimeDelta.setText(snippets[1]);

                return view;
            }
        });

        googleMap.addTileOverlay(tileOverlayOptions);
        Log.w(TAG, "map ready");
        googleMap.setMyLocationEnabled(true);

        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(final Marker marker) {
                Log.w(TAG, "onInfoWindowCliecked");
                Log.w(TAG, "marker id: " + marker.getId());
                Log.w(TAG, "firstAlarmMarker id: " + firstAlarmMarker.getId());
                if (!marker.equals(firstAlarmMarker)) return;
                new android.support.v4.app.DialogFragment() {
                    @Override
                    public Dialog onCreateDialog(Bundle savedInstanceState) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                        builder.setMessage(R.string.dialog_select_alarm)
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        selectAlarm(markerMap.get(marker.getPosition()).getId());
                                    }
                                })
                                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // User cancelled the dialog
                                    }
                                });
                        // Create the AlertDialog object and return it
                        return builder.create();
                    }
                }.show(getSupportFragmentManager(), "InfoWindowDialog");
            }
        });

        googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if (cameraPosition.zoom > maxZoom)
                    googleMap.animateCamera(CameraUpdateFactory.zoomTo(maxZoom));
            }
        });

        /* TODO: Find out how to set the map zoom before/while the map loads */
        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                updateMarkers();
            }
        });

    }

    private synchronized  void updateMarkers() {
        Log.w(TAG, "updating markers");
        if (Helper.getAllUnfinishedAlarmsSorted().size() == 0) return; // no alarms in queue. don't build markers
        new AsyncTask<Void, Void, List<MarkerOptions>>() {
            LatLngBounds.Builder builder;
            @Override
            protected List<MarkerOptions> doInBackground(Void... params) {
                builder = new LatLngBounds.Builder();
                List<MarkerOptions> markerOptions = new ArrayList<>();
                int queueNumber = 1;
                for (Alarm alarm : Helper.getAllUnfinishedAlarmsSorted()) {
                    if (markerMap.containsKey(alarm)) { firstAlarmSet = true; queueNumber++; continue; }
                    LatLng latLng = findLatLng(alarm);

                    // custom markers showing the queue number of the alarm
                    IconGenerator iconGenerator = new IconGenerator(getApplicationContext());
                    iconGenerator.setBackground(getResources().getDrawable(R.mipmap.ic_alarm_map));
                    iconGenerator.setTextAppearance(R.style.map_icon_font);
                    Bitmap bitmap = iconGenerator.makeIcon("     " + Integer.toString(queueNumber));

                    String snippet = "Alarm type: " + getResources().getString(alarm.getTypeInNaturalLanguage()) + ".";
                    if (queueNumber == 1) snippet += " Tap to select.";

                    Long dispatchTimeDelta = new Date().getTime() - alarm.getDispatchingTime().getTime();
                    Long dispatchTimeDeltaMinutes = dispatchTimeDelta / (60 * 1000) % 60;
                    Long dispatchTimeDeltaHours = dispatchTimeDeltaMinutes / 60;
                    String timeSinceDispatch = "";
                    if (dispatchTimeDeltaHours > 0) timeSinceDispatch += dispatchTimeDelta + "h ";
                    timeSinceDispatch += dispatchTimeDeltaMinutes + "m";
                    // ; is a marker for splitting the string in the InfoWindowAdapter
                    snippet += ";" + "Time since dispatch: " + timeSinceDispatch;

                    MarkerOptions marker = new MarkerOptions()
                            .title(alarm.getOccuranceAddress())
                            .snippet(snippet)
                            .position(latLng)
                            .icon(BitmapDescriptorFactory.fromBitmap(bitmap));

                    builder.include(marker.getPosition());
                    markerMap.put(marker.getPosition(), alarm);
                    queueNumber++;
                    markerOptions.add(marker);
                }
                return markerOptions;
            }

            @Override
            protected void onPostExecute(List<MarkerOptions> markerOptions) {
                Log.w(TAG, "onPostExecute");
                int queueNumber = 1;
                for (MarkerOptions marker : markerOptions) {
                    if (queueNumber == 1 && !firstAlarmSet) { firstAlarmMarker = googleMap.addMarker(marker); firstAlarmSet = true; }
                    else googleMap.addMarker(marker);
                    queueNumber++;
                }
                LatLngBounds bounds = builder.build();
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            }

        }.execute();

    }

    private LatLng findLatLng(Alarm alarm) {
        if (alarm.getLatitude() == 0L || alarm.getLongitude() == 0L) {
            try {
                Address address = new Geocoder(this).getFromLocationName(alarm.getOccuranceAddress(), 1).get(0);
                return new LatLng(address.getLatitude(), address.getLongitude());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return new LatLng(alarm.getLatitude(), alarm.getLongitude());
    }

    private void selectAlarm(Long alarmId) {
        Alarm.findById(Alarm.class, alarmId).setActive(true);
        Intent dashboardActivity = new Intent(this, DashboardActivity.class);
        dashboardActivity.putExtra(Constants.ALARM_ID, alarmId);
        startActivity(dashboardActivity);
    }

    @SuppressWarnings("unused")
    public void onEvent(NewAlarmEvent newAlarmEvent) {
        Log.w(TAG, "received new alarm event");
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateMarkers();
                ((TextView) findViewById(R.id.number_of_alarms)).setText(Integer.toString(Helper.getAllUnfinishedAlarms().size()));
            }
        });

    }

    public void callCentral(View view) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        String number = getResources().getString(R.string.central_phone_number);
        callIntent.setData(Uri.parse("tel:"+number));
        startActivity(callIntent);
    }
}
