package org.codemob.theStoufeitator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Objects;

public class TickRunnable implements Runnable {
    Main mainPlugin;
    public TickRunnable(Main main) {
        mainPlugin = main;
    }

    public void warCrownTick(Player player) {
        if (player.getUniqueId() == Main.netherGodUUID) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 50, 2, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 50, 2, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 50, 1, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 50, 9, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 50, 0, false, false));
        } else {
            player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 50, 1, false, true));
        }
    }

    public void copperHatTick(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 50, 1, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 50, 4, false, true));
    }

    public void sculkCrownTick(Player player) {
        if (player.getUniqueId() == Main.sculkGodUUID) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 50, 5, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 50, 1, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 50, 19, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 50, 1, false, false));
        } else {
            player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 50, 10));
            player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 50, 10));
        }
    }

    private void catHatTick(Player player) {
        if (player.getUniqueId() == Main.jungleRulerUUID) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 50, 0, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 50, 4, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 50, 4, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 50, 3, false, false));
        } else {
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 50, 1, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 50, 0, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 1, false, false));
        }
    }

    public void copperBandTick(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 50, 2, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 50, 2, false, false));
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerInventory playerInventory = player.getInventory();
            if (Objects.nonNull(playerInventory.getHelmet())) {
                ItemMeta meta = playerInventory.getHelmet().getItemMeta();
                if (playerInventory.getHelmet().getType() == Material.CARVED_PUMPKIN && Objects.nonNull(meta) && meta.hasCustomModelData()) {
                    switch (meta.getCustomModelData()) {
                        case 1790001 -> netherCrownTick(player);
                        case 1790002 -> copperHatTick(player);
                        case 1790003 -> warCrownTick(player);
                        case 1790004 -> sculkCrownTick(player);
                        case 1790005 -> copperBandTick(player);
                        case 1790006 -> catHatTick(player);
                    }
                }
            }

            if (playerInventory.getItemInMainHand().hasItemMeta() && playerInventory.getItemInMainHand().getItemMeta().hasCustomModelData()) {
                switch (playerInventory.getItemInMainHand().getType()) {
                    case STICK -> {
                        ItemMeta meta = playerInventory.getItemInMainHand().getItemMeta();
                        switch (meta.getCustomModelData()) {
                            case 1790001 -> livingStaffTick(playerInventory.getItemInMainHand(), player);
                            case 1790002 -> deadStaffTick(playerInventory.getItemInMainHand(), player);
                            case 1790003, 1790004 -> grappleGunTick(playerInventory.getItemInMainHand(), player);
                        }
                    }
                }
            }
        }

        ArrayList<Grapple> toRemove = new ArrayList<>();
        for (Grapple grapple : mainPlugin.grapples) {
            if (!grapple.bat.isValid() | !grapple.projectile.isValid() | grapple.projectile == grapple.player) {
                grapple.remove();
                toRemove.add(grapple);
            } else {
                if (grappleTick(grapple)) toRemove.add(grapple);
            }
        }
        mainPlugin.grapples.removeAll(toRemove);
    }

    private void grappleGunTick(ItemStack grappleGun, Player player) {
    }

    @SuppressWarnings("deprecation")
    private boolean grappleTick(Grapple grapple) {
        Location location = grapple.projectile.getLocation();
        if (grapple.projectile instanceof Arrow) {
            location.subtract(0, 0.25, 0);
        }
        grapple.bat.teleport(location);
        grapple.bat.setVelocity(grapple.projectile.getVelocity());

        Vector playerDistance = grapple.projectile.getLocation().subtract(grapple.player.getLocation()).toVector();

        if (!grapple.player.isOnGround()
                    && !grapple.player.isSneaking()
                    && !(!grapple.player.getLocation().subtract(0, 1.5, 0).getBlock().isEmpty() && grapple.player.getVelocity().length() < 0.25)
                    && grapple.projectile instanceof Arrow) {
            grapple.player.setGliding(true);
            Vector modifiedVelocity = grapple.player.getVelocity().multiply(0.95);
            modifiedVelocity.add(grapple.player.getLocation().getDirection().multiply(new Vector(0.05, 0, 0.05)));
            grapple.player.setVelocity(modifiedVelocity);
        }

        float power = playerDistance.length() < grapple.maxDistance ? 0 : (float) (playerDistance.length() - grapple.maxDistance);
        power *= 0.1F;
        if (power > 1.2) {
            grapple.remove();
            return true;
        }
        power = Main.unsignedSmoothClip(power, 1, 6);

        Vector deltaV = playerDistance.normalize().multiply(power);
        deltaV.add(grapple.player.getVelocity().multiply(new Vector().copy(deltaV).multiply(-1)));

        if (!(grapple.projectile instanceof Arrow arrow && arrow.isInBlock())) {
            grapple.projectile.setVelocity(grapple.projectile.getVelocity().subtract(deltaV));
        }
        if (power != 0 && !(grapple.projectile instanceof Arrow arrow && !arrow.isInBlock())) {
            grapple.player.setVelocity(grapple.player.getVelocity().add(deltaV));
        }
        return false;
    }

    private void netherCrownTick(Player player) {
        if (player.getUniqueId() == Main.netherGodUUID) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 50, 2, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 50, 2, false, false));
        } else if (player.getUniqueId() == Main.sculkGodUUID) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 50, 0, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 50, 0, false, false));
        } else {
            player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 50, 1, false, true));
        }
    }

    private void deadStaffTick(ItemStack item, Player player) {
        if (player.getUniqueId() == Main.sculkGodUUID) {
            ItemMeta meta = item.getItemMeta();
            meta.setCustomModelData(1790001);
            item.setItemMeta(meta);
        }
    }

    private void livingStaffTick(ItemStack item, Player player) {
        if (player.getUniqueId() != Main.sculkGodUUID) {
            ItemMeta meta = item.getItemMeta();
            meta.setCustomModelData(1790002);
            item.setItemMeta(meta);
        }
    }
}
