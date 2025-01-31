package at.fhtw.MTCG.service.packages;

import at.fhtw.MTCG.dal.UnitOfWork;
import at.fhtw.MTCG.dal.repository.PackageRepository;
import at.fhtw.MTCG.dal.repository.UserRepository;
import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.httpserver.server.Service;

public class PackageService implements Service {
    private final PackageController packageController;

    public PackageService() {
        UnitOfWork unitOfWork = new UnitOfWork();
        UserRepository userRepository = new UserRepository(unitOfWork);
        PackageRepository packageRepository = new PackageRepository(unitOfWork);
        this.packageController = new PackageController(packageRepository, userRepository, unitOfWork);
    }

    @Override
    public Response handleRequest(Request request) {
        if ("POST".equalsIgnoreCase(request.getMethod().toString()) && request.getServiceRoute().trim().equalsIgnoreCase("/packages")) {
            return packageController.createPackage(request);
        }

        if ("POST".equalsIgnoreCase(request.getMethod().toString()) && request.getPathname().trim().equalsIgnoreCase("/transactions/packages")) {
            return packageController.acquirePackage(request);
        }

        return new Response(HttpStatus.METHOD_NOT_ALLOWED, ContentType.JSON, "{ \"message\": \"Method Not Allowed\" }");
    }
}
