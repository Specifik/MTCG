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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class PackageController {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Response createPackage(Request request) {
        System.out.println("DEBUG: `POST /packages` wurde im PackageController aufgerufen!");

        String token = request.getHeaderMap().getHeader("Authorization");
        if (token == null) {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{ \"message\": \"Missing authentication token\" }");
        }

        try (UnitOfWork unitOfWork = new UnitOfWork()) {
            UserRepository userRepository = new UserRepository(unitOfWork);
            PackageRepository packageRepository = new PackageRepository(unitOfWork);

            User user = userRepository.findUserByToken(token);
            if (user == null || !user.getUsername().equals("admin")) {
                return new Response(HttpStatus.FORBIDDEN, ContentType.JSON, "{ \"message\": \"Only admin can create packages\" }");
            }

            ObjectMapper objectMapper = new ObjectMapper();
            List<Card> cards;

            try {
                // Debugging: Loggen, was `cURL` tatsächlich sendet
                System.out.println("DEBUG: Raw Request Body - " + request.getBody());

                // JSON direkt als Liste verarbeiten (cURL-Format)
                cards = objectMapper.readValue(request.getBody(), new TypeReference<List<Card>>() {});
                System.out.println("DEBUG: JSON-Format als Liste erkannt.");
            } catch (Exception e) {
                // Falls Fehler, versuche das Format mit "cards": [...]
                try {
                    JsonNode rootNode = objectMapper.readTree(request.getBody());
                    JsonNode cardsNode = rootNode.get("cards");
                    if (cardsNode == null || !cardsNode.isArray()) {
                        return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{ \"message\": \"Invalid JSON format\" }");
                    }
                    cards = objectMapper.readValue(cardsNode.toString(), new TypeReference<List<Card>>() {});
                    System.out.println("DEBUG: JSON-Format mit \"cards\": [] erkannt.");
                } catch (Exception ex) {
                    return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{ \"message\": \"Invalid JSON format\" }");
                }
            }

            if (cards.size() != 5) {
                return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{ \"message\": \"A package must contain exactly 5 cards\" }");
            }

            boolean success = packageRepository.createPackage(new Package(cards));

            return success
                    ? new Response(HttpStatus.CREATED, ContentType.JSON, "{ \"message\": \"Package created successfully\" }")
                    : new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{ \"message\": \"Could not create package\" }");
        } catch (Exception e) {
            e.printStackTrace();
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
