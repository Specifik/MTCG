package at.fhtw.httpserver.server;

public interface Service {
    Response handleRequest(Request request);
}
