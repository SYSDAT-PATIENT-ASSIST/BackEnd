package dk.patientassist.test.security.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.bugelhartmann.ITokenSecurity;
import dk.bugelhartmann.UserDTO;
import dk.patientassist.security.controllers.SecurityController;
import dk.patientassist.security.daos.ISecurityDAO;
import io.javalin.http.Context;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SecurityControllerTest {

    @Mock
    private Context ctx;

    @Mock
    private ISecurityDAO securityDAO;

    @Mock
    private ITokenSecurity tokenSecurity;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SecurityController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Make ctx.status(...) and ctx.json(...) return the ctx itself,
        // so that handler code like ctx.status(200).json(...) doesn't NPE.
        when(ctx.status(anyInt())).thenReturn(ctx);
        when(ctx.json(any())).thenReturn(ctx);

        // Make objectMapper.createObjectNode() return a real mutable node:
        when(objectMapper.createObjectNode())
                .thenAnswer(inv -> new ObjectMapper().createObjectNode());
    }

    @Test
    void testLogin_Success() throws Exception {
        // Arrange
        UserDTO loginReq      = new UserDTO("alice", "password123");
        UserDTO verifiedUser  = new UserDTO("alice", Set.of("USER"));

        when(ctx.bodyAsClass(UserDTO.class)).thenReturn(loginReq);
        when(securityDAO.getVerifiedUser("alice", "password123"))
                .thenReturn(verifiedUser);
        when(tokenSecurity.createToken(
                eq(verifiedUser),
                anyString(), anyString(), anyString()
        )).thenReturn("token123");

        // Act
        controller.login().handle(ctx);

        // Assert: status(200) then json({...})
        InOrder inOrder = inOrder(ctx);
        inOrder.verify(ctx).status(200);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<ObjectNode> nodeCaptor = ArgumentCaptor.forClass(ObjectNode.class);
        inOrder.verify(ctx).json(nodeCaptor.capture());

        ObjectNode resp = nodeCaptor.getValue();
        assertEquals("alice",       resp.get("username").asText());
        assertEquals("token123",    resp.get("token").asText());
    }

    @Test
    void testLogin_InvalidCredentials() throws Exception {
        // Arrange
        UserDTO loginReq = new UserDTO("bob", "wrongpass");
        when(ctx.bodyAsClass(UserDTO.class)).thenReturn(loginReq);
        when(securityDAO.getVerifiedUser("bob", "wrongpass"))
                .thenThrow(new EntityNotFoundException("User not found"));

        // Act
        controller.login().handle(ctx);

        // Assert 401 + { msg: "User not found" }
        InOrder inOrder = inOrder(ctx);
        inOrder.verify(ctx).status(401);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<ObjectNode> nodeCaptor = ArgumentCaptor.forClass(ObjectNode.class);
        inOrder.verify(ctx).json(nodeCaptor.capture());

        ObjectNode resp = nodeCaptor.getValue();
        assertEquals("User not found", resp.get("msg").asText());
    }

    @Test
    void testHealthCheck() {
        // Act
        controller.healthCheck(ctx);

        // Assert 200 + literal JSON
        InOrder inOrder = inOrder(ctx);
        inOrder.verify(ctx).status(200);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Object> argCaptor = ArgumentCaptor.forClass(Object.class);
        inOrder.verify(ctx).json(argCaptor.capture());

        assertEquals("{\"msg\": \"API is up and running\"}", argCaptor.getValue());
    }
}
