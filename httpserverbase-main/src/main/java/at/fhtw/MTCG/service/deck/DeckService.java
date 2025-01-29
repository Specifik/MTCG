package at.fhtw.MTCG.service.deck;

import at.fhtw.MTCG.service.deck.DeckController;
import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.httpserver.server.Service;

public class DeckService implements Service {
    private final DeckController deckController = new DeckController();

    @Override
    public Response handleRequest(Request request) {
        if ("GET".equalsIgnoreCase(request.getMethod().toString())) {
            return deckController.getDeck(request);
        }
        if ("PUT".equalsIgnoreCase(request.getMethod().toString())) {
            return deckController.updateDeck(request);
        }
        return new Response(HttpStatus.METHOD_NOT_ALLOWED, ContentType.JSON,"{ \"message\": \"Method Not Allowed\" }");
    }
}
