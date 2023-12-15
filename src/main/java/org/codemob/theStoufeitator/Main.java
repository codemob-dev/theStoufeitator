package org.codemob.theStoufeitator;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;

public class Main extends JavaPlugin {
    public static UUID netherGodUUID;
    public static UUID copperMayorUUID;
    public static UUID sculkGodUUID;
    public static Random random = new Random();
    public Updater updater = new Updater(this);

    public Main() {

    }

    @Override
    public void onLoad() {
        try {
            updater.update(false);
        } catch (IOException | InterruptedException | NullPointerException e) {
            Bukkit.getLogger().warning("Update failed! If this warning continues, contact Codemob.");
        }
    }
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new MainListener(this), this);
        Bukkit.getScheduler().runTaskTimer(this, new TickRunnable(), 0L, 1L);
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            try {
                updater.update(false);
            } catch (IOException | InterruptedException | NullPointerException e) {
                Bukkit.getLogger().warning("Update failed! If this warning continues, contact Codemob.");
            }
        }, 0L, 1200L);
    }
}
