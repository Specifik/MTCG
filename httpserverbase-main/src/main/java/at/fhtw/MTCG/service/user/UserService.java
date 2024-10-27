package at.fhtw.MTCG.service.user;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.http.Method;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.httpserver.server.Service;

public class UserService implements Service {
    private final UserController userController;

    public UserService() {
        this.userController = new UserController();
    }

    @Override
    public Response handleRequest(Request request) {
        String token = request.getHeaderMap().getHeader("token");
        if (request.getMethod() == Method.GET && request.getPathParts().size() > 1) {

            return this.userController.getUser(request.getPathParts().get(1), token);
        } else if (request.getMethod() == Method.GET) {
            return this.userController.getAllUsers(token);
        } else if (request.getMethod() == Method.POST && "/users".equals(request.getServiceRoute())) {
            return this.userController.addUser(request);
        } else if (request.getMethod() == Method.POST && "/sessions".equals(request.getServiceRoute())) {
            return this.userController.loginUser(request);
        } else if (request.getMethod() == Method.DELETE && "/sessions".equals(request.getServiceRoute())) {
            return this.userController.logoutUser(request);
        }

        return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "[user]");
    }
}
