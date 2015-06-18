package sintef.android.emht_app.models;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Date;

/**
 * Created by iver on 12/06/15.
 */
public class Alarm extends SugarRecord<Alarm> {

    @JsonProperty("id")
    private Long alarmId;

    private String type;
    private Callee callee;
    private Date openingTime;
    @Ignore
    private Date dispatchingTime; // TODO: implement dispatching
    @Ignore
    private Date closingTime; // at the moment we are dispatching and closing all alarms
    private String occuranceAddress; // address of where the incident took place
    private boolean expired = false;
    private AlarmAttendant attendant;
    private AlarmAttendant mobileCareTaker;
    private String alarmLog;
    private String notes;
    private Patient patient;
    private double latitude;
    private double longitude;

    /* empty constructor required by sugar */
    public Alarm() {}

    public Alarm(Long alarmId, String type, Callee callee, Date openingTime, Date dispatchingTime, Date closingTime, String occuranceAddress, boolean expired, AlarmAttendant attendant, AlarmAttendant mobileCareTaker, String alarmLog, String notes, Patient patient, double latitude, double longitude) {
        this.alarmId = alarmId;
        this.type = type;
        this.callee = callee;
        this.openingTime = openingTime;
        this.dispatchingTime = dispatchingTime;
        this.closingTime = closingTime;
        this.occuranceAddress = occuranceAddress;
        this.expired = expired;
        this.attendant = attendant;
        this.mobileCareTaker = mobileCareTaker;
        this.alarmLog = alarmLog;
        this.notes = notes;
        this.patient = patient;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getAlarmId() {
        return alarmId;
    }

    public void setAlarmId(Long id) {
        this.alarmId = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Callee getCallee() {
        return callee;
    }

    public void setCallee(Callee callee) {
        this.callee = callee;
    }

    public Date getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(Date openingTime) {
        this.openingTime = openingTime;
    }

    public Date getDispatchingTime() {
        return dispatchingTime;
    }

    public void setDispatchingTime(Date dispatchingTime) {
        this.dispatchingTime = dispatchingTime;
    }

    public Date getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(Date closingTime) {
        this.closingTime = closingTime;
    }

    public String getOccuranceAddress() {
        return occuranceAddress;
    }

    public void setOccuranceAddress(String occuranceAddress) {
        this.occuranceAddress = occuranceAddress;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public AlarmAttendant getAttendant() {
        return attendant;
    }

    public void setAttendant(AlarmAttendant attendant) {
        this.attendant = attendant;
    }

    public String getAlarmLog() {
        return alarmLog;
    }

    public void setAlarmLog(String alarmLog) {
        this.alarmLog = alarmLog;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public AlarmAttendant getMobileCareTaker() {
        return mobileCareTaker;
    }

    public void setMobileCareTaker(AlarmAttendant mobileCareTaker) {
        this.mobileCareTaker = mobileCareTaker;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
