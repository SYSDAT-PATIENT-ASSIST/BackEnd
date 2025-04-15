package dk.patientassist.persistence.dto;

import dk.patientassist.persistence.enums.Role;

/**
 * Patient Assist
 */
public class EmployeeDTO
{
	public Long id;
	public String middleName;
	public String lastName;
	public String email;
    public String firstName;
    public Role role;
}
