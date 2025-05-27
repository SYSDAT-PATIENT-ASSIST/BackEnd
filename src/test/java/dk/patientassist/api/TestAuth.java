package dk.patientassist.api;

import static dk.patientassist.api.impl.HelperMethods.get;
import static dk.patientassist.api.impl.HelperMethods.jwt;
import static dk.patientassist.api.impl.HelperMethods.login;
import static dk.patientassist.api.impl.HelperMethods.logout;
import static dk.patientassist.api.impl.HelperMethods.register;
import static dk.patientassist.api.impl.HelperMethods.setup;
import static dk.patientassist.api.impl.HelperMethods.stop;

import java.util.Arrays;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import dk.patientassist.api.resources.EmployeeData;
import dk.patientassist.security.enums.Role;
import dk.patientassist.service.dto.EmployeeDTO;

/**
 * Authentication tests
 */
public class TestAuth {

    EmployeeData testData;

    @BeforeAll
    static void init() {
        setup();
    }

    @AfterAll
    static void teardown() {
        stop();
    }

    @BeforeEach
    void setupBeforeEach() {
        testData = new EmployeeData();
        logout();
    }

    /* TESTS */

    @Test
    void testRegistration() {
        try {
            register(testData.guest, "guest");
            login(testData.guest, "guest");
            get("auth/guest", 200);
            register(testData.admin, "admin");
            login(testData.admin, "admin");
            get("auth/admin_only", 200);
            register(testData.chef, "chef");
            login(testData.chef, "chef");
            get("auth/chef_only", 200);
            register(testData.headchef, "headchef");
            login(testData.headchef, "headchef");
            get("auth/headchef_only", 200);
            register(testData.nurse, "nurse");
            login(testData.nurse, "nurse");
            get("auth/nurse_only", 200);
            register(testData.doctor, "doctor");
            login(testData.doctor, "doctor");
            get("auth/doctor_only", 200);
        } catch (Exception e) {
            Assertions.fail("registration error");
        }
    }

    @Test
    void testAccessDenied() {
        get("auth/guest", 200);
        get("auth/admin_only", 403);
        get("auth/chef_only", 403);
        get("auth/headchef_only", 403);
        get("auth/doctor_only", 403);
        get("auth/nurse_only", 403);
    }

    @Test
    void testSectionsAndRoles() { // maybe randomize and hammer these types of tests
        Long[] sections = new Long[] { 1L, 2L, 3L }; // these have to actually exist, consider randomly creating and
        // then fetching at random from db
        Role[] roles = new Role[] { Role.DOCTOR, Role.ADMIN, Role.GUEST };

        EmployeeDTO emp = new EmployeeDTO();
        emp.email = "test@example.com";
        emp.firstName = "testName";
        emp.lastName = "testName";
        emp.sections = sections;
        emp.roles = roles;
        emp.setPassword("test");

        register(emp, "test");
        login(emp, "test");
        get("auth/doctor_only", 200);

        DecodedJWT jwtDec = JWT.decode(jwt);
        Long[] sectionsInResp = jwtDec.getClaim("sectionIds").asArray(Long.class);
        Role[] rolesInResp = jwtDec.getClaim("roles").asArray(Role.class);

        Arrays.sort(sections);
        Arrays.sort(sectionsInResp);
        Arrays.sort(roles);
        Arrays.sort(rolesInResp);

        Assertions.assertArrayEquals(sections, sectionsInResp);
        Assertions.assertArrayEquals(roles, rolesInResp);
    }
}
