package at.fhtw.MTCG.service.user;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.http.Method;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.httpserver.server.Service;

public class UserService implements Service {
    private final UserController userController = new UserController();

    @Override
    public Response handleRequest(Request request) {
        System.out.println("DEBUG: `handleRequest()` in UserService aufgerufen! Methode: " + request.getMethod() + ", Route: " + request.getServiceRoute());

        if ("POST".equalsIgnoreCase(request.getMethod().toString())) {
            if ("/sessions".equals(request.getServiceRoute())) {
                System.out.println("DEBUG: `POST /sessions` erkannt!");
                return userController.loginUser(request);
            }
            if (request.getPathParts().size() == 1) {
                System.out.println("DEBUG: `POST /users` erkannt!");
                return userController.addUser(request);
            }
        }

        if ("DELETE".equalsIgnoreCase(request.getMethod().toString()) && "/sessions".equals(request.getServiceRoute())) {
            System.out.println("DEBUG: `DELETE /sessions` erkannt!");
            return userController.logoutUser(request);
        }

        if ("GET".equalsIgnoreCase(request.getMethod().toString())) {
            if (request.getPathParts().size() == 1) {
                System.out.println("DEBUG: `GET /users` erkannt!");
                return userController.getAllUsers(request.getHeaderMap().getHeader("Authorization"));
            }
            if (request.getPathParts().size() == 2) {
                System.out.println("DEBUG: `GET /users/{username}` erkannt!");
                return userController.getUser(request.getPathParts().get(1), request.getHeaderMap().getHeader("Authorization"));
            }
        }

        if ("PUT".equalsIgnoreCase(request.getMethod().toString()) && request.getPathParts().size() == 2) {
            System.out.println("DEBUG: `PUT /users/{username}` erkannt!");
            return userController.updateUser(request.getPathParts().get(1), request);
        }

        return new Response(HttpStatus.METHOD_NOT_ALLOWED, ContentType.JSON, "{ \"message\": \"Method Not Allowed\" }");
    }

}
