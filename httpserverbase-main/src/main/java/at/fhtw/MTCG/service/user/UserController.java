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
import java.util.Map;
import java.util.HashMap;

public class UserController {
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, User> tokenStorage = new HashMap<>();

    //Registrierung (POST /users)
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
                userRepository.updateUserLoggedInState(user.getUsername(), true); // set logged_in true
                String token = generateToken();
                userRepository.updateUserToken(token,user.getUsername());

                //String token = generateToken(user); // Generate token for user
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

    public Response logoutUser(Request request) {
        try (UnitOfWork unitOfWork = new UnitOfWork()) {
            String token = request.getHeaderMap().getHeader("Authorization");
            if (token != null && tokenStorage.containsKey(token)) {
                User user = tokenStorage.get(token);
                UserRepository userRepository = new UserRepository(unitOfWork);
                userRepository.updateUserLoggedInState(user.getUsername(), false);
                tokenStorage.remove(token);
                unitOfWork.commitTransaction();
                return new Response(HttpStatus.OK, ContentType.JSON, "{ \"message\" : \"Logout successful\" }");
            } else {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{ \"message\" : \"Invalid or missing token\" }");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{ \"message\" : \"Internal Server Error\" }");
        }
    }

    // GET /user/:id
    public Response getUser(String id, String token) {

        if(!validateToken(token)) {
             return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{ \"message\" : \"Unauthorized\" }");
        }
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
    public Response getAllUsers(String token) {
        if(!validateToken(token)) {
             return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{ \"message\" : \"Unauthorized\" }");
         }
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

    // Token generation
    private String generateToken() {
        //String token = user.getUsername() + "-mtcgToken"; // Simple token return for CURL-Script
        //tokenStorage.put(token, user);
        //user.setToken(token);
        return UUID.randomUUID().toString();

    }

    // Token validation
    public boolean validateToken(String token) {

        try (UnitOfWork unitOfWork = new UnitOfWork()) {
            UserRepository userRepository = new UserRepository(unitOfWork);
            boolean isUser = userRepository.checkUser(token);
            if (isUser) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }
}
