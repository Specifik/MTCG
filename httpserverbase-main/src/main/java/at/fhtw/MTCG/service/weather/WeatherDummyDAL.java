package at.fhtw.MTCG.service.weather;

import at.fhtw.MTCG.model.Weather;

import java.util.ArrayList;
import java.util.List;

public class WeatherDummyDAL {
    private List<Weather> weatherData;

    public WeatherDummyDAL() {
        weatherData = new ArrayList<>();
        weatherData.add(new Weather(1,"Europe","Vienna", 9.f));
        weatherData.add(new Weather(2,"Europe","Berlin", 8.f));
        weatherData.add(new Weather(3,"Asia","Tokyo", 12.f));
    }

    // GET /weather/:id
    public Weather getWeather(Integer ID) {
        Weather foundWaether = weatherData.stream()
                .filter(waether -> ID == waether.getId())
                .findAny()
                .orElse(null);

        return foundWaether;
    }

    // GET /weather
    public List<Weather> getWeather() {
        return weatherData;
    }

    // POST /weather
    public void addWeather(Weather weather) {
        weatherData.add(weather);
    }
}
