package com.genandnic.server.armor;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.util.Vector;

public class GoldenArmor implements Listener {

	@EventHandler
	public void onPlayerDamage(EntityDamageEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof Player) {

			Player player = (Player) entity;
			DamageCause cause = event.getCause();
			EntityEquipment equipment = player.getEquipment();

			if (((cause == DamageCause.FIRE) || (cause == DamageCause.LAVA) || (cause == DamageCause.HOT_FLOOR)
					|| (cause == DamageCause.FIRE_TICK)) && (equipment.getLeggings() != null)
					&& (equipment.getLeggings().getType() == Material.GOLD_LEGGINGS)) {
				event.setCancelled(true);
				return;
			}

			if ((cause == DamageCause.DROWNING || cause == DamageCause.FLY_INTO_WALL) && (equipment.getHelmet() != null)
					&& (equipment.getHelmet().getType() == Material.GOLD_HELMET)) {
				event.setCancelled(true);
				return;
			}

			if ((cause == DamageCause.FALL)
					&& ((equipment.getBoots() != null) && (equipment.getBoots().getType() == Material.GOLD_BOOTS))) {
				event.setCancelled(true);
				return;
			}

			if ((cause == DamageCause.ENTITY_EXPLOSION || cause == DamageCause.BLOCK_EXPLOSION)
					&& (equipment.getChestplate() != null)
					&& (equipment.getChestplate().getType() == Material.GOLD_CHESTPLATE)) {
				event.setCancelled(true);

				if (event instanceof EntityDamageByEntityEvent) {
					Entity damager = ((EntityDamageByEntityEvent) event).getDamager();

					double dX = player.getLocation().getX() - damager.getLocation().getX();
					double dY = player.getLocation().getY() - (damager.getLocation().getY() - 0.75);
					double dZ = player.getLocation().getZ() - damager.getLocation().getZ();
					double yaw = Math.atan2(dZ, dX);
					double pitch = Math.atan2(Math.sqrt(dZ * dZ + dX * dX), dY) + Math.PI;
					double x = (Math.sin(pitch) * Math.cos(yaw)) * 2;
					double y = Math.cos(pitch);
					double z = (Math.sin(pitch) * Math.sin(yaw) * 2);

					double distance = -5 / (player.getLocation().distance(damager.getLocation()));
					if (distance < -3)
						distance = -3;
					if (distance > -1)
						distance = 0;

					Vector vector = new Vector(x, y, z);
					player.setVelocity(vector.multiply(distance));
				}

			}
		}
	}
}
