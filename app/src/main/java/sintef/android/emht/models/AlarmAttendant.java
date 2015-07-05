package sintef.android.emht.models;

import com.orm.SugarRecord;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonFilter;

import java.io.Serializable;

/**
 * Created by iver on 12/06/15.
 */

@JsonFilter("sugarFilter")
public class AlarmAttendant extends SugarRecord<AlarmAttendant> implements Serializable {

    @JsonProperty("id")
    private Long alarmAttendantId;
    private String username;
    private int role;

    public AlarmAttendant() {}

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


    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }
}
