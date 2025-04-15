package dk.patientassist.persistence.service;

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
        dto.id = ent.id;
        dto.firstName = ent.firstName;
        dto.middleName = ent.middleName;
        dto.lastName = ent.lastName;
        dto.email = ent.email;
        dto.role = ent.role;
        return dto;
    }

    public static Employee EmployeeDTOToEnd(EmployeeDTO dto)
    {
        Employee ent = new Employee();
        ent.id = dto.id;
        ent.firstName = dto.firstName;
        ent.middleName = dto.middleName;
        ent.lastName = dto.lastName;
        ent.email = dto.email;
        ent.role = dto.role;
        return ent;
    }
}
