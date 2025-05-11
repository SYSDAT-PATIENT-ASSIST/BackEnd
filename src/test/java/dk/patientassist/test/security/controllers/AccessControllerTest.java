package dk.patientassist.test.security.controllers;

import dk.bugelhartmann.UserDTO;
import dk.patientassist.persistence.HibernateConfig;
import dk.patientassist.security.controllers.AccessController;
import dk.patientassist.security.controllers.SecurityController;
import dk.patientassist.security.enums.Role;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.UnauthorizedResponse;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AccessController.
 */
class AccessControllerTest {
    @Mock private Context ctx;
    @Mock private SecurityController mockSecController;
    private AccessController accessController;

    /**
     * Prepare a fake EntityManagerFactory, reset the SecurityController singleton,
     * and inject a mock SecurityController into AccessController.
     */
    @BeforeEach
    void setUp() throws Exception {
        // 1) Stub out HibernateConfig.emf so getEntityManagerFactory() won't crash.
        EntityManagerFactory fakeEmf = mock(EntityManagerFactory.class);
        Field emfField = HibernateConfig.class.getDeclaredField("emf");
        emfField.setAccessible(true);
        emfField.set(null, fakeEmf);

        // 2) Reset SecurityController.instance so getInstance() initializes cleanly.
        Field instField = SecurityController.class.getDeclaredField("instance");
        instField.setAccessible(true);
        instField.set(null, null);

        // 3) Init and inject mocks
        MockitoAnnotations.openMocks(this);
        accessController = new AccessController();
        Field scField = AccessController.class.getDeclaredField("securityController");
        scField.setAccessible(true);
        scField.set(accessController, mockSecController);
    }

    /**
     * Public routes (no roles) should do nothing.
     */
    @Test
    void accessHandler_publicRouteDoesNothing() {
        when(ctx.routeRoles()).thenReturn(Set.of());
        assertDoesNotThrow(() -> accessController.accessHandler(ctx));
        verifyNoInteractions(mockSecController);
    }

    /**
     * If authenticate() throws, accessHandler should bubble it up.
     */
    @Test
    void accessHandler_missingAuth_throws() throws Exception {
        when(ctx.routeRoles()).thenReturn(Set.of(Role.HOVEDKOK));
        Handler fakeAuth = mock(Handler.class);
        when(mockSecController.authenticate()).thenReturn(fakeAuth);
        doThrow(new UnauthorizedResponse("no header")).when(fakeAuth).handle(ctx);

        assertThrows(UnauthorizedResponse.class,
                () -> accessController.accessHandler(ctx));
    }

    /**
     * If authorization fails, accessHandler should throw.
     */
    @Test
    void accessHandler_forbiddenRole_throws() throws Exception {
        when(ctx.routeRoles()).thenReturn(Set.of(Role.ADMIN));
        Handler fakeAuth = mock(Handler.class);
        when(mockSecController.authenticate()).thenReturn(fakeAuth);
        doAnswer(inv -> {
            ctx.attribute("user", new UserDTO("u", Set.of("USER")));
            return null;
        }).when(fakeAuth).handle(ctx);
        when(mockSecController.authorize(any(), any())).thenReturn(false);

        assertThrows(UnauthorizedResponse.class,
                () -> accessController.accessHandler(ctx));
    }

    /**
     * hasRole() and requireRole() should permit and deny appropriately.
     */
    @Test
    void hasAndRequireRole() {
        UserDTO user = new UserDTO("u", Set.of("ADMIN","USER"));
        when(ctx.attribute("user")).thenReturn(user);

        assertTrue(accessController.hasRole(ctx, Role.ADMIN));
        assertDoesNotThrow(() -> accessController.requireRole(ctx, Role.ADMIN));

        UnauthorizedResponse ex = assertThrows(UnauthorizedResponse.class,
                () -> accessController.requireRole(ctx, Role.LÆGE));
        assertTrue(ex.getMessage().contains("Required role: LÆGE"));
    }

    /**
     * requireOneOfRoles() should pass if any match, else throw.
     */
    @Test
    void requireOneOfRoles() {
        UserDTO user = new UserDTO("u", Set.of("KOK"));
        when(ctx.attribute("user")).thenReturn(user);

        assertDoesNotThrow(() -> accessController.requireOneOfRoles(ctx, Role.KOK, Role.ADMIN));

        UnauthorizedResponse ex = assertThrows(UnauthorizedResponse.class,
                () -> accessController.requireOneOfRoles(ctx, Role.ADMIN, Role.LÆGE));
        assertTrue(ex.getMessage().contains("Required one of"));
    }
}
