package org.codemob.theStoufeitator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class Updater {
    public String address = "https://api.github.com/repos/commandblox/theStoufeitator/releases/latest";
    public Plugin plugin;

    public Updater(Plugin plugin) {
        this.plugin = plugin;
    }

    public String getVersionInfo() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder(URI.create(address)).header("accept", "application/json").GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public void update(boolean restart) throws IOException, InterruptedException, NullPointerException {
        Bukkit.getLogger().info("Retrieving version info...");
        JsonElement json = JsonParser.parseString(getVersionInfo());
        JsonObject jsonObject = json.getAsJsonObject();

        try {
            if (jsonObject.has("message")) {
                Bukkit.getLogger().warning(jsonObject.get("message").getAsString());
            } else {
                for (JsonElement asset : jsonObject.getAsJsonArray("assets")) Bukkit.getLogger().info(asset.getAsJsonObject().get("content_type").getAsString());

                String releaseVersion = jsonObject.get("tag_name").getAsString();
                if (!releaseVersion.equals("v" + plugin.getDescription().getVersion())) {
                    Bukkit.getLogger().info("Updating to version %s from version v%s".formatted(releaseVersion, plugin.getDescription().getVersion()));

                    int jarIndex = 0;
                    int i = 0;
                    for (JsonElement asset : jsonObject.getAsJsonArray("assets")) {
                        if (asset.getAsJsonObject().get("content_type").getAsString().equals("application/octet-stream")) {
                            jarIndex = i;
                            break;
                        }
                        i++;
                    }
                    String releaseUrl = jsonObject.getAsJsonArray("assets").get(jarIndex).getAsJsonObject().get("browser_download_url").getAsString();
                    String releaseName = jsonObject.getAsJsonArray("assets").get(jarIndex).getAsJsonObject().get("name").getAsString();
                    File updateFile = new File(Bukkit.getUpdateFolderFile() + File.separator + releaseName);
                    updateFile.getParentFile().mkdir();


                    Bukkit.getLogger().info("Downloading update from %s".formatted(releaseUrl));
                    URL releaseAddress = new URL(releaseUrl);

                    try (InputStream inputStream = releaseAddress.openStream(); ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream); FileOutputStream fileOutputStream = new FileOutputStream(updateFile)) {
                        fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, 1 << 24);
                    }

                    Bukkit.getLogger().info("Successfully updated!");
                    if (restart) {
                        Bukkit.getLogger().info("Restarting...");
                        Bukkit.getServer().spigot().restart();
                    } else {
                        Bukkit.getLogger().info("Reloading...");
                        Bukkit.reload();
                    }
                    Bukkit.broadcastMessage("%s%sUpdated to version %s!".formatted(ChatColor.BLUE, ChatColor.BOLD, releaseVersion));
                }
            }
        } catch (NullPointerException e) {
            Bukkit.getLogger().warning(jsonObject.toString());
            throw e;
        }


    }
}
