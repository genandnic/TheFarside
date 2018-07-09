package com.genandnic.server;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ConveyorBelts implements Listener {

	private FileConfiguration config;
	private double speed;
	private Plugin plugin;

	public ConveyorBelts(Main plugin) {
		this.config = plugin.getConfig();
		this.speed = config.getDouble("conveyorSpeed");
		this.plugin = plugin;
		moveAllEntities();
	}

	// @EventHandler
	// public void onPlayerMove(PlayerMoveEvent event) {
	//
	// moveEntity(event.getPlayer());
	//
	// }

	private void moveAllEntities() {

		new BukkitRunnable() {

			@Override
			public void run() {

				for (World world : Bukkit.getWorlds()) {
					for (Entity entity : world.getEntities()) {

						moveEntity(entity);

					}
				}

			}
		}.runTaskTimer(plugin, 0L, 1L);
	}

	private void moveEntity(Entity entity) {
		Block block = getStandingBlock(entity);

		if (block.getType() == Material.MAGENTA_GLAZED_TERRACOTTA) {
			switch (block.getData()) {
			case 1:
				entity.setVelocity(entity.getVelocity().setX(speed));
				// +x
				break;
			case 2:
				entity.setVelocity(entity.getVelocity().setZ(speed));
				// +z
				break;
			case 3:
				entity.setVelocity(entity.getVelocity().setX(-speed));
				// -x
				break;
			case 0:
				entity.setVelocity(entity.getVelocity().setZ(-speed));
				// -z
				break;
			}
		}

	}

	private Block getStandingBlock(Entity player) {

		final Location location = player.getLocation().add(0, -0.00000000000001, 0);

		Block block = location.getBlock();
		if (block.getType().isSolid())
			return block;

		if (location.add(0.3, 0, 0.3).getBlock().getType().isSolid() || location.add(-0.6, 0, 0).getBlock().getType().isSolid()
				|| location.add(0, 0, -0.6).getBlock().getType().isSolid() || location.add(0.6, 0, 0).getBlock().getType().isSolid()) {
			return location.getBlock();
		}

		return block;
	}

}
