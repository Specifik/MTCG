package at.fhtw.MTCG.service.user;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.MTCG.dal.UnitOfWork;
import at.fhtw.MTCG.dal.repository.UserRepository;
import at.fhtw.MTCG.model.User;
import at.fhtw.MTCG.utils.PasswordHasher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;

public class UserController {
    private final ObjectMapper objectMapper = new ObjectMapper();

    //Registrierung (POST /users)
    public Response addUser(Request request) {
        try (UnitOfWork unitOfWork = new UnitOfWork()) {
            User user = objectMapper.readValue(request.getBody(), User.class);

            String hashedPassword = PasswordHasher.hashPassword(user.getPassword());
            user.setPassword(hashedPassword);

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

            String hashedPassword = PasswordHasher.hashPassword(loginRequest.getPassword());

            UserRepository userRepository = new UserRepository(unitOfWork);
            User user = userRepository.findUserByUsernameAndPassword(loginRequest.getUsername(), hashedPassword);

            if (user != null) {
                userRepository.updateUserLoggedInState(user.getUsername(), true); // set logged_in true
                String token = generateToken(user);
                userRepository.updateUserToken(token,user.getUsername());
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
            String token = request.getHeaderMap().getHeader("token");
            if(token == null || token.isEmpty()) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{ \"message\" : \"Invalid or missing token\" }");
            }
            UserRepository userRepository = new UserRepository(unitOfWork);
            User user = userRepository.findUserByToken(token);

            if(user == null) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{ \"message\" : \"Invalid token\" }");
            }

                userRepository.updateUserToken(null, user.getUsername()); // remove token from user
                userRepository.updateUserLoggedInState(user.getUsername(), false);
                unitOfWork.commitTransaction();

                return new Response(HttpStatus.OK, ContentType.JSON, "{ \"message\" : \"Logout successful\" }");

        } catch (Exception e) {
            e.printStackTrace();
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{ \"message\" : \"Internal Server Error\" }");
        }
    }

    // GET /user/id:/token:
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

    // GET /user/token:
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
    private String generateToken(User user) {
        String token = user.getUsername() + "-mtcgToken"; // token for CURL-Script
        return token;
        //return UUID.randomUUID().toString();
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
