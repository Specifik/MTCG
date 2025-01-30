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
        System.out.println("DEBUG: `handleRequest()` in TradingService aufgerufen! Methode: " + request.getMethod() + ", Route: " + request.getServiceRoute());

        if ("GET".equalsIgnoreCase(request.getMethod().toString())) {
            return tradingController.getAllTradings(request);
        }

        if ("POST".equalsIgnoreCase(request.getMethod().toString())) {
            if (request.getPathParts().size() == 1) {
                System.out.println("DEBUG: `POST /tradings` erkannt!");
                return tradingController.createTrading(request);
            }
            if (request.getPathParts().size() == 2) {
                System.out.println("DEBUG: `POST /tradings/{id}` erkannt!");
                return tradingController.tradeCard(request);
            }
        }

        if ("DELETE".equalsIgnoreCase(request.getMethod().toString()) && request.getPathParts().size() > 1) {
            System.out.println("DEBUG: `DELETE /tradings/{id}` erkannt!");
            return tradingController.deleteTrading(request);
        }

        return new Response(HttpStatus.METHOD_NOT_ALLOWED, ContentType.JSON,"{ \"message\": \"Method Not Allowed\" }");
    }
}
