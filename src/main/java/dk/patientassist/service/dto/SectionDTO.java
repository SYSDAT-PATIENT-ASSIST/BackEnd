package dk.patientassist.service.dto;

import lombok.EqualsAndHashCode;

/**
 * Patient Assist
 */
@EqualsAndHashCode
public class SectionDTO
{
    public Long id;
    public String name;
    public Integer[] employeeIds;
    public Integer[] bedIds;
}
