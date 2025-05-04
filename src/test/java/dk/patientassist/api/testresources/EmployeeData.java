package dk.patientassist.api.testresources;

import dk.patientassist.persistence.enums.Role;
import dk.patientassist.service.dto.EmployeeDTO;

/**
 * TestData
 */
public class EmployeeData {

    public EmployeeDTO guest;
    public EmployeeDTO doctor;
    public EmployeeDTO nurse;
    public EmployeeDTO chef;
    public EmployeeDTO headchef;
    public EmployeeDTO admin;

    public EmployeeData() {
        guest = new EmployeeDTO();
        doctor = new EmployeeDTO();
        nurse = new EmployeeDTO();
        chef = new EmployeeDTO();
        headchef = new EmployeeDTO();
        admin = new EmployeeDTO();

        guest.email = "guest@email.dk";
        guest.firstName = "guest";
        guest.middleName = "guest";
        guest.lastName = "guest";
        guest.roles = new Role[]{Role.GUEST};
        guest.sections = new Long[0];
        guest.setPassword("guest");

        doctor.email = "doctor@email.dk";
        doctor.firstName = "doctor";
        doctor.middleName = "doctor";
        doctor.lastName = "doctor";
        doctor.roles = new Role[]{Role.DOCTOR};
        doctor.sections = new Long[]{0L, 1L};
        doctor.setPassword("doctor");

        nurse.email = "nurse@email.dk";
        nurse.firstName = "nurse";
        nurse.middleName = "nurse";
        nurse.lastName = "nurse";
        nurse.roles = new Role[]{Role.NURSE};
        nurse.sections = new Long[]{0L, 1L};
        nurse.setPassword("nurse");

        chef.email = "chef@email.dk";
        chef.firstName = "chef";
        chef.middleName = "chef";
        chef.lastName = "chef";
        chef.roles = new Role[]{Role.CHEF};
        chef.sections = new Long[0];
        chef.setPassword("chef");

        headchef.email = "headchef@email.dk";
        headchef.firstName = "headchef";
        headchef.middleName = "headchef";
        headchef.lastName = "headchef";
        headchef.roles = new Role[]{Role.HEADCHEF};
        headchef.sections = new Long[0];
        headchef.setPassword("headchef");

        admin.email = "admin@email.dk";
        admin.firstName = "admin";
        admin.middleName = "admin";
        admin.lastName = "admin";
        admin.roles = new Role[]{Role.ADMIN};
        admin.sections = new Long[0];
        admin.setPassword("admin");
    }
}
