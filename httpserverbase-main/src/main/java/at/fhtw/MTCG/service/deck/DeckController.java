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
            System.out.println("DEBUG: Raw Request Body - " + request.getBody());

            Deck deckRequest = objectMapper.readValue(request.getBody(), Deck.class);
            List<UUID> cardIds = deckRequest.getCardIds();

            System.out.println("DEBUG: Parsed cardIds - " + cardIds);

            boolean success = deckRepository.updateDeck(user.getId(), cardIds);
            unitOfWork.commitTransaction();

            return new Response(HttpStatus.OK, ContentType.JSON, "{ \"message\": \"Deck updated successfully\" }");
        } catch (Exception e) {
            e.printStackTrace(); // Stacktrace in der Konsole anzeigen
            return new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "{ \"message\": \"Internal Server Error\" }");
        }
    }

}

