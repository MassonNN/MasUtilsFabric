package ru.massonnn.masutils.client.telemetry;

import ru.massonnn.masutils.Masutils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TelemetryManager {
    private static final URI api = URI.create("https://api.masutils.massonnn.ru/v1/telemetry");

    public static void sendTelemetry() {
        try {
            TelemetryData data = new TelemetryData();
            data.collectTelemetry();
            String dataDump = Masutils.GSON.toJson(data);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(api)
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "MasUtils-Mod")
                    .POST(HttpRequest.BodyPublishers.ofString(dataDump))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::statusCode)
                    .thenAccept(code -> {
                        if (code == 200 || code == 201) {
                            Masutils.LOGGER.info("Telemetry sent successfully!");
                        } else {
                            Masutils.LOGGER.warn("Server returned error code: " + code);
                        }
                    })
                    .exceptionally(ex -> {
                        Masutils.LOGGER.error("HTTP Request failed", ex);
                        return null;
                    });
        } catch (Exception e) {
            Masutils.LOGGER.error("Error at sending telemetry: " + e.getMessage());
        }

    }
}
