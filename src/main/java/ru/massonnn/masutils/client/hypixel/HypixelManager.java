package ru.massonnn.masutils.client.hypixel;

import com.google.gson.*;
import ru.massonnn.masutils.Masutils;
import ru.massonnn.masutils.client.hypixel.model.*;
import ru.massonnn.masutils.client.telemetry.ErrorManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class HypixelManager {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    private static final String BASE = "https://sky.shiiyu.moe/api/";

    public static CompletableFuture<SkyBlockProfile> fetchProfile(String username) {
        if (username == null || username.trim().isEmpty()) return CompletableFuture.completedFuture(null);
        String finalUsername = username.trim();

        return resolveUuid(finalUsername).thenCompose(uuid -> {
            if (uuid == null) return CompletableFuture.completedFuture(null);

            return fetchEmbed(uuid).thenCompose(embed -> {
                if (embed == null || embed.profileId == null) {
                    HashMap<Object, Object> locals = new HashMap<>();
                    locals.put("username", finalUsername);
                    ErrorManager.sendError(
                            "Cannot get profile id",
                            locals
                    );
                    return CompletableFuture.completedFuture(null);
                }

                String pid = embed.profileId;
                CompletableFuture<JsonElement> petsFut = fetchModule("pets/" + uuid + "/" + pid);
                CompletableFuture<JsonElement> dungeonsFut = fetchModule("dungeons/" + uuid + "/" + pid);
                CompletableFuture<JsonElement> accFut = fetchModule("accessories/" + uuid + "/" + pid);
                CompletableFuture<JsonElement> gearFut = fetchModule("gear/" + uuid + "/" + pid);

                return CompletableFuture.allOf(petsFut, dungeonsFut, accFut, gearFut).thenApply(v ->
                        buildProfile(embed, petsFut.join(), dungeonsFut.join(), accFut.join(), gearFut.join())
                );
            });
        }).exceptionally(ex -> {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            String message = cause.getMessage() != null ? cause.getMessage() : cause.toString();

            HashMap<Object, Object> locals = new HashMap<>();
            locals.put("username", finalUsername);
            ErrorManager.sendError(message, locals);
            return null;
        });
    }

    private static CompletableFuture<String> resolveUuid(String username) {
        return fetchJson(BASE + "uuid/" + username).thenApply(json -> {
            if (json != null && json.isJsonObject()) {
                return safeGetString(json.getAsJsonObject(), "uuid");
            }
            return null;
        });
    }

    private static CompletableFuture<EmbedInfo> fetchEmbed(String uuid) {
        return fetchJson(BASE + "embed/" + uuid).thenApply(json -> {
            if (json == null || !json.isJsonObject()) return null;
            JsonObject obj = json.getAsJsonObject();

            String profileId = safeGetString(obj, "profile_id");
            double bankValue = 0.0;

            if (obj.has("bank") && obj.get("bank").isJsonPrimitive()) {
                bankValue = obj.get("bank").getAsDouble();
            }

            return new EmbedInfo(profileId, bankValue);
        });
    }

    private static CompletableFuture<JsonElement> fetchModule(String path) {
        return fetchJson(BASE + path);
    }

    private static CompletableFuture<JsonElement> fetchJson(String url) {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0")
                .GET()
                .build();

        return CLIENT.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(resp -> {
                    if (resp.statusCode() != 200) return JsonNull.INSTANCE;
                    try {
                        return JsonParser.parseString(resp.body());
                    } catch (Exception e) {
                        return JsonNull.INSTANCE;
                    }
                });
    }

    private static SkyBlockProfile buildProfile(EmbedInfo embed, JsonElement pets, JsonElement dungeons, JsonElement accs, JsonElement gear) {
        int mp = 0;
        if (accs != null && accs.isJsonObject()) {
            JsonObject obj = accs.getAsJsonObject();
            if (obj.has("magicalPower") && obj.get("magicalPower").isJsonObject()) {
                JsonObject mpObj = obj.getAsJsonObject("magicalPower");
                if (mpObj.has("total")) {
                    mp = mpObj.get("total").getAsInt();
                }
            }
        }
        return new SkyBlockProfile(true, mp, parseDungeons(dungeons), parseItems(gear), parsePets(pets), embed.bank);
    }

    private static DungeonData parseDungeons(JsonElement json) {
        Map<String, Integer> comps = new HashMap<>();
        Map<String, Double> pbs = new HashMap<>();
        if (json == null || !json.isJsonObject()) return new DungeonData(comps, pbs);

        JsonObject root = json.getAsJsonObject();
        if (root.has("catacombs") && root.get("catacombs").isJsonArray()) {
            for (JsonElement el : root.getAsJsonArray("catacombs")) {
                if (el.isJsonObject()) {
                    JsonObject f = el.getAsJsonObject();
                    String rawName = safeGetString(f, "name");
                    if (rawName == null) continue;

                    String cleanKey = rawName.toLowerCase()
                            .replace("master mode floor ", "m")
                            .replace("floor ", "")
                            .replace("entrance", "e")
                            .trim();

                    if (f.has("stats") && f.get("stats").isJsonObject()) {
                        JsonObject s = f.getAsJsonObject("stats");
                        if (s.has("tier_completions")) {
                            comps.put(cleanKey, s.get("tier_completions").getAsInt());
                        }
                        if (s.has("fastest_time")) {
                            pbs.put(cleanKey, s.get("fastest_time").getAsDouble() / 1000.0);
                        }
                    }
                }
            }
        }
        return new DungeonData(comps, pbs);
    }

    private static ItemData parseItems(JsonElement json) {
        AtomicBoolean term = new AtomicBoolean(false), hyp = new AtomicBoolean(false), wi = new AtomicBoolean(false);
        AtomicBoolean gyro = new AtomicBoolean(false), giant = new AtomicBoolean(false), midas = new AtomicBoolean(false);

        if (json != null && json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            String[] sections = {"armor", "equipment", "wardrobe", "weapons", "inventory"};
            for (String s : sections) {
                if (obj.has(s)) {
                    JsonElement section = obj.get(s);
                    if (section.isJsonArray()) {
                        processArray(section.getAsJsonArray(), term, hyp, wi, gyro, giant, midas);
                    } else if (section.isJsonObject()) {
                        JsonObject sectionObj = section.getAsJsonObject();
                        for (Map.Entry<String, JsonElement> entry : sectionObj.entrySet()) {
                            if (entry.getValue().isJsonArray()) {
                                processArray(entry.getValue().getAsJsonArray(), term, hyp, wi, gyro, giant, midas);
                            }
                        }
                    }
                }
            }
        }
        return new ItemData(term.get(), hyp.get(), wi.get(), gyro.get(), giant.get(), midas.get());
    }

    private static void processArray(JsonArray arr, AtomicBoolean t, AtomicBoolean h, AtomicBoolean w, AtomicBoolean gy, AtomicBoolean gt, AtomicBoolean m) {
        for (JsonElement el : arr) {
            if (el == null || el.isJsonNull() || !el.isJsonObject()) continue;
            JsonObject item = el.getAsJsonObject();

            String name = safeGetString(item, "display_name");

            if (name != null) {
                String clean = name.replaceAll("§.", "").toLowerCase();

                if (clean.contains("terminator")) t.set(true);
                if (clean.contains("hyperion") || clean.contains("scylla")) h.set(true);
                if (clean.contains("gyrokinetic")) gy.set(true);
                if (clean.contains("giant's sword")) gt.set(true);
                if (clean.contains("midas")) m.set(true);
            }

            if (item.has("contains_wither_impact") && !item.get("contains_wither_impact").isJsonNull()) {
                if (item.get("contains_wither_impact").getAsBoolean()) {
                    w.set(true);
                    h.set(true);
                }
            }

            if (item.has("containsItems") && item.get("containsItems").isJsonArray()) {
                processArray(item.getAsJsonArray("containsItems"), t, h, w, gy, gt, m);
            }
        }
    }

    private static PetData parsePets(JsonElement json) {
        List<Pet> list = new ArrayList<>();
        String active = null;
        if (json != null && json.isJsonObject()) {
            JsonObject root = json.getAsJsonObject();
            if (root.has("pets") && root.get("pets").isJsonArray()) {
                for (JsonElement el : root.getAsJsonArray("pets")) {
                    if (el.isJsonObject()) {
                        JsonObject p = el.getAsJsonObject();
                        String type = safeGetString(p, "type");
                        if (type != null) {
                            list.add(new Pet(type, safeGetString(p, "tier"), p.has("level") ? p.get("level").getAsDouble() : 0));
                            if (p.has("active") && !p.get("active").isJsonNull() && p.get("active").getAsBoolean()) active = type;
                        }
                    }
                }
            }
        }
        return new PetData(active, list);
    }

    private static String safeGetString(JsonObject obj, String key) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsString();
        }
        return null;
    }

    private static class EmbedInfo {
        String profileId; Double bank;
        EmbedInfo(String p, Double b) { this.profileId = p; this.bank = b; }
    }
}