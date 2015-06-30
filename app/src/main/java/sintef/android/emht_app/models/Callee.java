package sintef.android.emht_app.models;

import com.orm.SugarRecord;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonFilter;

import java.io.Serializable;

/**
 * Created by iver on 12/06/15.
 */

@JsonFilter("sugarFilter")
public class Callee extends SugarRecord<Callee> implements Serializable {

    @JsonProperty("id")
    private Long calleeId;
    private String name;
    private String address;
    private String phoneNumber; // TODO: add validation format to the phoneNumber

    public Callee() {}

    public Long getCalleeId() {
        return calleeId;
    }

    public void setCalleeId(Long calleeId) {
        this.calleeId = calleeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
