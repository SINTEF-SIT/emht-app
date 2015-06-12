package sintef.android.emht_app.models;

import android.util.JsonReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

/**
 * Created by iver on 12/06/15.
 */
public class Alarm {

    private Long id;
    private String type;
    private Callee callee;
    private Date openingTime;
    private Date dispatchingTime; // TODO: implement dispatching
    private Date closingTime; // at the moment we are dispatching and closing all alarms
    private String occuranceAddress; // address of where the incident took place
    private boolean expired = false;
    private AlarmAttendant attendant;
    private String alarmLog;
    private String notes;
    private Patient patient;

    public Alarm(Long id, String type, Callee callee, Date openingTime, Date dispatchingTime, Date closingTime, String occuranceAddress, boolean expired, AlarmAttendant attendant, String alarmLog, String notes, Patient patient) {
        this.id = id;
        this.type = type;
        this.callee = callee;
        this.openingTime = openingTime;
        this.dispatchingTime = dispatchingTime;
        this.closingTime = closingTime;
        this.occuranceAddress = occuranceAddress;
        this.expired = expired;
        this.attendant = attendant;
        this.alarmLog = alarmLog;
        this.notes = notes;
        this.patient = patient;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}
