package at.fhtw.MTCG.service.user;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.MTCG.dal.UnitOfWork;
import at.fhtw.MTCG.dal.repository.UserRepository;
import at.fhtw.MTCG.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;

public class UserController {
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Registrierung (POST /users)
    public Response addUser(Request request) {
        try (UnitOfWork unitOfWork = new UnitOfWork()) {
            User user = objectMapper.readValue(request.getBody(), User.class);
            UserRepository userRepository = new UserRepository(unitOfWork);
            boolean success = userRepository.registerUser(user.getUsername(), user.getPassword());
            if (success) {
                unitOfWork.commitTransaction();
                return new Response(HttpStatus.CREATED, ContentType.JSON, "{ \"message\" : \"User registered successfully\" }");
            } else {
                return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{ \"message\" : \"Registration failed. User may already exist.\" }");
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{ \"message\" : \"Invalid request body\" }");
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{ \"message\" : \"Internal Server Error\" }");
        }
    }

    // Login (POST /sessions)
    public Response loginUser(Request request) {
        try (UnitOfWork unitOfWork = new UnitOfWork()) {
            User loginRequest = objectMapper.readValue(request.getBody(), User.class);
            UserRepository userRepository = new UserRepository(unitOfWork);
            User user = userRepository.findUserByUsernameAndPassword(loginRequest.getUsername(), loginRequest.getPassword());

            if (user != null) {
                String token = generateToken(user); // Generate token for user
                unitOfWork.commitTransaction();
                return new Response(HttpStatus.OK, ContentType.JSON, "{ \"message\" : \"Login successful\", \"token\": \"" + token + "\" }");
            } else {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{ \"message\" : \"Invalid username or password\" }");
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{ \"message\" : \"Invalid request body\" }");
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{ \"message\" : \"Internal Server Error\" }");
        }
    }

    // GET /user/:id
    public Response getUser(String id) {
        try (UnitOfWork unitOfWork = new UnitOfWork()) {
            UserRepository userRepository = new UserRepository(unitOfWork);
            User user = userRepository.findUserByUsername(id);
            if (user == null) {
                return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{ \"message\": \"User not found\" }");
            }
            String userJSON = objectMapper.writeValueAsString(user);
            unitOfWork.commitTransaction();
            return new Response(HttpStatus.OK, ContentType.JSON, userJSON);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{ \"message\" : \"Internal Server Error\" }");
        }
    }

    // GET /user
    public Response getAllUsers() {
        try (UnitOfWork unitOfWork = new UnitOfWork()) {
            UserRepository userRepository = new UserRepository(unitOfWork);
            List<User> users = userRepository.findAllUsers();
            String usersJSON = objectMapper.writeValueAsString(users);
            unitOfWork.commitTransaction();
            return new Response(HttpStatus.OK, ContentType.JSON, usersJSON);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{ \"message\" : \"Internal Server Error\" }");
        }
    }

    // Token generierung
    private String generateToken(User user) {
        return user.getUsername() + "-mtcgToken"; // Simples Token-Format to match expected format in the CURL script
    }
}
