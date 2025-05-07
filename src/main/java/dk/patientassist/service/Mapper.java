package dk.patientassist.service;

import dk.patientassist.persistence.ent.Employee;
import dk.patientassist.persistence.ent.Event;
import dk.patientassist.persistence.enums.Role;
import dk.patientassist.service.dto.EmployeeDTO;
import dk.patientassist.service.dto.EventDTO;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Patient Assist
 */
public class Mapper {
    public static EmployeeDTO EmployeeEntToDTO(Employee ent) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.firstName = ent.firstName;
        dto.middleName = ent.middleName;
        dto.lastName = ent.lastName;
        dto.email = ent.email;
        dto.sections = ent.sections != null ? ent.sections.stream().map(s -> s.id).toArray(Long[]::new) : new Long[0];
        return dto;
    }

    public static Employee EmployeeDTOToEnt(EmployeeDTO dto) {
        Employee ent = new Employee();
        ent.firstName = dto.firstName;
        ent.middleName = dto.middleName;
        ent.lastName = dto.lastName;
        ent.email = dto.email;
        ent.roles = (dto.roles != null) ? new HashSet<Role>(Arrays.stream(dto.roles).toList()) : new HashSet<>();
        ent.sections = new HashSet<>();
        return ent;
    }

    public static Event EventDTOToEnt(EventDTO dto) {
        Event ent = new Event();
        ent.id = dto.id;
        ent.name = dto.name;
        ent.description = dto.description;
        ent.startTime = dto.startTime;
        ent.duration = dto.duration;
        return ent;
    }

    public static EventDTO EventEntToDTO(Event ent) {
        EventDTO dto = new EventDTO();
        dto.id = ent.id;
        dto.name = ent.name;
        dto.description = ent.description;
        dto.startTime = ent.startTime;
        dto.duration = ent.duration;
        return dto;
    }
}
