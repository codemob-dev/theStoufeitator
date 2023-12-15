package org.codemob.theStoufeitator;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.UUID;

public class Main extends JavaPlugin {
    public static UUID netherGodUUID;
    public static UUID copperMayorUUID;
    public static UUID sculkGodUUID;
    public static Random random = new Random();

    public static final boolean doUpdates = true;
    public Updater updater = new Updater(this);

    public String resourcePackURL = "https://github.com/commandblox/theStoufeitator/releases/download/v1.1.2/theStoufeitator.jar";

    public byte[] resourcePackHash;

    @Override
    public void onLoad() {
        File temp = new File(System.getProperty("java.io.tmpdir"));
        File downloadLocation = new File(temp.getPath() + File.separator + "resourcePack.zip");

        try (InputStream inputStream = new URL(resourcePackURL).openStream(); ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);
             FileOutputStream fileOutputStream = new FileOutputStream(downloadLocation)) {
            fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, 1 << 24);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            resourcePackHash = createSha1(downloadLocation);
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        downloadLocation.delete();

        if (doUpdates) {
            try {
                updater.update(false);
            } catch (IOException | InterruptedException | NullPointerException e) {
                Bukkit.getLogger().warning("Update failed! If this warning continues, contact Codemob.");
            }
        }
    }
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new MainListener(this), this);
        Bukkit.getScheduler().runTaskTimer(this, new TickRunnable(), 0L, 1L);
        if (doUpdates) {
            Bukkit.getScheduler().runTaskTimer(this, () -> {
                try {
                    updater.update(false);
                } catch (IOException | InterruptedException | NullPointerException e) {
                    Bukkit.getLogger().warning("Update failed! If this warning continues, contact Codemob.");
                }
            }, 0L, 2400L);
        }
    }



    public static byte[] createSha1(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        try (InputStream fis = new FileInputStream(file)) {
            int n = 0;
            byte[] buffer = new byte[8192];
            while (n != -1) {
                n = fis.read(buffer);
                if (n > 0) {
                    digest.update(buffer, 0, n);
                }
            }
        }
        return digest.digest();
    }
}
