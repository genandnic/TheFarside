package com.genandnic.server;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class LightningArrows implements Listener {

	@EventHandler
	public void onHit(ProjectileHitEvent event) {
		if (event.getEntity() instanceof SpectralArrow) {
			SpectralArrow arrow = (SpectralArrow) event.getEntity();
			Location location = arrow.getLocation().add(-0.5, 0, -0.5);

			location.getWorld().strikeLightning(location);
			arrow.remove();
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof LivingEntity) {

			LivingEntity entity = (LivingEntity) event.getEntity();

			if (event.getCause() == DamageCause.LIGHTNING) {
				event.setDamage(2);
				entity.removePotionEffect(PotionEffectType.GLOWING);
				entity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 0));
			}
		}
	}

	@EventHandler
	public void onBlockIgnite(BlockIgniteEvent event) {
		if (event.getCause() == IgniteCause.LIGHTNING)
			event.setCancelled(true);
	}

}
