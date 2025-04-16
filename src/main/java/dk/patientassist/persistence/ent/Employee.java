package dk.patientassist.persistence.ent;

import dk.patientassist.persistence.HibernateConfig;
import dk.patientassist.persistence.enums.Role;
import dk.patientassist.utilities.Utils;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.Array;
import org.mindrot.jbcrypt.BCrypt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * Patient Assist
 */
@Entity
@Table(name = "employee", uniqueConstraints = @UniqueConstraint(columnNames = { "email" }))
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "email", unique = true, nullable = false)
    public String email;
    @Column(name = "first_name", nullable = false)
    public String firstName;
    @Column(name = "middle_name")
    public String middleName;
    @Column(name = "last_name", nullable = false)
    public String lastName;
    @Column(nullable = false)
    public String password;
    @Array(length = 16)
    @Enumerated(EnumType.STRING)
    public Set<Role> roles;
    @ManyToMany
    @JoinTable(name = "employee_section", joinColumns = @JoinColumn(name = "section_id"), inverseJoinColumns = @JoinColumn(name = "employee_id"))
    public Set<Section> sections;

    public String getFullName() {
        return firstName + " " + (middleName != null ? middleName + " " : "") + lastName;
    }

    public String getRolesAsString() {
        if (roles == null || roles.isEmpty())
            return "No roles assigned";
        return String.join(", ", roles.stream().map(Role::name).toList());
    }

    public List<String> getRolesAsStringList() {
        if (roles == null || roles.isEmpty())
            return new ArrayList<>();
        return roles.stream().map(Role::name).toList();
    }

    public ArrayNode getRolesAsJSONArray() {
        ArrayNode arrayNode = Utils.getObjectMapperCompact().createArrayNode();
        if (roles == null || roles.isEmpty())
            return arrayNode;
        for (Role role : roles) {
            arrayNode.add(role.name());
        }
        return arrayNode;
    }

    public static Employee fromJson(JsonNode json) {
        try (EntityManager em = HibernateConfig.getEntityManagerFactory().createEntityManager()) {
            Employee emp = new Employee();
            emp.email = json.get("email").asText();
            emp.password = BCrypt.hashpw(json.get("password").asText(), BCrypt.gensalt());
            emp.firstName = json.get("first_name").asText();
            emp.middleName = json.get("middle_name").asText();
            emp.lastName = json.get("last_name").asText();

            emp.roles = new HashSet<>();
            if (json.has("roles")) {
                for (JsonNode role : json.get("roles")) {
                    emp.roles.add(Role.valueOf(role.asText().toUpperCase()));
                }
            }

            emp.sections = new HashSet<>();
            if (json.has("sections")) {
                for (JsonNode sectionId : json.get("sections")) {
                    Section section = em.find(Section.class, sectionId.asLong());
                    if (section != null) {
                        section.employees.add(emp);
                        emp.sections.add(section);
                    }
                }
            }
            return emp;
        }
    }
}
