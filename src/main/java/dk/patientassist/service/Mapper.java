package dk.patientassist.service;

import dk.patientassist.persistence.ent.Employee;
import dk.patientassist.persistence.enums.Role;
import dk.patientassist.service.dto.EmployeeDTO;

import java.util.Arrays;
import java.util.HashSet;

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
        dto.sections = ent.sections.stream().map(s -> s.id).toArray(Long[]::new);
        return dto;
    }

    public static Employee EmployeeDTOToEnt(EmployeeDTO dto)
    {
        Employee ent = new Employee();
        ent.firstName = dto.firstName;
        ent.middleName = dto.middleName;
        ent.lastName = dto.lastName;
        ent.email = dto.email;
        ent.roles = new HashSet<Role>(Arrays.stream(dto.roles).toList());
        ent.sections = new HashSet<>();
        return ent;
    }
}
