package at.fhtw.MTCG.service.packages;

import at.fhtw.MTCG.service.packages.PackageController;
import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.httpserver.server.Service;

public class PackageService implements Service {
    private final PackageController packageController = new PackageController();

    @Override
    public Response handleRequest(Request request) {
        if ("POST".equals(request.getMethod())) {
            return packageController.createPackage(request);
        }
        return new Response(HttpStatus.METHOD_NOT_ALLOWED, ContentType.JSON,"{ \"message\": \"Method Not Allowed\" }");
    }
}

