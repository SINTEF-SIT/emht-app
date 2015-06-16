package sintef.android.emht_app.models;

import com.orm.SugarRecord;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by iver on 12/06/15.
 */
public class Patient extends SugarRecord<Patient> {

    @JsonProperty("id")
    private Long patientId;

    private String name;
    private String address;
    private Integer age;
    private String phoneNumber;
    private String personalNumber;
    private String obs;

    public Patient() {}

    public Patient(Long patientId, String name, String address, Integer age, String phoneNumber, String personalNumber, String obs) {
        this.patientId = patientId;
        this.name = name;
        this.address = address;
        this.age = age;
        this.phoneNumber = phoneNumber;
        this.personalNumber = personalNumber;
        this.obs = obs;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
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

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPersonalNumber() {
        return personalNumber;
    }

    public void setPersonalNumber(String personalNumber) {
        this.personalNumber = personalNumber;
    }

    public String getObs() {
        return obs;
    }

    public void setObs(String obs) {
        this.obs = obs;
    }
}
