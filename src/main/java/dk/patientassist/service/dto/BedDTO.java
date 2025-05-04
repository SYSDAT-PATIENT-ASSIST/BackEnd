package dk.patientassist.service.dto;

import lombok.EqualsAndHashCode;

/**
 * Patient Assist
 */
@EqualsAndHashCode
public class BedDTO
{
    public Integer id;
    public Boolean occupied;
    public String patientName;
    public Integer section_id;
}
