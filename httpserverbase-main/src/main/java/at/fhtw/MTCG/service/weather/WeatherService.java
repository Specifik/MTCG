package at.fhtw.MTCG.service.weather;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.http.Method;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.httpserver.server.Service;

public class WeatherService implements Service {
    private final WeatherController weatherController;

    public WeatherService() {
        this.weatherController = new WeatherController();
    }

    @Override
    public Response handleRequest(Request request) {
        if (request.getMethod() == Method.GET &&
            request.getPathParts().size() > 1) {
            return this.weatherController.getWeather(request.getPathParts().get(1));
        } else if (request.getMethod() == Method.GET) {
            return this.weatherController.getWeatherPerRepository();
            //return this.weatherController.getWeatherPerRepository();
        } else if (request.getMethod() == Method.POST) {
            return this.weatherController.addWeather(request);
        }

        return new Response(
                HttpStatus.BAD_REQUEST,
                ContentType.JSON,
                "[weather]"
        );
    }

}
