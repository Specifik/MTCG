package at.fhtw.MTCG.service.card;

import at.fhtw.MTCG.service.card.CardController;
import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.httpserver.server.Service;

public class CardService implements Service {
    private final CardController cardController = new CardController();

    @Override
    public Response handleRequest(Request request) {
        if ("GET".equalsIgnoreCase(request.getMethod().toString())) {
            return cardController.getUserCards(request);
        }
        return new Response(HttpStatus.METHOD_NOT_ALLOWED, ContentType.JSON,"{ \"message\": \"Method Not Allowed\" }");
    }
}
