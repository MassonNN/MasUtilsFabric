package ru.massonnn.masutils.client.telemetry;

import net.minecraft.client.MinecraftClient;
import ru.massonnn.masutils.Masutils;
import ru.massonnn.masutils.client.utils.SecretKeyUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class ErrorManager {
    private static final URI api = URI.create("https://api.masutils.massonnn.ru/v1/error");
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final int POW_DIFFICULTY = 3;

    public static class Error {
        String message;
        HashMap<Object, Object> locals;
        String actionContext;
        String playerName;

        public Error(String message, HashMap<Object, Object> locals, String actionContext, String playerName) {
            this.message = message;
            this.locals = locals;
            this.actionContext = actionContext;
            this.playerName = playerName;
        }
    }

    public static void sendError(String message, HashMap<Object, Object> locals) {
        CompletableFuture.runAsync(() -> {
            try {
                String pName = MinecraftClient.getInstance().player != null ?
                        MinecraftClient.getInstance().player.getName().getString() : "Unknown";

                Error error = new Error(message, locals, ActionCollector.prepareJson(), pName);
                String dataDump = Masutils.GSON.toJson(error);

                long timestamp = Instant.now().getEpochSecond();
                String hwid = SecretKeyUtils.getHardwareUUID();

                long nonce = SecretKeyUtils.solvePoW(dataDump, hwid, timestamp, POW_DIFFICULTY);
                String signature = SecretKeyUtils.generateSignature(dataDump, nonce, timestamp, hwid);

                ErrorPacket packet = new ErrorPacket(dataDump, hwid, nonce, signature, timestamp);
                String finalJson = Masutils.GSON.toJson(packet);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(api)
                        .header("Content-Type", "application/json")
                        .header("User-Agent", "MasUtils-Mod")
                        .POST(HttpRequest.BodyPublishers.ofString(finalJson))
                        .build();

                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(res -> {
                            if (res.statusCode() >= 200 && res.statusCode() < 300) {
                                Masutils.LOGGER.info("Error report sent successfully!");
                            } else {
                                Masutils.LOGGER.warn("Server returned " + res.statusCode() + ": " + res.body());
                            }
                        });

            } catch (Exception e) {
                Masutils.LOGGER.error("Cant send error: " + e.getMessage());
            }
        });
    }

    private static class ErrorPacket {
        private final String data;
        private final String hwid;
        private final long nonce;
        private final String signature;
        private final long timestamp;

        public ErrorPacket(String data, String hwid, long nonce, String signature, long timestamp) {
            this.data = data;
            this.hwid = hwid;
            this.nonce = nonce;
            this.signature = signature;
            this.timestamp = timestamp;
        }
    }
}