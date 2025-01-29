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

        System.out.println("DEBUG: Incoming request - " + request.getMethod() + " " + request.getPathname());
        System.out.println("DEBUG: Processed Service Route - '" + request.getServiceRoute().trim() + "'");
        System.out.println("DEBUG: Authorization Header - " + request.getHeaderMap().getHeader("Authorization"));
        System.out.println("DEBUG: Raw Pathname - '" + request.getPathname() + "'");
        System.out.println("DEBUG: Trimmed Pathname - '" + request.getPathname().trim() + "'");

        if ("POST".equalsIgnoreCase(request.getMethod().toString()) && request.getServiceRoute().trim().equalsIgnoreCase("/packages")) {
            System.out.println("DEBUG: Route matched! Calling createPackage()");
            return packageController.createPackage(request);
        }

        if ("POST".equalsIgnoreCase(request.getMethod().toString()) && request.getPathname().trim().equalsIgnoreCase("/transactions/packages")) {
            System.out.println("DEBUG: Route matched! Calling acquirePackage()");
            return packageController.acquirePackage(request);
        }

        System.out.println("DEBUG: No matching route found, returning 405.");
        return new Response(HttpStatus.METHOD_NOT_ALLOWED, ContentType.JSON,"{ \"message\": \"Method Not Allowed\" }");
    }

}



