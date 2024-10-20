package at.fhtw.MTCG.service.echo;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.httpserver.server.Service;

public class EchoService implements Service {
    @Override
    public Response handleRequest(Request request) {
        return new Response(HttpStatus.OK,
                            ContentType.PLAIN_TEXT,
                     "Echo-" + request.getBody());
    }
}
