import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

void main(String[] args) {
    var client = HttpClient.newHttpClient();
    var bucharestWeather = "https://api.open-meteo.com/v1/forecast?latitude=44.4323&longitude=26.1063&current_weather=true";
    var amsterdamWeather = "https://api.open-meteo.com/v1/forecast?latitude=52.377956&longitude=4.897070&current_weather=true";
    // var newyorkWeather = "https://api.open-meteo.com/v1/forecast?latitude=40.7143&longitude=-74.006&current_weather=true";

    var futureWeatherBucharest = sendRequest(client, bucharestWeather);
    var futureWeatherAmsterdam = sendRequest(client, amsterdamWeather);

    var combinedFutureWeather = CompletableFuture.allOf(futureWeatherBucharest, futureWeatherAmsterdam);

    combinedFutureWeather.thenRun(() -> {
        try {
            var temperatureBucharest = getTemperature(futureWeatherBucharest.get());
            var temperatureAmsterdam = getTemperature(futureWeatherAmsterdam.get());

            System.out.println("Temperature at Bucharest: " + temperatureBucharest);
            System.out.println("Temperature at Amsterdam: " + temperatureAmsterdam);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }).join();
}

CompletableFuture<String> sendRequest(HttpClient client, String url) {
    var request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .build();

    return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(HttpResponse::body);
}

String getTemperature(String body) {
    var objectMapper = new ObjectMapper();
    try {
        var rootNode = objectMapper.readTree(body);
        var weatherNode = rootNode.path("current_weather");

        return weatherNode.path("temperature").asText();
    } catch (Exception e) {
        e.printStackTrace();
    }
    return "Cannot parse the temperature";
}

