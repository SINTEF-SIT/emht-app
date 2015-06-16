package sintef.android.emht_app.models;

import com.orm.SugarRecord;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by iver on 12/06/15.
 */
public class AlarmAttendant extends SugarRecord<AlarmAttendant> {

    @JsonProperty("id")
    private Long alarmAttendantId;

    private String username;

    public AlarmAttendant() {}

    public AlarmAttendant(Long alarmAttendantId, String username) {
        this.alarmAttendantId = alarmAttendantId;
        this.username = username;
    }

    public Long getAlarmAttendantId() {
        return alarmAttendantId;
    }

    public void setAlarmAttendantId(Long alarmAttendantId) {
        this.alarmAttendantId = alarmAttendantId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
