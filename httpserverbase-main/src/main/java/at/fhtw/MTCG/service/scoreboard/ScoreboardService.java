package at.fhtw.MTCG.service.scoreboard;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.httpserver.server.Service;

public class ScoreboardService implements Service {
    private final ScoreboardController scoreboardController = new ScoreboardController();

    @Override
    public Response handleRequest(Request request) {

        if ("GET".equalsIgnoreCase(request.getMethod().toString())) {
            return scoreboardController.getScoreboard(request);
        }

        return new Response(HttpStatus.METHOD_NOT_ALLOWED, ContentType.JSON, "{ \"message\": \"Method Not Allowed\" }");
    }
}
