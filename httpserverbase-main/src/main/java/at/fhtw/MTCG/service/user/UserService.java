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

        if ("POST".equalsIgnoreCase(request.getMethod().toString())) {
            if ("/sessions".equals(request.getServiceRoute())) {
                return userController.loginUser(request);
            }
            if (request.getPathParts().size() == 1) {
                return userController.addUser(request);
            }
        }

        if ("DELETE".equalsIgnoreCase(request.getMethod().toString()) && "/sessions".equals(request.getServiceRoute())) {
            return userController.logoutUser(request);
        }

        if ("GET".equalsIgnoreCase(request.getMethod().toString())) {
            if (request.getPathParts().size() == 1) {
                return userController.getAllUsers(request.getHeaderMap().getHeader("Authorization"));
            }
            if (request.getPathParts().size() == 2) {
                return userController.getUser(request.getPathParts().get(1), request.getHeaderMap().getHeader("Authorization"));
            }
        }

        if ("PUT".equalsIgnoreCase(request.getMethod().toString()) && request.getPathParts().size() == 2) {
            return userController.updateUser(request.getPathParts().get(1), request);
        }

        return new Response(HttpStatus.METHOD_NOT_ALLOWED, ContentType.JSON, "{ \"message\": \"Method Not Allowed\" }");
    }

}
