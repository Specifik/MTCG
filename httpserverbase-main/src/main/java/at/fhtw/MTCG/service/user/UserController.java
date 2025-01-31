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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

public class UserController {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserRepository userRepository;
    private final UnitOfWork unitOfWork;

    public UserController(UserRepository userRepository, UnitOfWork unitOfWork) {
        this.userRepository = userRepository;
        this.unitOfWork = unitOfWork;
    }

    public Response addUser(Request request) {
        try {
            User user = objectMapper.readValue(request.getBody(), User.class);
            String hashedPassword = PasswordHasher.hashPassword(user.getPassword());

            boolean success = userRepository.registerUser(user.getUsername(), hashedPassword);
            if (success) {
                unitOfWork.commitTransaction();
                return new Response(HttpStatus.CREATED, ContentType.JSON, "{ \"message\" : \"User registered successfully\" }");
            } else {
                return new Response(HttpStatus.CONFLICT, ContentType.JSON, "{ \"message\" : \"Registration failed. User may already exist.\" }");
            }
        } catch (JsonProcessingException e) {
            return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{ \"message\" : \"Invalid request body\" }");
        } catch (Exception e) {
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{ \"message\" : \"Internal Server Error\" }");
        }
    }

    public Response loginUser(Request request) {
        try {
            User loginRequest = objectMapper.readValue(request.getBody(), User.class);
            String hashedPassword = PasswordHasher.hashPassword(loginRequest.getPassword());

            User user = userRepository.findUserByUsernameAndPassword(loginRequest.getUsername(), hashedPassword);
            if (user != null) {
                userRepository.updateUserLoggedInState(user.getUsername(), true);
                String token = generateToken(user);
                userRepository.updateUserToken(token, user.getUsername());

                unitOfWork.commitTransaction();
                return new Response(HttpStatus.OK, ContentType.JSON, "{ \"message\" : \"Login successful\", \"token\": \"" + token + "\" }");
            } else {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{ \"message\" : \"Invalid username or password\" }");
            }
        } catch (JsonProcessingException e) {
            return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{ \"message\" : \"Invalid request body\" }");
        } catch (Exception e) {
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{ \"message\" : \"Internal Server Error\" }");
        }
    }

    public Response logoutUser(Request request) {
        try {
            String token = request.getHeaderMap().getHeader("token");
            if (token == null || token.isEmpty()) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{ \"message\" : \"Invalid or missing token\" }");
            }

            User user = userRepository.findUserByToken(token);
            if (user == null) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{ \"message\" : \"Invalid token\" }");
            }

            userRepository.updateUserToken(null, user.getUsername());
            userRepository.updateUserLoggedInState(user.getUsername(), false);
            unitOfWork.commitTransaction();

            return new Response(HttpStatus.OK, ContentType.JSON, "{ \"message\" : \"Logout successful\" }");
        } catch (Exception e) {
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{ \"message\" : \"Internal Server Error\" }");
        }
    }

    public Response getUser(String username, String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{ \"message\": \"Missing or invalid authentication token\" }");
        }

        String cleanToken = token.replace("Bearer ", "");
        User user = userRepository.findUserByToken(cleanToken);

        if (user == null) {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{ \"message\": \"Unauthorized\" }");
        }

        if (!user.getUsername().equals(username)) {
            return new Response(HttpStatus.FORBIDDEN, ContentType.JSON, "{ \"message\": \"Forbidden: You can only access your own profile\" }");
        }

        ObjectNode sanitizedUser = objectMapper.createObjectNode();
        sanitizedUser.put("username", user.getUsername());
        sanitizedUser.put("name", user.getName());
        sanitizedUser.put("bio", user.getBio());
        sanitizedUser.put("image", user.getImage());

        return new Response(HttpStatus.OK, ContentType.JSON, sanitizedUser.toString());
    }

    public Response getAllUsers(String token) {
        if (!validateToken(token)) {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{ \"message\" : \"Unauthorized\" }");
        }
        try {
            List<User> users = userRepository.findAllUsers();
            String usersJSON = objectMapper.writeValueAsString(users);
            return new Response(HttpStatus.OK, ContentType.JSON, usersJSON);
        } catch (Exception e) {
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{ \"message\" : \"Internal Server Error\" }");
        }
    }

    public Response updateUser(String username, Request request) {
        String token = request.getHeaderMap().getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{ \"message\": \"Missing or invalid authentication token\" }");
        }

        User user = userRepository.findUserByToken(token.replace("Bearer ", ""));
        if (user == null || !user.getUsername().equals(username)) {
            return new Response(HttpStatus.FORBIDDEN, ContentType.JSON, "{ \"message\": \"Forbidden: You can only update your own profile\" }");
        }

        try {
            JsonNode rootNode = objectMapper.readTree(request.getBody());

            if (rootNode.has("password")) {
                return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{ \"message\": \"Password cannot be updated\" }");
            }

            String newName = rootNode.has("Name") ? rootNode.get("Name").asText() : user.getName();
            String newBio = rootNode.has("Bio") ? rootNode.get("Bio").asText() : user.getBio();
            String newImage = rootNode.has("Image") ? rootNode.get("Image").asText() : user.getImage();

            boolean success = userRepository.updateUserData(username, newName, newBio, newImage);

            if (!success) {
                return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{ \"message\": \"Could not update user profile\" }");
            }

            return new Response(HttpStatus.OK, ContentType.JSON, "{ \"message\": \"User profile updated successfully\" }");
        } catch (JsonProcessingException e) {
            return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{ \"message\": \"Invalid request body\" }");
        } catch (Exception e) {
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{ \"message\": \"Internal Server Error\" }");
        }
    }


    private String generateToken(User user) {
        return user.getUsername() + "-mtcgToken";
    }

    public boolean validateToken(String token) {
        return userRepository.checkUser(token);
    }
}
