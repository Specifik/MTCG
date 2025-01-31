package at.fhtw.MTCG.service.test;

import at.fhtw.MTCG.dal.UnitOfWork;
import at.fhtw.MTCG.dal.repository.UserRepository;
import at.fhtw.MTCG.model.User;
import at.fhtw.MTCG.service.user.UserController;
import at.fhtw.httpserver.server.HeaderMap;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.MTCG.utils.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserControllerTest {
    private UserController userController;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        UnitOfWork unitOfWork = mock(UnitOfWork.class);
        userController = new UserController(userRepository, unitOfWork);
    }

    @Test
    void testRegisterUser_Success() {
        // Arrange
        Request request = mock(Request.class);
        when(request.getBody()).thenReturn("{\"Username\": \"testUser\", \"Password\": \"password123\"}");

        ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);
        when(userRepository.registerUser(eq("testUser"), passwordCaptor.capture())).thenReturn(true);

        // Act
        Response response = userController.addUser(request);

        // Assert
        assertEquals(201, response.status, "User registration should return HTTP 201");

        // Prüfen, ob das Passwort gehasht wurde (nicht mit Klartext verglichen wird)
        String capturedHashedPassword = passwordCaptor.getValue();
        assertNotEquals("password123", capturedHashedPassword, "Password should be hashed");
        assertTrue(capturedHashedPassword.length() > 20, "Hashed password should be sufficiently long");
    }

    @Test
    void testRegisterUser_Fail_UserExists() {
        // Arrange
        Request request = mock(Request.class);
        String hashedPassword = PasswordHasher.hashPassword("password123");

        when(request.getBody()).thenReturn("{\"Username\": \"existingUser\", \"Password\": \"password123\"}");
        when(userRepository.registerUser("existingUser", hashedPassword)).thenReturn(false);

        // Act
        Response response = userController.addUser(request);

        // Assert
        assertEquals(409, response.status, "User registration should return HTTP 409 if user exists");
    }

    @Test
    void testLoginUser_Success() {
        // Arrange
        Request request = mock(Request.class);
        String hashedPassword = PasswordHasher.hashPassword("password123");
        User mockUser = new User(1, "testUser", hashedPassword, "Test Name", 100, "", "", "testUser-mtcgToken", true, 100, 0, 0, 0);

        when(request.getBody()).thenReturn("{\"Username\": \"testUser\", \"Password\": \"password123\"}");
        when(userRepository.findUserByUsernameAndPassword("testUser", hashedPassword)).thenReturn(mockUser);

        // Act
        Response response = userController.loginUser(request);

        // Assert
        assertEquals(200, response.status, "Login should return HTTP 200");
        assertTrue(response.content.contains("testUser-mtcgToken"), "Response should contain valid token");
    }

    @Test
    void testLoginUser_Fail_WrongPassword() {
        // Arrange
        Request request = mock(Request.class);
        String hashedWrongPassword = PasswordHasher.hashPassword("wrongPassword");

        when(request.getBody()).thenReturn("{\"Username\": \"testUser\", \"Password\": \"wrongPassword\"}");
        when(userRepository.findUserByUsernameAndPassword("testUser", hashedWrongPassword)).thenReturn(null);

        // Act
        Response response = userController.loginUser(request);

        // Assert
        assertEquals(401, response.status, "Login should return HTTP 401 for incorrect password");
    }

    @Test
    void testGetUser_Success() {
        // Arrange
        String username = "testUser";
        String token = "testUser-mtcgToken";
        User mockUser = new User(1, username, "password", "Test Name", 100, "Test Bio", "Test Image", token, true, 100, 10, 5, 5);

        when(userRepository.findUserByToken(token)).thenReturn(mockUser);

        // Act
        Response response = userController.getUser(username, "Bearer " + token);

        // Assert
        assertEquals(200, response.status, "Fetching user data should return HTTP 200");

        // Prüfen, ob die Antwort die richtigen Felder enthält
        assertTrue(response.content.contains("\"username\":\"testUser\""), "Response should contain username");
        assertTrue(response.content.contains("\"name\":\"Test Name\""), "Response should contain name");
        assertTrue(response.content.contains("\"bio\":\"Test Bio\""), "Response should contain bio");
        assertTrue(response.content.contains("\"image\":\"Test Image\""), "Response should contain image");
    }

    @Test
    void testGetUser_Fail_InvalidToken() {
        // Arrange
        String username = "testUser";
        String invalidToken = "invalid-token";
        when(userRepository.findUserByToken(invalidToken)).thenReturn(null);

        // Act
        Response response = userController.getUser(username, "Bearer " + invalidToken);

        // Assert
        assertEquals(401, response.status, "Fetching user data should return HTTP 401 if token is invalid");
    }
}
