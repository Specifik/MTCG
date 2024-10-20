package at.fhtw.MTCG.service.weather;

import at.fhtw.httpserver.http.ContentType;
import at.fhtw.httpserver.http.HttpStatus;
import at.fhtw.httpserver.server.Request;
import at.fhtw.httpserver.server.Response;
import at.fhtw.MTCG.controller.Controller;
import at.fhtw.MTCG.dal.UnitOfWork;
import at.fhtw.MTCG.dal.repository.WeatherRepository;
import at.fhtw.MTCG.model.Weather;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Collection;
import java.util.List;

public class WeatherController extends Controller {
    private WeatherDummyDAL weatherDAL;

    public WeatherController() {

        // Nur noch für die Dummy-JUnit-Tests notwendig. Stattdessen ein RepositoryPattern verwenden.
        this.weatherDAL = new WeatherDummyDAL();
    }

    // GET /weather(:id
    public Response getWeather(String id)
    {
        try {
            Weather weatherData = this.weatherDAL.getWeather(Integer.parseInt(id));
            // "[ { \"id\": 1, \"city\": \"Vienna\", \"temperature\": 9.0 }, { ... }, { ... } ]"
            String weatherDataJSON = this.getObjectMapper().writeValueAsString(weatherData);

            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    weatherDataJSON
            );
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.JSON,
                    "{ \"message\" : \"Internal Server Error\" }"
            );
        }
    }

    // GET /weather
    public Response getWeather() {
        try {
            List weatherData = this.weatherDAL.getWeather();
            // "[ { \"id\": 1, \"city\": \"Vienna\", \"temperature\": 9.0 }, { ... }, { ... } ]"
            String weatherDataJSON = this.getObjectMapper().writeValueAsString(weatherData);

            return new Response(
                HttpStatus.OK,
                ContentType.JSON,
                weatherDataJSON
            );
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new Response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ContentType.JSON,
                "{ \"message\" : \"Internal Server Error\" }"
            );
        }
    }

    // POST /weather
    public Response addWeather(Request request) {
        try {

            // request.getBody() => "{ \"id\": 4, \"city\": \"Graz\", ... }
            Weather weather = this.getObjectMapper().readValue(request.getBody(), Weather.class);
            this.weatherDAL.addWeather(weather);

            return new Response(
                HttpStatus.CREATED,
                ContentType.JSON,
                "{ message: \"Success\" }"
            );
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return new Response(
            HttpStatus.INTERNAL_SERVER_ERROR,
            ContentType.JSON,
            "{ \"message\" : \"Internal Server Error\" }"
        );
    }

    // GET /weather
    // gleich wie "public Response getWeather()" nur mittels Repository
    public Response getWeatherPerRepository() {
        UnitOfWork unitOfWork = new UnitOfWork();
        try (unitOfWork){
            Collection<Weather> weatherData = new WeatherRepository(unitOfWork).findAllWeather();

            // "[ { \"id\": 1, \"city\": \"Vienna\", \"temperature\": 9.0 }, { ... }, { ... } ]"
            String weatherDataJSON = this.getObjectMapper().writeValueAsString(weatherData);
            unitOfWork.commitTransaction();
            return new Response(
                    HttpStatus.OK,
                    ContentType.JSON,
                    weatherDataJSON
            );
        } catch (Exception e) {
            e.printStackTrace();

            unitOfWork.rollbackTransaction();
            return new Response(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ContentType.JSON,
                    "{ \"message\" : \"Internal Server Error\" }"
            );
        }
    }
}
