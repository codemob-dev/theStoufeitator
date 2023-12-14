package org.codemob.theStoufeitator;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;
import java.util.UUID;

public class Main extends JavaPlugin {
    public static UUID netherGodUUID;
    public static UUID copperMayorUUID;
    public static UUID sculkGodUUID;
    public static Random random = new Random();
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new MainListener(this), this);
        Bukkit.getScheduler().runTaskTimer(this, new TickRunnable(), 0L, 1L);
    }
}
