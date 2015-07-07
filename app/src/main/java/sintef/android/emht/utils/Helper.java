package sintef.android.emht.utils;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import sintef.android.emht.models.Alarm;

/**
 * Created by iver on 03/07/15.
 */
public class Helper {

    public static String dateToPrettyString(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM-yyyy HH:mm");
        return dateFormat.format(date);
    }

    public static List<Alarm> getAllUnfinishedAlarmsSorted() {
        List<Alarm> alarms = getAllUnfinishedAlarms();
        Collections.sort(alarms, new AlarmComparator());
        return alarms;
    }

    public static List<Alarm> getAllUnfinishedAlarms() {
        return Alarm.find(Alarm.class, "add_to_upload_queue = ?", "0");
    }

    public static List<Alarm> getAllUnfinishedAndInactiveAlarmsSorted() {
        List<Alarm> alarms = getAllUnfinishedAndInactiveAlarms();
        Collections.sort(alarms, new AlarmComparator());
        return alarms;
    }

    public static List<Alarm> getAllUnfinishedAndInactiveAlarms() {
        return Alarm.find(Alarm.class, "add_to_upload_queue = ? and active = ?", "0", "0");
    }

    // from http://developer.android.com/guide/topics/location/strategies.html
    private static final int TWO_MINUTES = 1000 * 60 * 2;

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    public static boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnected();
    }
}
