package org.codemob.theStoufeitator;

import org.bukkit.entity.*;

public class Grapple {
    public Entity projectile;
    public Player player;
    public Bat bat;
    public final float absoluteMaxDistance = 48;
    public float maxDistance = absoluteMaxDistance;

    public Grapple(Arrow projectile, Player player) {
        this.projectile = projectile;
        this.player = player;

        bat = player.getWorld().spawn(projectile.getLocation(), Bat.class);
        bat.setInvisible(true);
        bat.setCollidable(false);
        bat.setSilent(true);
        bat.setInvulnerable(true);
        bat.setAI(false);

        bat.setLeashHolder(player);
    }

    public void remove() {
        bat.remove();
        if (projectile instanceof Arrow) {
            projectile.remove();
        } else if (projectile instanceof LivingEntity livingEntity) {
            livingEntity.setLeashHolder(null);
        }
    }
}
