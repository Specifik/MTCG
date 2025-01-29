package at.fhtw.MTCG.service.packages;

import at.fhtw.MTCG.dal.UnitOfWork;
import at.fhtw.MTCG.dal.repository.PackageRepository;
import at.fhtw.MTCG.model.Package;
import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PackageController {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Response createPackage(Request request) {
        try (UnitOfWork unitOfWork = new UnitOfWork()) {
            Package cardPackage = objectMapper.readValue(request.getBody(), Package.class);
            PackageRepository packageRepository = new PackageRepository(unitOfWork);

            boolean success = packageRepository.createPackage(cardPackage);
            if (success) {
                unitOfWork.commitTransaction();
                return new Response(HttpStatus.CREATED, ContentType.JSON, "{ \"message\": \"Package created successfully\" }");
            } else {
                return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{ \"message\": \"Failed to create package\" }");
            }
        } catch (JsonProcessingException e) {
            return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{ \"message\": \"Invalid JSON format\" }");
        } catch (Exception e) {
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{ \"message\": \"Internal Server Error\" }");
        }
    }
}
