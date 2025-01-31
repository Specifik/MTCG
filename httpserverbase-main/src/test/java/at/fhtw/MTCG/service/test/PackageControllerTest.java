package at.fhtw.MTCG.service.test;

import at.fhtw.MTCG.dal.repository.PackageRepository;
import at.fhtw.MTCG.dal.repository.UserRepository;
import at.fhtw.MTCG.dal.UnitOfWork;
import at.fhtw.MTCG.model.Card;
import at.fhtw.MTCG.model.Package;
import at.fhtw.MTCG.model.User;
import at.fhtw.MTCG.service.packages.PackageController;
import at.fhtw.httpserver.server.HeaderMap;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PackageControllerTest {
    private PackageController packageController;
    private PackageRepository packageRepository;
    private UserRepository userRepository;
    private UnitOfWork unitOfWork;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        packageRepository = mock(PackageRepository.class);
        userRepository = mock(UserRepository.class);
        unitOfWork = mock(UnitOfWork.class);

        packageController = new PackageController(packageRepository, userRepository, unitOfWork);
    }

    @Test
    void testCreatePackage_Fail() throws Exception {
        // Arrange
        Request request = mock(Request.class);
        HeaderMap headerMap = mock(HeaderMap.class);
        when(request.getHeaderMap()).thenReturn(headerMap);
        when(headerMap.getHeader("Authorization")).thenReturn("Bearer user-mtcgToken");

        Card card1 = new Card(UUID.randomUUID(), "WaterGoblin", 10, "water", null, 0);
        Package cardPackage = new Package(Arrays.asList(card1));

        when(request.getBody()).thenReturn(objectMapper.writeValueAsString(cardPackage));

        User normalUser = new User(2, "normalUser", "password123", "User", 100, "Bio", "Image", "user-mtcgToken", true, 100, 10, 5, 5);
        when(userRepository.findUserByToken("user-mtcgToken")).thenReturn(normalUser);

        // Act
        Response response = packageController.createPackage(request);

        // Assert
        assertEquals(403, response.status, "Package creation should return HTTP 403 if user is not admin");
        verify(packageRepository, never()).createPackage(any());
    }

    @Test
    void testAcquirePackage_Success() {
        // Arrange
        Request request = mock(Request.class);
        HeaderMap headerMap = mock(HeaderMap.class);
        when(request.getHeaderMap()).thenReturn(headerMap);
        when(headerMap.getHeader("Authorization")).thenReturn("Bearer testUser-mtcgToken");

        when(userRepository.findUserByToken(anyString())).thenAnswer(invocation -> {
            String token = invocation.getArgument(0);
            if (token.endsWith("-mtcgToken")) {
                return new User(1, "testUser", "password", "Test Name", 10, "", "", token, true, 100, 10, 5, 5);
            }
            return null;
        });

        when(packageRepository.acquirePackage(anyInt())).thenReturn(true);

        // Act
        Response response = packageController.acquirePackage(request);

        // Assert
        assertEquals(201, response.status, "Acquiring a package should return HTTP 201");
        verify(packageRepository, times(1)).acquirePackage(anyInt());
    }


    @Test
    void testAcquirePackage_NoPackageAvailable() {
        // Arrange
        Request request = mock(Request.class);
        HeaderMap headerMap = mock(HeaderMap.class);
        when(request.getHeaderMap()).thenReturn(headerMap);
        when(headerMap.getHeader("Authorization")).thenReturn("Bearer testUser-mtcgToken");

        when(userRepository.findUserByToken(anyString())).thenAnswer(invocation -> {
            String token = invocation.getArgument(0);
            if (token.endsWith("-mtcgToken")) {
                return new User(1, "testUser", "password", "Test Name", 100, "", "", token, true, 100, 10, 5, 5);
            }
            return null; // Falls Token ungültig
        });

        when(packageRepository.acquirePackage(anyInt())).thenReturn(false);

        // Act
        Response response = packageController.acquirePackage(request);

        // Assert
        assertEquals(404, response.status, "Should return HTTP 404 if no package is available");
        verify(packageRepository, times(1)).acquirePackage(anyInt());
    }

    @Test
    void testAcquirePackage_NoAuthHeader() {
        // Arrange
        Request request = mock(Request.class);
        HeaderMap headerMap = mock(HeaderMap.class);
        when(request.getHeaderMap()).thenReturn(headerMap);
        when(headerMap.getHeader("Authorization")).thenReturn(null);

        // Act
        Response response = packageController.acquirePackage(request);

        // Assert
        assertEquals(401, response.status, "Should return HTTP 401 if authorization header is missing");
        verify(packageRepository, never()).acquirePackage(anyInt());
    }

    @Test
    void testCreatePackage_MissingToken() {
        // Arrange
        Request request = mock(Request.class);
        HeaderMap headerMap = mock(HeaderMap.class);

        when(request.getHeaderMap()).thenReturn(headerMap);
        when(headerMap.getHeader("Authorization")).thenReturn(null); // Kein Token

        // Act
        Response response = packageController.createPackage(request);

        // Assert
        assertEquals(401, response.status, "Should return HTTP 401 if no authentication token is provided");
    }

    @Test
    void testCreatePackage_InvalidAdmin() throws Exception {
        // Arrange
        Request request = mock(Request.class);
        HeaderMap headerMap = mock(HeaderMap.class);

        when(request.getHeaderMap()).thenReturn(headerMap);
        when(headerMap.getHeader("Authorization")).thenReturn("Bearer fake-user-mtcgToken");

        when(userRepository.findUserByToken("fake-user-mtcgToken")).thenReturn(
                new User(1, "fakeUser", "password", "Fake User", 0, "", "", "fake-user-mtcgToken", true, 0, 0, 0, 0)
        );

        // Act
        Response response = packageController.createPackage(request);

        // Assert
        assertEquals(403, response.status, "Should return HTTP 403 if user is not an admin");
    }

    @Test
    void testAcquirePackage_NotEnoughCoins() {
        // Arrange
        Request request = mock(Request.class);
        HeaderMap headerMap = mock(HeaderMap.class);

        when(request.getHeaderMap()).thenReturn(headerMap);
        when(headerMap.getHeader("Authorization")).thenReturn("Bearer testUser-mtcgToken");

        when(userRepository.findUserByToken(anyString())).thenAnswer(invocation -> {
            String token = invocation.getArgument(0);
            if (token.endsWith("-mtcgToken")) {
                return new User(1, "testUser", "password", "Test Name", 0, "", "", token, true, 100, 10, 5, 5);
            }
            return null;
        });

        when(packageRepository.acquirePackage(anyInt())).thenReturn(true);

        // Act
        Response response = packageController.acquirePackage(request);

        // Assert
        assertEquals(400, response.status, "Should return HTTP 400 if user does not have enough coins");
    }

    @Test
    void testAcquirePackage_InsufficientPackages() {
        // Arrange
        Request request = mock(Request.class);
        HeaderMap headerMap = mock(HeaderMap.class);

        when(request.getHeaderMap()).thenReturn(headerMap);
        when(headerMap.getHeader("Authorization")).thenReturn("testUser-mtcgToken");

        when(userRepository.findUserByToken("testUser-mtcgToken")).thenReturn(
                new User(1, "testUser", "password", "Test User", 10, "", "", "testUser-mtcgToken", true, 0, 0, 0, 0)
        );

        when(packageRepository.acquirePackage(1)).thenReturn(false); // Kein verfügbares Paket mehr

        // Act
        Response response = packageController.acquirePackage(request);

        // Assert
        assertEquals(404, response.status, "Should return HTTP 404 if no packages are left");
    }


}
