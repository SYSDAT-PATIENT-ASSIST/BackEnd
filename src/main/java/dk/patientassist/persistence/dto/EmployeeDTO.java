package dk.patientassist.persistence.dto;

import java.util.Set;

import dk.patientassist.persistence.enums.Role;
import lombok.EqualsAndHashCode;

/**
* Patient Assist
*/
@EqualsAndHashCode
public class EmployeeDTO
{
    public String email;
    public String firstName;
    public String middleName;
    public String lastName;
    public Set<Role> roles;
    public Set<Integer> section_ids;
}
