package sintef.android.emht.utils;

import java.util.Comparator;

import sintef.android.emht.models.Alarm;

/**
 * Created by iver on 02/07/15.
 */
public class AlarmComparator implements Comparator<Alarm> {
    @Override
    public int compare(Alarm lhs, Alarm rhs) {
        if (lhs.getDispatchingTime().before(rhs.getDispatchingTime())) return -1;
        if (lhs.getDispatchingTime().after(rhs.getDispatchingTime())) return 1;
        return 0;
    }
}
