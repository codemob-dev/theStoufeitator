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
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MusicInstrumentMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.Objects;

public class MainListener implements Listener {
    public final Main mainPlugin;

    public MainListener(Main plugin) {
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
        event.getPlayer().setResourcePack(mainPlugin.resourcePackURL, mainPlugin.resourcePackHash, Main.forceResourcePack);

        Main.checkUUIDs(event.getPlayer());
    }

    @EventHandler
    public void onBowUse(EntityShootBowEvent event) {
        if (Objects.nonNull(event.getBow()) && Objects.nonNull(event.getBow().getItemMeta()) && event.getBow().getItemMeta().hasCustomModelData()) {
            switch (event.getBow().getType()) {
                case BOW -> {
                    switch (event.getBow().getItemMeta().getCustomModelData()) {
                        case 1790001 -> onFlameBowUse(event);
                        case 1790002 -> onCopperBowUse(event);
                        case 1790003 -> onExplosiveCopperBowUse(event);
                    }
                }
            }
        }
    }

    private void onGrappleHookUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Location eyeLocation = player.getEyeLocation();
        Arrow arrow = player.getWorld().spawnArrow(eyeLocation, eyeLocation.getDirection(), 3.6F, 1F);
        arrow.setMetadata("grappleArrow", new FixedMetadataValue(mainPlugin, true));
        Grapple grapple = new Grapple(arrow, player);
        mainPlugin.grapples.add(grapple);
        ItemMeta meta = event.getItem().getItemMeta();
        meta.setCustomModelData(1790003);
        event.getItem().setItemMeta(meta);
    }

    private void onGrapplePullBackTick(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        int playerGrapples = 0;
        for (Grapple grapple : mainPlugin.grapples) {
            if (grapple.player == player) {
                float distanceChange;
                if (player.isSneaking()) {
                    distanceChange = 1;
                } else {
                    distanceChange = grapple.maxDistance * -0.02F - 1;
                    distanceChange = Math.max(-2.5F, distanceChange);
                }

                grapple.maxDistance += distanceChange;
                grapple.maxDistance = Math.max(2.5F, grapple.maxDistance);
                grapple.maxDistance = Math.min(grapple.absoluteMaxDistance, grapple.maxDistance);

                playerGrapples ++;
            }
        }
        if (playerGrapples == 0) {
            PlayerInventory playerInventory = player.getInventory();
            if (player.getGameMode() == GameMode.CREATIVE) {
                ItemMeta meta = event.getItem().getItemMeta();
                meta.setCustomModelData(1790004);
                event.getItem().setItemMeta(meta);
            } else if (playerInventory.contains(Material.ARROW)) {
                ItemStack arrow = playerInventory.getItem(playerInventory.first(Material.ARROW));
                arrow.setAmount(arrow.getAmount() - 1);

                ItemMeta meta = event.getItem().getItemMeta();
                meta.setCustomModelData(1790004);
                event.getItem().setItemMeta(meta);
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
            } else if (arrow.hasMetadata("grappleArrow")) {
                onGrappleArrowHit(event);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player
                    && player.getUniqueId() == Main.jungleRulerUUID
                    && player.getInventory().getHelmet().getType() == Material.CARVED_PUMPKIN
                    && player.getInventory().getHelmet().hasItemMeta()
                    && player.getInventory().getHelmet().getItemMeta().hasCustomModelData()
                    && player.getInventory().getHelmet().getItemMeta().getCustomModelData() == 1790006
                    && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
        }
    }

    private void onBoomStickBoom(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        event.getPlayer().getWorld().createExplosion(player.getLocation(), 2.0F, true, true, player);
        player.setVelocity(player.getVelocity().add(new Vector(0, 2, 0)));
    }

    private void onGrappleArrowHit(ProjectileHitEvent event) {
        for (Grapple grapple : mainPlugin.grapples) {
            if (grapple.projectile == event.getEntity()) {
                Player player = grapple.player;
                Vector playerDistance = grapple.projectile.getLocation().subtract(player.getLocation()).toVector();
                float targetDistance = (float) playerDistance.length() + 6F;
                grapple.maxDistance = Math.min(targetDistance, grapple.maxDistance);

                if (Objects.nonNull(event.getHitEntity())) {
                    if (grapple.projectile instanceof LivingEntity livingEntity) {
                        grapple.projectile = livingEntity;
                        grapple.bat.setLeashHolder(null);
                        livingEntity.setLeashHolder(player);
                        livingEntity.setLastDamage(2);
                    } else {
                        grapple.projectile = event.getHitEntity();
                    }
                }
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
    public void onDispenserActivate(BlockDispenseEvent event) {
        if (event.getBlock().getType() == Material.DISPENSER && event.getItem().getType() == Material.GOAT_HORN) {
            ItemStack goatHorn = event.getItem();
            MusicInstrumentMeta meta = (MusicInstrumentMeta) goatHorn.getItemMeta();
            Sound sound = null;
            MusicInstrument instrument = meta.getInstrument();
            if (instrument.equals(MusicInstrument.PONDER)) {
                sound = Sound.ITEM_GOAT_HORN_SOUND_0;
            } else if (instrument.equals(MusicInstrument.SING)) {
                sound = Sound.ITEM_GOAT_HORN_SOUND_1;
            } else if (instrument.equals(MusicInstrument.SEEK)) {
                sound = Sound.ITEM_GOAT_HORN_SOUND_2;
            } else if (instrument.equals(MusicInstrument.FEEL)) {
                sound = Sound.ITEM_GOAT_HORN_SOUND_3;
            } else if (instrument.equals(MusicInstrument.ADMIRE)) {
                sound = Sound.ITEM_GOAT_HORN_SOUND_4;
            } else if (instrument.equals(MusicInstrument.CALL)) {
                sound = Sound.ITEM_GOAT_HORN_SOUND_5;
            } else if (instrument.equals(MusicInstrument.YEARN)) {
                sound = Sound.ITEM_GOAT_HORN_SOUND_6;
            } else if (instrument.equals(MusicInstrument.DREAM)) {
                sound = Sound.ITEM_GOAT_HORN_SOUND_7;
            }
            event.getBlock().getWorld().playSound(event.getBlock().getLocation(), sound, 16, 1);



            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteraction(PlayerInteractEvent event) {
        switch (event.getAction()) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> {
                if (event.hasItem() && event.getItem().hasItemMeta() && event.getItem().getItemMeta().hasCustomModelData()) {
                    switch (event.getItem().getType()) {
                        case STICK -> {
                            switch (event.getItem().getItemMeta().getCustomModelData()) {
                                case 1790001 -> sculkStaffAttack(event);
                                case 1790003 -> onGrappleDisconnect(event);
                                case 1790005 -> jungleStaffAttack(event);
                            }
                        }
                    }
                }
            }
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> {
                if (event.hasItem() && event.getItem().hasItemMeta() && event.getItem().getItemMeta().hasCustomModelData()) {
                    switch (event.getItem().getType()) {
                        case STICK -> {
                            switch (event.getItem().getItemMeta().getCustomModelData()) {
                                case 1790001 -> sculkStaffUse(event);
                                case 1790003 -> onGrapplePullBackTick(event);
                                case 1790004 -> onGrappleHookUse(event);
                                case 1790006 -> onBoomStickBoom(event);
                            }
                        }
                    }
                }
            }
        }
    }

    private void jungleStaffAttack(PlayerInteractEvent event) {
        event.setCancelled(true);
        Location location = event.getPlayer().getEyeLocation();
        float step = 0.25F;
        for (float i = 0; i < 25; i += step) {
            location.add(location.getDirection().multiply(step));
            location.getWorld().spawnParticle(Particle.REDSTONE, location, 2, 0.2, 0.2, 0.2, new Particle.DustOptions(Color.GREEN, 2));
            if (!location.getBlock().isEmpty()) {
                location.add(0, 1, 0);
                Main.fill(location.clone().add(-2, 0, -2), location.clone().add(2, 16, 2), Material.AIR);
                Main.fill(location.clone().add(-2, -1, -2), location.clone().add(2, -1, 2), Material.GRASS_BLOCK);
                Main.fill(location.clone().add(-2, -5, -2), location.clone().add(2, -2, 2), Material.DIRT);



                boolean success = location.getWorld().generateTree(location, TreeType.JUNGLE);

                if (success) {
                    for (Entity entity : location.getWorld().getNearbyEntities(location.add(0, 15, 0), 3.5, 15, 3.5)) {
                        entity.teleport(entity.getLocation().add(0, 31, 0));
                        entity.setVelocity(entity.getVelocity().add(new Vector(0, 3, 0)));
                    }
                }
                break;
            }
        }
    }

    private void sculkStaffAttack(PlayerInteractEvent event) {
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

    @EventHandler
    public void onLeadDisconnect(PlayerUnleashEntityEvent event) {
        for (Grapple grapple : mainPlugin.grapples) {
            if (event.getEntity() == grapple.bat || event.getEntity() == grapple.projectile) {
                event.setCancelled(true);
            }
        }
    }

    private void onGrappleDisconnect(PlayerInteractEvent event) {
        for (Grapple grapple : mainPlugin.grapples) {
            Player player = event.getPlayer();
            if (player == grapple.player) {

                Vector playerDistance = grapple.projectile.getLocation().subtract(player.getLocation()).toVector();

                grapple.remove();

                if (playerDistance.length() <= 4) {
                    ItemMeta meta = event.getItem().getItemMeta();
                    meta.setCustomModelData(1790004);
                    event.getItem().setItemMeta(meta);
                }
                break;
            }
        }
    }

    private void sculkStaffUse(PlayerInteractEvent event) {
        Location loc = event.getPlayer().getLocation().subtract(0, 1, 0);
        Block block = loc.getBlock();
        if (!block.isEmpty()) {
            block.setType(Material.SCULK_CATALYST);
            SculkCatalyst catalyst = (SculkCatalyst) block.getState();
            catalyst.bloom(loc.add(1, 1, 1).getBlock(), 68); // note: this number is random
            catalyst.bloom(loc.add(-1, 1, 1).getBlock(), 68); // note: this number is also random
            catalyst.bloom(loc.add(1, 1, -1).getBlock(), 68); // note: this number is the same as the others, and the others are random. Does that make this number random too?
            catalyst.bloom(loc.add(-1, 1, -1).getBlock(), 68); // note: im suffering
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBroken(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        Location location = event.getBlock().getLocation();
        if (item.getType() == Material.DIAMOND_PICKAXE && item.hasItemMeta() && item.getItemMeta().hasCustomModelData() && item.getItemMeta().getCustomModelData() == 1790001) {

            Main.replaceSculk(location.clone().add(1, 0, 0));
            Main.replaceSculk(location.clone().add(0, 1, 0));
            Main.replaceSculk(location.clone().add(0, 0, 1));

            Main.replaceSculk(location.clone().add(-1, 0, 0));
            Main.replaceSculk(location.clone().add(0, -1, 0));
            Main.replaceSculk(location.clone().add(0, 0, -1));
        } else if (item.getType() == Material.NETHERITE_PICKAXE && item.hasItemMeta() && item.getItemMeta().hasCustomModelData() && item.getItemMeta().getCustomModelData() == 1790001) {
            Main.replaceSculkable(location.clone().add(1, 0, 0), Material.COPPER_BLOCK);
            Main.replaceSculkable(location.clone().add(0, 1, 0), Material.COPPER_BLOCK);
            Main.replaceSculkable(location.clone().add(0, 0, 1), Material.COPPER_BLOCK);

            Main.replaceSculkable(location.clone().add(-1, 0, 0), Material.COPPER_BLOCK);
            Main.replaceSculkable(location.clone().add(0, -1, 0), Material.COPPER_BLOCK);
            Main.replaceSculkable(location.clone().add(0, 0, -1), Material.COPPER_BLOCK);
        }
    }

    @EventHandler
    public void onPlayerEat(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (item.getType() == Material.ENCHANTED_GOLDEN_APPLE && item.hasItemMeta() && item.getItemMeta().hasCustomModelData() && item.getItemMeta().getCustomModelData() == 1790001) {
            if (event.getPlayer().getUniqueId() != Main.netherGodUUID) {
                event.getPlayer().setFireTicks(100);
                event.getPlayer().getInventory().getItem(event.getHand()).setAmount(0);
                event.setItem(new ItemStack(Material.GOLDEN_CARROT));
            }
        }
    }


    @EventHandler
    public void onBlockBreakStart(BlockDamageEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        Location location = event.getBlock().getLocation();
        if (item.getType() == Material.DIAMOND_PICKAXE
                    && item.hasItemMeta()
                    && item.getItemMeta().hasCustomModelData()
                    && item.getItemMeta().getCustomModelData() == 1790001
                    && item.getType() == Material.SCULK) {
            event.setInstaBreak(true);
        }
    }
}