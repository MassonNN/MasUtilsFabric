package ru.massonnn.masutils.client.features.updater;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import ru.massonnn.masutils.Masutils;
import ru.massonnn.masutils.client.utils.ModMessage;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class UpdateManager {

    private static final String REPO = "MassonNn/MasUtilsFabric";

    private static final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public static CompletableFuture<VersionInfo> check(UpdateChannel userChannel) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.github.com/repos/" + REPO + "/releases"))
                .header("User-Agent", "MasUtils-Updater")
                .header("Accept", "application/vnd.github.v3+json")
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .thenApply(response -> {
                    if (response.statusCode() != 200) return null;

                    JsonArray releases = JsonParser.parseString(response.body()).getAsJsonArray();
                    ModVersion current = new ModVersion(Masutils.VERSION);

                    for (int i = 0; i < releases.size(); i++) {
                        JsonObject release = releases.get(i).getAsJsonObject();
                        String tag = release.get("tag_name").getAsString();
                        ModVersion found = new ModVersion(tag);

                        if (found.getChannel().ordinal() < userChannel.ordinal()) continue;

                        if (found.compareTo(current) > 0) {
                            JsonArray assets = release.getAsJsonArray("assets");
                            for (int j = 0; j < assets.size(); j++) {
                                JsonObject asset = assets.get(j).getAsJsonObject();
                                String fileName = asset.get("name").getAsString();

                                if (fileName.endsWith(".jar") && !fileName.contains("-sources") && !fileName.contains("-dev")) {
                                    return new VersionInfo(
                                            tag,
                                            asset.get("browser_download_url").getAsString(),
                                            release.has("body") ? release.get("body").getAsString() : ""
                                    );
                                }
                            }
                        }
                    }
                    return null;
                });
    }


    public static CompletableFuture<Path> download(VersionInfo info, Consumer<Boolean> callback) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path modsFolder = FabricLoader.getInstance().getGameDir().resolve("mods");

                Optional<ModContainer> container = FabricLoader.getInstance().getModContainer("masutils");
                Path targetPath = getPath(info, container, modsFolder);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(info.getDownloadUrl()))
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

    private static @NotNull Path getPath(VersionInfo info, Optional<ModContainer> container, Path modsFolder) {
        if (container.isPresent()) {
            Path currentPath = container.get().getOrigin().getPaths().get(0);

            File currentFile = currentPath.toFile();

            if (currentFile.exists() && currentFile.getName().endsWith(".jar")) {
                currentFile.deleteOnExit();
            }
        }

        String newFileName = "MasUtils-" + info.getVersionName() + ".jar";
        Path targetPath = modsFolder.resolve(newFileName);
        return targetPath;
    }

    public static void checkAndDownload(UpdateChannel channel) {
        check(channel).thenAccept(info -> {
            if (info != null) {
                download(info, success -> {
                    if (success) {
                        ModMessage.sendModMessage(Text.translatable("masutils.update.success", info.getVersionName()));
                    } else {
                        ModMessage.sendErrorMessage(Text.translatable("masutils.update.failed", info.getVersionName()));
                    }
                });
            }
        });
    }
}