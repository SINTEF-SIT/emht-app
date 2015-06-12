package sintef.android.emht_app.models;

import java.util.Date;

/**
 * Created by iver on 12/06/15.
 */
public class Alarm {

    public Long id;

    public String type;

    public Callee callee;
    public Date openingTime;
    public Date dispatchingTime; // TODO: implement dispatching
    public Date closingTime; // at the moment we are dispatching and closing all alarms
    public String occuranceAddress; // address of where the incident took place
    public boolean expired = false;
    public AlarmAttendant attendant;
    public String alarmLog;
    public String notes;

}
