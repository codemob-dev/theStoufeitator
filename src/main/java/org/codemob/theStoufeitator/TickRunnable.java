package org.codemob.theStoufeitator;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Objects;

public class TickRunnable implements Runnable {
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
                    }
                }
            }

            if (Objects.nonNull(playerInventory.getItemInMainHand().getItemMeta()) && playerInventory.getItemInMainHand().getItemMeta().hasCustomModelData()) {
                if (playerInventory.getItemInMainHand().getType() == Material.STICK) {
                    ItemMeta meta = playerInventory.getItemInMainHand().getItemMeta();
                    switch (meta.getCustomModelData()) {
                        case 1790001 -> livingStaffTick(playerInventory.getItemInMainHand(), player);
                        case 1790002 -> deadStaffTick(playerInventory.getItemInMainHand(), player);
                    }
                }
            }
        }
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
            Objects.requireNonNull(item.getItemMeta()).setCustomModelData(1790001);
        }
    }

    private void livingStaffTick(ItemStack item, Player player) {
        if (player.getUniqueId() != Main.sculkGodUUID) {
            Objects.requireNonNull(item.getItemMeta()).setCustomModelData(1790002);
        }
    }
}
