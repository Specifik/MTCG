package at.fhtw.MTCG.service.battle;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.httpserver.server.Service;

public class BattleService implements Service {
    private final BattleController battleController = new BattleController();

    @Override
    public Response handleRequest(Request request) {
        if ("POST".equalsIgnoreCase(request.getMethod().toString())) {
            return battleController.startBattle(request);
        }
        return new Response(HttpStatus.METHOD_NOT_ALLOWED, ContentType.JSON,"{ \"message\": \"Method Not Allowed\" }");
    }
}
