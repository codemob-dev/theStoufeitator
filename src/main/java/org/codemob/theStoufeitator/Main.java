package org.codemob.theStoufeitator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class Main extends JavaPlugin {
    public static UUID netherGodUUID;
    public static UUID copperMayorUUID;
    public static UUID sculkGodUUID;
    public static UUID jungleRulerUUID;
    public static Random random = new Random();
    public static final boolean doUpdates = true;
    public static final boolean forceResourcePack = true;
    public Updater updater = new Updater(this);

    public ArrayList<Grapple> grapples = new ArrayList<>();

    public String resourcePackURL = "https://github.com/commandblox/theStoufeitator/releases/download/v1.1.11/Server_pack.zip";

    public byte[] resourcePackHash;

    public static float unsignedSmoothClip(float val, float max, float i) {
        return (float) (-Math.pow(1/i, val) + 1) * max;
    }

    public static void checkUUIDs(Player player) {
        switch (player.getName()) {
            case "Dogoo_Dogster" -> Main.netherGodUUID   = player.getUniqueId();
            case "Kitty_Katster" -> Main.copperMayorUUID = player.getUniqueId();
            case "Codemob"       -> Main.sculkGodUUID    = player.getUniqueId();
            case "bethebean"     -> Main.jungleRulerUUID = player.getUniqueId();
        }
    }

    @Override
    public void onLoad() {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.setResourcePack(resourcePackURL, resourcePackHash, forceResourcePack);

            checkUUIDs(onlinePlayer);
        }

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
        Bukkit.getScheduler().runTaskTimer(this, new TickRunnable(this), 0L, 1L);
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

    @Override
    public void onDisable() {
        for (Grapple grapple : grapples) {
            grapple.remove();
        }
    }

    public static void fill(Location loc1in, Location loc2in, Material material) {
        fill(loc1in, loc2in, material, material.createBlockData());
    }

    public static void fill(Location loc1in, Location loc2in, Material material, BlockData blockData) {
        Location loc1 = new Location(loc1in.getWorld(),
                Math.min(loc1in.getBlockX(), loc2in.getBlockX()),
                Math.min(loc1in.getBlockY(), loc2in.getBlockY()),
                Math.min(loc1in.getBlockZ(), loc2in.getBlockZ()));

        Location loc2 = new Location(loc1in.getWorld(),
                Math.max(loc1in.getBlockX(), loc2in.getBlockX()),
                Math.max(loc1in.getBlockY(), loc2in.getBlockY()),
                Math.max(loc1in.getBlockZ(), loc2in.getBlockZ()));

        for (int x = loc1.getBlockX(); x <= loc2.getBlockX(); x++) {
            for (int y = loc1.getBlockY(); y <= loc2.getBlockY(); y++) {
                for (int z = loc1.getBlockZ(); z <= loc2.getBlockZ(); z++) {
                    Block block = loc1.getWorld().getBlockAt(x, y, z);
                    block.setType(material);
                    block.setBlockData(blockData);
                }
            }
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

    public static boolean replaceSculk(Location location) {
        return replaceSculkable(location, Material.SCULK);
    }

    public static boolean replaceSculkable(Location location, Material material) {
        if (Tag.SCULK_REPLACEABLE.isTagged(location.getBlock().getType())) {
            location.getBlock().setType(material);
            return true;
        }
        return false;
    }
}
