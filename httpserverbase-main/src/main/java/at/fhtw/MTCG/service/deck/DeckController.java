package at.fhtw.MTCG.service.deck;

import at.fhtw.MTCG.dal.UnitOfWork;
import at.fhtw.MTCG.dal.repository.DeckRepository;
import at.fhtw.MTCG.dal.repository.UserRepository;
import at.fhtw.MTCG.model.Deck;
import at.fhtw.MTCG.model.User;
import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;

public class DeckController {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Response getDeck(Request request) {
        String token = request.getHeaderMap().getHeader("Authorization");
        if (token == null) {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{ \"message\": \"Missing authentication token\" }");
        }

        try (UnitOfWork unitOfWork = new UnitOfWork()) {
            UserRepository userRepository = new UserRepository(unitOfWork);
            DeckRepository deckRepository = new DeckRepository(unitOfWork);

            User user = userRepository.findUserByToken(token);
            if (user == null) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{ \"message\": \"Invalid token\" }");
            }

            Deck deck = deckRepository.getUserDeck(user.getId());
            return new Response(HttpStatus.OK, ContentType.JSON, objectMapper.writeValueAsString(deck));
        } catch (Exception e) {
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{ \"message\": \"Internal Server Error\" }");
        }
    }

    public Response updateDeck(Request request) {
        System.out.println("DEBUG: PUT /deck wurde im DeckController aufgerufen!");

        String token = request.getHeaderMap().getHeader("Authorization");
        if (token == null) {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{ \"message\": \"Missing authentication token\" }");
        }

        try (UnitOfWork unitOfWork = new UnitOfWork()) {
            UserRepository userRepository = new UserRepository(unitOfWork);
            DeckRepository deckRepository = new DeckRepository(unitOfWork);

            User user = userRepository.findUserByToken(token);
            if (user == null) {
                return new Response(HttpStatus.UNAUTHORIZED, ContentType.JSON, "{ \"message\": \"Invalid token\" }");
            }
            System.out.println("DEBUG: Received User ID - " + user.getId());

            ObjectMapper objectMapper = new ObjectMapper();
            List<UUID> cardIds;

            try {
                // JSON direkt als Liste verarbeiten (["id1", "id2", "id3", "id4"])
                cardIds = objectMapper.readValue(request.getBody(), new TypeReference<List<UUID>>() {});
                System.out.println("DEBUG: JSON-Format als Array erkannt.");
            } catch (Exception e) {
                // Falls Fehler, versuche das Format {"cardIds": [...]}
                try {
                    JsonNode rootNode = objectMapper.readTree(request.getBody());
                    JsonNode cardIdsNode = rootNode.get("cardIds");
                    if (cardIdsNode == null || !cardIdsNode.isArray()) {
                        return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{ \"message\": \"Invalid JSON format\" }");
                    }
                    cardIds = objectMapper.readValue(cardIdsNode.toString(), new TypeReference<List<UUID>>() {});
                    System.out.println("DEBUG: JSON-Format mit cardIds-Array erkannt.");
                } catch (Exception ex) {
                    return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{ \"message\": \"Invalid JSON format\" }");
                }
            }

            // Prüfen, ob genau 4 Karten gesetzt werden
            if (cardIds.size() != 4) {
                return new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{ \"message\": \"A deck must contain exactly 4 cards.\" }");
            }

            boolean success = deckRepository.updateDeck(user.getId(), cardIds);

            return success
                    ? new Response(HttpStatus.OK, ContentType.JSON, "{ \"message\": \"Deck updated successfully\" }")
                    : new Response(HttpStatus.BAD_REQUEST, ContentType.JSON, "{ \"message\": \"Could not update deck\" }");
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{ \"message\": \"Internal Server Error\" }");
        }
    }

}

