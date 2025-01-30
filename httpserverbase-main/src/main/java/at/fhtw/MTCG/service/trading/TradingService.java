package at.fhtw.MTCG.service.trading;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.httpserver.server.Service;

public class TradingService implements Service {
    private final TradingController tradingController = new TradingController();

    @Override
    public Response handleRequest(Request request) {
        if ("GET".equalsIgnoreCase(request.getMethod().toString())) {
            return tradingController.getAllTradings(request);
        }
        if ("POST".equalsIgnoreCase(request.getMethod().toString())) {
            return tradingController.createTrading(request);
        }
        if ("DELETE".equalsIgnoreCase(request.getMethod().toString()) && request.getPathParts().size() > 1) {
            return tradingController.deleteTrading(request);
        }
        return new Response(HttpStatus.METHOD_NOT_ALLOWED, ContentType.JSON,"{ \"message\": \"Method Not Allowed\" }");
    }
}
