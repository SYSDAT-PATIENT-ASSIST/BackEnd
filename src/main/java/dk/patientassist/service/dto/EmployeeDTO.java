package dk.patientassist.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

import dk.patientassist.persistence.enums.Role;
import dk.patientassist.utilities.Utils;
import jakarta.persistence.Column;
import lombok.EqualsAndHashCode;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Patient Assist
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EmployeeDTO {
    String password; // we don't ever want to read this ideally, before encryption
    @Column(unique = true)
    @EqualsAndHashCode.Include
    @JsonProperty(required = true)
    public String email; // email == identity
    public String firstName;
    public String middleName;
    public String lastName;
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    public Role[] roles;
    @JsonProperty(required = false)
    public Long[] sections;

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean checkAgainstBCryptPw(String encrPw) {
        return BCrypt.checkpw(password, encrPw);
    }

    public String hashPw() {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public String makeRegistrationForm(String pw) throws JsonProcessingException { // for testing
        String empJson = Utils.getObjectMapperCompact().writeValueAsString(this);
        empJson = "{" + String.format("\"password\": \"%s\", ", pw) + empJson.substring(empJson.indexOf('{') + 1);
        return empJson;
    }

    public String makeLoginForm(String pw) throws JsonProcessingException { // for testing
        String empJson = String.format("{\"password\": \"%s\", \"email\": \"%s\"}", pw, email);
        return empJson;
    }
}
