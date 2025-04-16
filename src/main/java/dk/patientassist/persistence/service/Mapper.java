package dk.patientassist.persistence.service;

import java.util.HashSet;

import dk.patientassist.persistence.dto.EmployeeDTO;
import dk.patientassist.persistence.ent.Employee;

/**
 * Patient Assist
 */
public class Mapper
{
    public static EmployeeDTO EmployeeEntToDTO(Employee ent)
    {
        EmployeeDTO dto = new EmployeeDTO();
        dto.firstName = ent.firstName;
        dto.middleName = ent.middleName;
        dto.lastName = ent.lastName;
        dto.email = ent.email;
        return dto;
    }

    public static Employee EmployeeDTOToEnt(EmployeeDTO dto)
    {
        Employee ent = new Employee();
        ent.firstName = dto.firstName;
        ent.middleName = dto.middleName;
        ent.lastName = dto.lastName;
        ent.email = dto.email;
        ent.roles = dto.roles;

        ent.sections = new HashSet<>();

        return ent;
    }
}
