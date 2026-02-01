package ru.massonnn.masutils.client.features.updater;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import ru.massonnn.masutils.Masutils;
import ru.massonnn.masutils.client.utils.ModMessage;

import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class UpdateManager {
    private static final String REPO = "MassonNn/MasUtilsFabric";
    private static final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public static void updateToLatest(UpdateChannel channel, String mcVersion) {
        Masutils.LOGGER.info("Requested to download another channel latest " + channel.name() + " " + mcVersion);
        fetchReleases().thenAccept(releases -> {
            VersionInfo latest = findBestVersion(releases, channel, mcVersion, null);
            if (latest != null) {
                download(latest, success -> handleNotification(success, latest.getVersionName()));
            }
        });
    }

    public static CompletableFuture<VersionInfo> check(UpdateChannel userChannel) {
        return fetchReleases().thenApply(releases -> {
            String mcVersion = getMinecraftVersion();
            ModVersion current = new ModVersion(Masutils.VERSION);
            return findBestVersion(releases, userChannel, mcVersion, current);
        });
    }

    private static CompletableFuture<JsonArray> fetchReleases() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.github.com/repos/" + REPO + "/releases"))
                .header("User-Agent", "MasUtils-Updater")
                .header("Accept", "application/vnd.github.v3+json")
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .thenApply(response -> {
                    if (response.statusCode() != 200) return new JsonArray();
                    return JsonParser.parseString(response.body()).getAsJsonArray();
                });
    }

    private static VersionInfo findBestVersion(JsonArray releases, UpdateChannel userChannel, String mcVersion, ModVersion currentLimit) {
        VersionInfo bestMatch = null;
        ModVersion bestVersion = null;

        for (int i = 0; i < releases.size(); i++) {
            JsonObject release = releases.get(i).getAsJsonObject();
            String tag = release.get("tag_name").getAsString();
            ModVersion found = new ModVersion(tag);

            if (found.getChannel().ordinal() < userChannel.ordinal()) continue;

            if (currentLimit != null && found.compareTo(currentLimit) <= 0) continue;

            JsonArray assets = release.getAsJsonArray("assets");
            for (int j = 0; j < assets.size(); j++) {
                JsonObject asset = assets.get(j).getAsJsonObject();
                String fileName = asset.get("name").getAsString();

                if (isValidJar(fileName, mcVersion)) {
                    if (bestVersion == null || found.compareTo(bestVersion) > 0) {
                        bestVersion = found;
                        bestMatch = new VersionInfo(
                                tag,
                                asset.get("browser_download_url").getAsString(),
                                release.has("body") ? release.get("body").getAsString() : ""
                        );
                    }
                }
            }
        }
        return bestMatch;
    }

    private static boolean isValidJar(String fileName, String mcVersion) {
        return fileName.endsWith(".jar") &&
                !fileName.contains("-sources") &&
                !fileName.contains("-dev") &&
                fileName.contains("+" + mcVersion);
    }

    public static CompletableFuture<Path> download(VersionInfo info, Consumer<Boolean> callback) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path modsFolder = FabricLoader.getInstance().getGameDir().resolve("mods");
                Path currentPath = Masutils.MOD_CONTAINER.getOrigin().getPaths().getFirst();

                String oldFileName = currentPath.getFileName().toString();
                String disabledName = oldFileName.replace(".jar", ".delete-it");
                Path disabledPath = currentPath.resolveSibling(disabledName);

                try {
                    Files.move(currentPath, disabledPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    Masutils.LOGGER.warn("Failed to rename old jar, it might be locked");
                }

                disabledPath.toFile().deleteOnExit();

                String url = info.getDownloadUrl();
                String rawFileName = url.substring(url.lastIndexOf('/') + 1);
                String fileName = URLDecoder.decode(rawFileName, StandardCharsets.UTF_8);
                Path targetPath = modsFolder.resolve(fileName);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("User-Agent", "MasUtils-Updater")
                        .build();

                HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

                if (response.statusCode() == 200) {
                    try (InputStream is = response.body()) {
                        Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                    callback.accept(true);
                    return targetPath;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            callback.accept(false);
            return null;
        });
    }

    public static void checkAndDownload(UpdateChannel channel) {
        check(channel).thenAccept(info -> {
            if (info != null) {
                download(info, success -> handleNotification(success, info.getVersionName()));
            }
        });
    }

    private static void handleNotification(boolean success, String version) {
        MinecraftClient.getInstance().execute(() -> {
            if (success) {
                ModMessage.sendModMessage(Text.translatable("masutils.update.success", version));
            } else {
                ModMessage.sendErrorMessage(Text.translatable("masutils.update.failed", version));
            }
        });
    }

    private static String getMinecraftVersion() {
        return FabricLoader.getInstance().getModContainer("minecraft")
                .get().getMetadata().getVersion().getFriendlyString();
    }
}