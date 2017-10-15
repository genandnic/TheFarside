package com.genandnic.server;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;

public class BombArrows implements Listener {

	private Main plugin;

	public BombArrows(Main plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		PlayerInventory inventory = event.getPlayer().getInventory();
		if (inventory.getItemInMainHand().getType() == Material.BOW
				&& inventory.getItemInOffHand().getType() == Material.TNT
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
				&& (inventory.contains(Material.ARROW) || inventory.contains(Material.SPECTRAL_ARROW)
						|| inventory.contains(Material.TIPPED_ARROW))) {

			event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_TNT_PRIMED, 1, 2);

		}
	}

	@EventHandler
	public void onShoot(EntityShootBowEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			ItemStack offhand = player.getInventory().getItemInOffHand();

			if (event.getForce() > 0.25 && offhand.getType() == Material.TNT) {

				if (player.getGameMode() != GameMode.CREATIVE)
					offhand.setAmount(offhand.getAmount() - 1);

				event.getProjectile().setMetadata("bombArrow", new FixedMetadataValue(plugin, true));
			}
		}
	}

	@EventHandler
	public void onHit(ProjectileHitEvent event) {
		Entity entity = event.getEntity();

		if (entity.hasMetadata("bombArrow")) {
			Entity arrow = entity;
			Location location = arrow.getLocation().add(0, -1.15, 0);
			World world = arrow.getWorld();

			Entity tnt = world.spawn(location, TNTPrimed.class);

			tnt.setMetadata("bombArrowExplosion", new FixedMetadataValue(plugin, true));
			((TNTPrimed) tnt).setFuseTicks(0);
			arrow.remove();

		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player && event.getDamager().hasMetadata("bombArrowExplosion")
				&& event.getDamage() > 3) {
			event.setDamage(3);
		}
	}

}
