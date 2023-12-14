package org.codemob.theStoufeitator;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.SculkCatalyst;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

public class MainListener implements Listener {
    public final Plugin mainPlugin;

    public MainListener(Plugin plugin) {
        mainPlugin = plugin;
    }


    public void onFlameBowUse(EntityShootBowEvent event) {
        if (event.getEntity().getUniqueId() == Main.netherGodUUID) {
            event.getProjectile().setMetadata("flameArrow", new FixedMetadataValue(mainPlugin, true));
            event.getEntity().getVelocity().subtract(event.getEntity().getLocation().getDirection().multiply(0.2));
        } else {
            event.getEntity().getWorld().createExplosion(event.getEntity().getLocation(), 5);
        }
    }

    public void onFlameBowProjectileHit(ProjectileHitEvent event) {
        event.getEntity().getWorld().createExplosion(event.getEntity().getLocation(), 4, true);
        event.getEntity().remove();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        switch (event.getPlayer().getName()) {
            case "Dogoo_Dogster" -> Main.netherGodUUID = event.getPlayer().getUniqueId();
            case "Kitty_Katster" -> Main.copperMayorUUID = event.getPlayer().getUniqueId();
            case "Codemob" -> Main.sculkGodUUID = event.getPlayer().getUniqueId();
        }
    }

    @EventHandler
    public void onBowUse(EntityShootBowEvent event) {
        if (Objects.nonNull(event.getBow()) && Objects.nonNull(event.getBow().getItemMeta()) && event.getBow().getItemMeta().hasCustomModelData()) {
            switch (event.getBow().getItemMeta().getCustomModelData()) {
                case 1790001 -> onFlameBowUse(event);
                case 1790002 -> onCopperBowUse(event);
                case 1790003 -> onExplosiveCopperBowUse(event);
            }
        }
    }

    private void onExplosiveCopperBowUse(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player player && player.isSneaking()) {
            event.getProjectile().setMetadata("explosiveCopperArrow", new FixedMetadataValue(mainPlugin, true));
        }
    }

    private void onCopperBowUse(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player player && player.isSneaking()) {
            event.getProjectile().setMetadata("copperArrow", new FixedMetadataValue(mainPlugin, true));
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow arrow) {
            if (arrow.hasMetadata("flameArrow")) {
                onFlameBowProjectileHit(event);
            } else if (arrow.hasMetadata("copperArrow")) {
                onCopperBowProjectileHit(event);
            } else if (arrow.hasMetadata("explosiveCopperArrow")) {
                onExplosiveCopperBowProjectileHit(event);
            }
        }
    }

    private void onExplosiveCopperBowProjectileHit(ProjectileHitEvent event) {
        Location location = event.getEntity().getLocation();

        event.getEntity().getWorld().createExplosion(location, 4, true);
        onCopperBowProjectileHit(event);

        for (int x = location.getBlockX() - 2; x <= location.getBlockX() + 2; x++) {
            for (int y = location.getBlockY() - 2; y <= location.getBlockY() + 2; y++) {
                for (int z = location.getBlockZ() - 2; z <= location.getBlockZ() + 2; z++) {

                    Location loopLoc = new Location(event.getEntity().getWorld(), x, y, z);
                    if (loopLoc.getBlock().isEmpty()) loopLoc.getBlock().setType(Material.WAXED_COPPER_BLOCK);
                }
            }
        }

        event.getEntity().remove();
    }

    private void onCopperBowProjectileHit(ProjectileHitEvent event) {
        Location location = event.getEntity().getLocation();
        for (int x = location.getBlockX() - 1; x <= location.getBlockX() + 1; x++) {
            for (int y = location.getBlockY() - 1; y <= location.getBlockY() + 1; y++) {
                for (int z = location.getBlockZ() - 1; z <= location.getBlockZ() + 1; z++) {

                    Location loopLoc = new Location(event.getEntity().getWorld(), x, y, z);
                    if (loopLoc.getBlock().isEmpty()) loopLoc.getBlock().setType(Material.WAXED_COPPER_BLOCK);
                }
            }
        }

        event.getEntity().remove();
    }

    @EventHandler
    public void onPlayerInteraction(PlayerInteractEvent event) {
        switch (event.getAction()) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> {
                if (event.hasItem() && event.getItem().getType() == Material.STICK && event.getItem().hasItemMeta() && event.getItem().getItemMeta().hasCustomModelData() && event.getItem().getItemMeta().getCustomModelData() == 1790001) {
                    Location loc = event.getPlayer().getLocation();

                    Objects.requireNonNull(loc.getWorld()).playSound(loc, Sound.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 1, 1);

                    for (int i = 0; i < 20; i++) {
                        for (Entity entity : Objects.requireNonNull(loc.getWorld()).getNearbyEntities(loc, 1.5, 1.5, 1.5)) {
                            if (entity != event.getPlayer() && entity instanceof LivingEntity livingEntity) {
                                livingEntity.damage(48, event.getPlayer());
                            }
                        }

                        if (Tag.SCULK_REPLACEABLE.isTagged(loc.getBlock().getType()) && Main.random.nextDouble() > 0.85) {
                            loc.getBlock().setType(Material.SCULK);
                        }

                        loc.getWorld().spawnParticle(Particle.SONIC_BOOM, loc, 1);
                        loc.add(loc.getDirection());
                    }
                }
            }
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> {
                if (event.hasItem() && event.getItem().getType() == Material.STICK && event.getItem().hasItemMeta() && event.getItem().getItemMeta().hasCustomModelData() && event.getItem().getItemMeta().getCustomModelData() == 1790001) {
                    Location loc = event.getPlayer().getLocation().subtract(0, 1, 0);
                    Block block = loc.getBlock();
                    if (!block.isEmpty()) {
                        block.setType(Material.SCULK_CATALYST);
                        SculkCatalyst catalyst = (SculkCatalyst) block.getState();
                        catalyst.bloom(loc.add(1, 1, 1).getBlock(), 500);
                        catalyst.bloom(loc.add(-1, 1, 1).getBlock(), 500);
                        catalyst.bloom(loc.add(1, 1, -1).getBlock(), 500);
                        catalyst.bloom(loc.add(-1, 1, -1).getBlock(), 500);
                    }
                }
            }
        }
    }
}