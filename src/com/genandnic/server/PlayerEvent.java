package com.genandnic.server;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerEvent implements Listener {

	private Main plugin;
	private FileConfiguration config;

	private boolean dropPlayerHeads;
	private boolean piggybackRides;
	private boolean hideSpectators;

	public PlayerEvent(Main plugin) {
		this.plugin = plugin;
		this.config = plugin.getConfig();

		dropPlayerHeads = config.getBoolean("dropPlayerHeads");
		piggybackRides = config.getBoolean("piggybackRides");
		hideSpectators = config.getBoolean("hideSpectators");
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		if (hideSpectators) {
			for (Player players : Bukkit.getOnlinePlayers()) {
				if (player.getGameMode() == GameMode.SPECTATOR)
					players.hidePlayer(player);
				if (players.getGameMode() == GameMode.SPECTATOR)
					player.hidePlayer(players);
			}
		}

		if (player.getGameMode() == GameMode.SPECTATOR) {
			event.setJoinMessage(null);
		} else {
			new BukkitRunnable() {
				@Override
				public void run() {
					for (Player players : Bukkit.getOnlinePlayers()) {
						players.playSound(players.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 2.0F, 1.0F);
					}
				}
			}.runTaskLater(plugin, 10);
		}

	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerDisconnect(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		if (player.getGameMode() == GameMode.SPECTATOR)
			event.setQuitMessage(null);

		if (player.getVehicle() instanceof Player)
			player.leaveVehicle();
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onGamemodeChange(PlayerGameModeChangeEvent event) {

		if (hideSpectators) {
			if (event.getNewGameMode() == GameMode.SPECTATOR) {
				for (Player players : Bukkit.getOnlinePlayers()) {
					players.hidePlayer(event.getPlayer());
				}
			} else {
				for (Player players : Bukkit.getOnlinePlayers()) {
					players.showPlayer(event.getPlayer());
				}
			}
		}

	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerInteract(PlayerInteractEntityEvent event) {
		if (piggybackRides && event.getRightClicked() instanceof Player) {
			Player entity = (Player) event.getRightClicked();

			if (entity.isSneaking() && entity.getLocation().getBlock().getRelative(0, 2, 0).getType().isTransparent())
				entity.addPassenger(event.getPlayer());
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		if (!event.isCancelled() && event.getEntity() instanceof LivingEntity) {

			LivingEntity entity = (LivingEntity) event.getEntity();

			if (event.getDamage() == 0) {
				event.setDamage(0.1);
				return;
			}

			Entity damager = event.getDamager();
			if (damager instanceof Player && entity instanceof Player) {
				damager.eject();
			}

		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			DamageCause cause = event.getCause();

			if ((cause == DamageCause.FALL && event.getDamage() < 5))
				event.setCancelled(true);

		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (dropPlayerHeads && event.getEntity().getKiller() instanceof Player) {

			ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
			SkullMeta meta = (SkullMeta) head.getItemMeta();
			meta.setOwner(event.getEntity().getName());
			head.setItemMeta(meta);

			event.getDrops().add(head);
		}
	}

}
