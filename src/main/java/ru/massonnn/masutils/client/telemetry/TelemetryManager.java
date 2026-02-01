package ru.massonnn.masutils.client.telemetry;

import ru.massonnn.masutils.Masutils;
import ru.massonnn.masutils.client.utils.SecretKeyUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class TelemetryManager {
    private static final URI api = URI.create("https://api.masutils.massonnn.ru/v1/telemetry");
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final int POW_DIFFICULTY = 4;

    public static void sendTelemetry() {
        CompletableFuture.runAsync(() -> {
            try {
                TelemetryData data = new TelemetryData();
                data.collectTelemetry();
                String dataDump = Masutils.GSON.toJson(data);

                long timestamp = java.time.Instant.now().getEpochSecond();
                String hwid = SecretKeyUtils.getHardwareUUID();

                long nonce = SecretKeyUtils.solvePoW(dataDump, hwid, timestamp, POW_DIFFICULTY);

                String signature = SecretKeyUtils.generateSignature(dataDump, nonce, timestamp, hwid);

                TelemetryPacket packet = new TelemetryPacket(dataDump, hwid, nonce, signature, timestamp);

                String finalJson = Masutils.GSON.toJson(packet);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(api)
                        .header("Content-Type", "application/json")
                        .header("User-Agent", "MasUtils-Mod")
                        .POST(HttpRequest.BodyPublishers.ofString(finalJson))
                        .build();

                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(res -> {
                            int code = res.statusCode();
                            if (code >= 200 && code < 300) {
                                Masutils.LOGGER.info("Telemetry sent successfully!");
                            } else {
                                Masutils.LOGGER.warn("Server returned error code: " + code + " Body: " + res.body());
                            }
                        })
                        .exceptionally(ex -> {
                            Masutils.LOGGER.error("HTTP Request failed", ex);
                            return null;
                        });

            } catch (Exception e) {
                Masutils.LOGGER.error("Error at preparing/sending telemetry: " + e.getMessage());
            }
        });
    }

    private static class TelemetryPacket {
        private final String data;
        private final String hwid;
        private final long nonce;
        private final String signature;
        private final long timestamp;

        public TelemetryPacket(String data, String hwid, long nonce, String signature, long timestamp) {
            this.data = data;
            this.hwid = hwid;
            this.nonce = nonce;
            this.signature = signature;
            this.timestamp = timestamp;
        }
    }
}