package dk.patientassist.persistence.ent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.Set;

/**
 * Patient Assist
 */
@Entity
public class Section {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String name;
    @ManyToMany
    @JsonIgnore
    @JoinTable(name = "employee_section", joinColumns = @JoinColumn(name = "employee_id"), inverseJoinColumns = @JoinColumn(name = "section_id"))
    public Set<Employee> employees;
    @OneToMany(mappedBy = "section")
    @JsonIgnore
    public Set<Bed> beds;
}
