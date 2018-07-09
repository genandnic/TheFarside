package com.genandnic.server.commands;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class Kill implements Listener {

	@EventHandler(priority = EventPriority.LOW)
	public void onCommandPreProcess(PlayerCommandPreprocessEvent event) {

		Player player = event.getPlayer();
		String message = event.getMessage().toLowerCase();

		if ((!player.isOp() && message.startsWith("/kill")) || message.startsWith("/die") || message.startsWith("/suicide")) {
			event.setCancelled(true);

			player.damage(1);
			player.setHealth(0.0);
			return;

		}

	}

}
