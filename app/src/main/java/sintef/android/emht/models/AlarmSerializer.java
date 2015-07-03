package sintef.android.emht.models;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

/**
 * Created by iver on 30/06/15.
 */
public class AlarmSerializer extends JsonSerializer<Alarm> {
    @Override
    public void serialize(Alarm value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        jgen.writeNumberField("id", value.getAlarmId());
        jgen.writeStringField("type", value.getType());
        jgen.writeObjectField("callee", value.getCallee());
        jgen.writeObjectField("openingTime", value.getOpeningTime());
        jgen.writeObjectField("dispatchingTime", value.getDispatchingTime());
        jgen.writeObjectField("closingTime", value.getClosingTime());
        jgen.writeStringField("occuranceAddress", value.getOccuranceAddress());
        jgen.writeBooleanField("expired", value.isExpired());
        jgen.writeObjectField("attendant", value.getAttendant());
        jgen.writeObjectField("mobileCareTaker", value.getMobileCareTaker());
        jgen.writeStringField("alarmLog", value.getAlarmLog());
        jgen.writeStringField("notes", value.getNotes());
        jgen.writeObjectField("patient", value.getPatient());
        jgen.writeNumberField("latitude", value.getLatitude());
        jgen.writeNumberField("longitude", value.getLongitude());
        jgen.writeObjectField("assessment", value.getAssessment());
        jgen.writeObjectField("fieldAssessment", value.getFieldAssessment());
        jgen.writeBooleanField("finished", value.isFinished());
        jgen.writeEndObject();
    }
}