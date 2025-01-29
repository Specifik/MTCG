package at.fhtw.MTCG.service.packages;

import at.fhtw.MTCG.dal.UnitOfWork;
import at.fhtw.MTCG.dal.repository.PackageRepository;
import at.fhtw.MTCG.dal.repository.UserRepository;
import at.fhtw.MTCG.model.Package;
import at.fhtw.MTCG.model.User;
import at.fhtw.MTCG.model.Card;
import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class PackageController {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Response createPackage(Request request) {
        try (UnitOfWork unitOfWork = new UnitOfWork()) {
            System.out.println("DEBUG: Raw Request Body - " + request.getBody());

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

    public Response acquirePackage(Request request) {
        String token = request.getHeaderMap().getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{ \"message\": \"Missing authentication token\" }");
        }

        try (UnitOfWork unitOfWork = new UnitOfWork()) {
            UserRepository userRepository = new UserRepository(unitOfWork);
            PackageRepository packageRepository = new PackageRepository(unitOfWork);

            User user = userRepository.findUserByToken(token);
            if (user == null) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{ \"message\": \"Invalid token\" }");
            }

            if (user.getCoins() < 5) {
                return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{ \"message\": \"Not enough coins\" }");
            }

            List<Card> acquiredCards = packageRepository.acquirePackage(user.getId());
            if (acquiredCards == null || acquiredCards.isEmpty()) {
                return new Response(HttpStatus.NOT_FOUND, ContentType.JSON, "{ \"message\": \"No packages available\" }");
            }

            userRepository.updateUserCoins(user.getUsername(), user.getCoins() - 5);
            unitOfWork.commitTransaction();

            String responseBody = objectMapper.writeValueAsString(acquiredCards);
            return new Response(HttpStatus.OK, ContentType.JSON, responseBody);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{ \"message\": \"Internal Server Error\" }");
        }
    }
}
