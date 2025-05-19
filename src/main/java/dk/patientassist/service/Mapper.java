package dk.patientassist.service;

import dk.patientassist.persistence.ent.Employee;
import dk.patientassist.persistence.ent.Event;
import dk.patientassist.persistence.ent.ExamTreat;
import dk.patientassist.persistence.ent.ExamTreatCategory;
import dk.patientassist.persistence.ent.ExamTreatType;
import dk.patientassist.persistence.enums.Role;
import dk.patientassist.service.dto.EmployeeDTO;
import dk.patientassist.service.dto.EventDTO;
import dk.patientassist.service.dto.ExamTreatCategoryDTO;
import dk.patientassist.service.dto.ExamTreatDTO;
import dk.patientassist.service.dto.ExamTreatTypeDTO;

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

    public static ExamTreatCategory ExamTreatCategoryDTOToEnt(ExamTreatCategoryDTO dto) {
        ExamTreatCategory ent = new ExamTreatCategory();
        ent.id = null;
        ent.name = dto.name;
        ent.description = dto.description;
        ent.examTreatTypes = new HashSet<>(
                Arrays.stream(dto.examTreatTypes).map(Mapper::ExamTreatTypeDTOToEnt).toList());
        ent.examTreatTypes.forEach(e -> e.examTreatCategory = ent);
        return ent;
    }

    public static ExamTreatCategoryDTO ExamTreatCategoryEntToDTO(ExamTreatCategory ent) {
        ExamTreatCategoryDTO dto = new ExamTreatCategoryDTO(ent.name);
        dto.id = ent.id;
        dto.description = ent.description;
        dto.examTreatTypes = new ExamTreatTypeDTO[0];
        return dto;
    }

    public static ExamTreatType ExamTreatTypeDTOToEnt(ExamTreatTypeDTO dto) {
        ExamTreatType ent = new ExamTreatType();
        ent.id = null;
        ent.name = dto.name;
        ent.description = dto.description;
        ent.examTreats = new HashSet<>(Arrays.stream(dto.examTreats).map(Mapper::ExamTreatDTOToEnt).toList());
        ent.examTreats.forEach(e -> e.examTreatType = ent);
        return ent;
    }

    public static ExamTreatTypeDTO ExamTreatTypeEntToDTO(ExamTreatType ent) {
        ExamTreatTypeDTO dto = new ExamTreatTypeDTO(ent.name);
        dto.id = ent.id;
        dto.description = ent.description;
        dto.examTreats = ent.examTreats != null
                ? ent.examTreats.stream().map(Mapper::ExamTreatEntToDTO).toArray(ExamTreatDTO[]::new)
                : null;
        return dto;
    }

    public static ExamTreat ExamTreatDTOToEnt(ExamTreatDTO dto) {
        ExamTreat ent = new ExamTreat();
        ent.id = null;
        ent.name = dto.name;
        ent.description = dto.description;
        ent.article = dto.article;
        ent.srcUrl = dto.srcUrl;
        return ent;
    }

    public static ExamTreatDTO ExamTreatEntToDTO(ExamTreat ent) {
        ExamTreatDTO dto = new ExamTreatDTO(ent.name);
        dto.id = ent.id;
        dto.name = ent.name;
        dto.srcUrl = ent.srcUrl;
        dto.description = ent.description;
        dto.article = null;
        return dto;
    }

    public static ExamTreatDTO ExamTreatEntToDTOFull(ExamTreat ent) {
        ExamTreatDTO dto = new ExamTreatDTO(ent.name);
        dto.id = ent.id;
        dto.name = ent.name;
        dto.srcUrl = ent.srcUrl;
        dto.description = ent.description;
        dto.article = ent.article;
        return dto;
    }
}
