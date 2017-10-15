package com.genandnic.server.commands;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Encase implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		
		if (args.length != 1) {
			return false;
		}

		Player player = sender.getServer().getPlayer(args[0]);

		if (player != null) {

			Block block = player.getLocation().subtract(0, 2, 0).getBlock();
			Material material = block.getType();

			if (material != Material.AIR) {
				Location location = player.getLocation().add(0, 3, 0);
				player.teleport(location);
			}
			
			block = player.getLocation().getBlock();
			block.setType(Material.AIR);
			block.getRelative(0, 1, 0).setType(Material.AIR);
			block.getRelative(0, -1, 0).setType(Material.BEDROCK);
			block.getRelative(1, 0, 0).setType(Material.BEDROCK);
			block.getRelative(-1, 0, 0).setType(Material.BEDROCK);
			block.getRelative(0, 0, 1).setType(Material.BEDROCK);
			block.getRelative(0, 0, -1).setType(Material.BEDROCK);
			block.getRelative(0, 2, 0).setType(Material.BEDROCK);
			
			Location location = new Location(player.getWorld(), player.getLocation().getBlockX() + 0.5, player.getLocation().getBlockY() + 0.0, player.getLocation().getBlockZ() + 0.5);
			
			player.teleport(location);
			sender.sendMessage("Encased " + args[0]);

			return true;

		} else {
			sender.sendMessage("Player '" + args[0] + "' cannot be found");
			return true;
		}
	}

}
