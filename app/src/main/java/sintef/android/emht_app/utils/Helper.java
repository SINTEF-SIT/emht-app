package sintef.android.emht_app.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import sintef.android.emht_app.models.Alarm;

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
        return Alarm.find(Alarm.class, "finished = ?", "0");
    }

    public static List<Alarm> getAllUnfinishedAndInactiveAlarmsSorted() {
        List<Alarm> alarms = getAllUnfinishedAndInactiveAlarms();
        Collections.sort(alarms, new AlarmComparator());
        return alarms;
    }

    public static List<Alarm> getAllUnfinishedAndInactiveAlarms() {
        return Alarm.find(Alarm.class, "finished = ? and active = ?", "0", "0");
    }
}
