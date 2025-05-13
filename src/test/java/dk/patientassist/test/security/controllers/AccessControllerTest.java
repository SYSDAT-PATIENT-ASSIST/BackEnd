package dk.patientassist.test.security.controllers;

import dk.bugelhartmann.UserDTO;
import dk.patientassist.security.controllers.AccessController;
import dk.patientassist.security.controllers.ISecurityController;
import dk.patientassist.security.enums.Role;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.UnauthorizedResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AccessController.
 */
class AccessControllerTest {

    @Mock
    private Context ctx;

    @Mock
    private ISecurityController mockSecController;

    private AccessController accessController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        accessController = new AccessController(mockSecController);
    }

    @Test
    void accessHandler_publicRouteDoesNothing() {
        when(ctx.routeRoles()).thenReturn(Set.of()); // No roles = public
        assertDoesNotThrow(() -> accessController.accessHandler(ctx));
        verifyNoInteractions(mockSecController);
    }

    @Test
    void accessHandler_missingAuth_throws() throws Exception {
        when(ctx.routeRoles()).thenReturn(Set.of(Role.HEAD_CHEF));
        Handler fakeAuth = mock(Handler.class);
        when(mockSecController.authenticate()).thenReturn(fakeAuth);

        doThrow(new UnauthorizedResponse("no token")).when(fakeAuth).handle(ctx);

        assertThrows(UnauthorizedResponse.class, () -> accessController.accessHandler(ctx));
    }

    @Test
    void accessHandler_forbiddenRole_throws() throws Exception {
        when(ctx.routeRoles()).thenReturn(Set.of(Role.ADMIN));
        Handler fakeAuth = mock(Handler.class);
        UserDTO mockedUser = new UserDTO("u", Set.of("USER"));

        when(mockSecController.authenticate()).thenReturn(fakeAuth);

        doAnswer(invocation -> {
            when(ctx.attribute("user")).thenReturn(mockedUser);
            return null;
        }).when(fakeAuth).handle(ctx);

        when(mockSecController.authorize(mockedUser, Set.of(Role.ADMIN))).thenReturn(false);

        assertThrows(UnauthorizedResponse.class, () -> accessController.accessHandler(ctx));
    }

    @Test
    void hasAndRequireRole() {
        UserDTO user = new UserDTO("u", Set.of("ADMIN", "USER"));
        when(ctx.attribute("user")).thenReturn(user);

        assertTrue(accessController.hasRole(ctx, Role.ADMIN));
        assertDoesNotThrow(() -> accessController.requireRole(ctx, Role.ADMIN));

        UnauthorizedResponse ex = assertThrows(UnauthorizedResponse.class,
                () -> accessController.requireRole(ctx, Role.DOCTOR));
        assertTrue(ex.getMessage().contains("Required role: DOCTOR"));
    }

    @Test
    void requireOneOfRoles() {
        UserDTO user = new UserDTO("u", Set.of("KOK"));
        when(ctx.attribute("user")).thenReturn(user);

        assertDoesNotThrow(() -> accessController.requireOneOfRoles(ctx, Role.CHEF, Role.ADMIN));

        UnauthorizedResponse ex = assertThrows(UnauthorizedResponse.class,
                () -> accessController.requireOneOfRoles(ctx, Role.ADMIN, Role.DOCTOR));
        assertTrue(ex.getMessage().contains("Required one of"));
    }
}
