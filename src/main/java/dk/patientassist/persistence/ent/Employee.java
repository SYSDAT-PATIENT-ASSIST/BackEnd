package dk.patientassist.persistence.ent;

import dk.patientassist.persistence.enums.Role;

import jakarta.persistence.*;

import java.util.Set;

/**
 * Patient Assist
 */
@Entity
public class Employee
{
    @Id
    public Long id;
    @Column(name = "first_name", nullable = false)
    public String firstName;
    @Column(name = "middle_name")
    public String middleName;
    @Column(name = "last_name", nullable = false)
    public String lastName;
    @Column(name = "user_name", nullable = false)
    public String userName;
    @Column(nullable = false)
    public String password;
    @Enumerated(EnumType.STRING)
    public Role role;
    @ManyToMany
    @JoinTable(name = "employee_section", joinColumns = @JoinColumn(name = "section_id"), inverseJoinColumns = @JoinColumn(name = "employee_id"))
    public Set<Section> sections;
	public String email;
}
