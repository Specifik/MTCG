package at.fhtw.MTCG.service.stats;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.httpserver.server.Service;

public class StatsService implements Service {
    private final StatsController statsController = new StatsController();

    @Override
    public Response handleRequest(Request request) {

        if ("GET".equalsIgnoreCase(request.getMethod().toString())) {
            return statsController.getStats(request);
        }

        return new Response(HttpStatus.METHOD_NOT_ALLOWED, ContentType.JSON, "{ \"message\": \"Method Not Allowed\" }");
    }
}
