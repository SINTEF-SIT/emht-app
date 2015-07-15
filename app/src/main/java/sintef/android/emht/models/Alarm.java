package sintef.android.emht.models;

import com.orm.SugarRecord;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonFilter;

import java.io.Serializable;
import java.util.Date;

import sintef.android.emht.R;

/**
 * Created by iver on 12/06/15.
 */

@JsonFilter("sugarFilter")
public class Alarm extends SugarRecord<Alarm> implements Serializable {

    @JsonProperty("id")
    private Long alarmId;
    private String type;
    private Callee callee;
    private Date openingTime;
    private Date dispatchingTime; // TODO: implement dispatching
    private Date closingTime; // at the moment we are dispatching and closing all alarms
    private String occuranceAddress; // address of where the incident took place
    private boolean expired = false;
    private AlarmAttendant attendant;
    private AlarmAttendant mobileCareTaker;
    private String notes;
    private Patient patient;
    private double latitude;
    private double longitude;
    private Assessment assessment;
    private Assessment fieldAssessment;
    private boolean finished;
    @JsonIgnore
    private boolean active;
    @JsonIgnore
    private boolean addToUploadQueue;

    /* empty constructor required by sugar */
    public Alarm() {}

    /* Sugar ORM doesn't support null java.util.Date objects.
       With hack below the app will not work with pre 1970-01-01 dates.
       Annotation @JsonProperty needed at getters. */

    private Date setSugarORMNullDateHack(Date date) {
        if (date != null) return date;
        else return new Date(0);
    }

    private Date getSugarORMNullDateHack(Date date) {
        if (date.equals(new Date(0))) return null;
        else return date;
    }

    /* getters and setters required by jackson */
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

    @JsonProperty("openingTime")
    public Date getOpeningTime() {
        return getSugarORMNullDateHack(openingTime);
    }

    public void setOpeningTime(Date openingTime) {
        this.openingTime = setSugarORMNullDateHack(openingTime);
    }

    @JsonProperty("dispatchingTime")
    public Date getDispatchingTime() {
        return getSugarORMNullDateHack(dispatchingTime);
    }

    public void setDispatchingTime(Date dispatchingTime) {
        this.dispatchingTime = setSugarORMNullDateHack(dispatchingTime);
    }

    @JsonProperty("closingTime")
    public Date getClosingTime() {
        return getSugarORMNullDateHack(closingTime);
    }

    public void setClosingTime(Date closingTime) {
        this.closingTime = setSugarORMNullDateHack(closingTime);
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

    public Assessment getAssessment() {
        return assessment;
    }

    public void setAssessment(Assessment assessment) {
        this.assessment = assessment;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public Assessment getFieldAssessment() {
        return fieldAssessment;
    }

    public void setFieldAssessment(Assessment fieldAssessment) {
        this.fieldAssessment = fieldAssessment;
    }

    public int getTypeInNaturalLanguage() {

        if (this.type.contains("safety")) return R.string.safety_alarm;
        if (this.type.contains("phone")) return R.string.phone_alarm;
        if (this.type.contains("police")) return R.string.police_alarm;
        if (this.type.contains("fire")) return R.string.fire_alarm;
        if (this.type.contains("elder")) return R.string.elder_alarm;
        if (this.type.contains("fall")) return R.string.fall_alarm;
        return R.string.undefined_alarm;
    }

    public int getImageResourceForType() {

        if (this.type.contains("safety")) return R.mipmap.ic_safety_alarm;
        if (this.type.contains("phone")) return R.mipmap.ic_phone_alarm;
        if (this.type.contains("police")) return R.mipmap.ic_police_alarm;
        if (this.type.contains("fire")) return R.mipmap.ic_fire_alarm;
        if (this.type.contains("elder")) return R.mipmap.ic_elder_alarm;
        if (this.type.contains("fall")) return R.mipmap.ic_fall_alarm;
        return R.mipmap.ic_launcher;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isAddToUploadQueue() {
        return addToUploadQueue;
    }

    public void setAddToUploadQueue(boolean addToUploadQueue) {
        this.addToUploadQueue = addToUploadQueue;
    }
}
