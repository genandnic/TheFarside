package com.genandnic.server.armor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import com.genandnic.server.Main;

public class SneakyArmor implements Listener {

	private Main plugin;
	private Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
	private Scoreboard sneakyBoard = Bukkit.getScoreboardManager().getNewScoreboard();

	public SneakyArmor(Main plugin) {
		this.plugin = plugin;

		for(Player player: Bukkit.getOnlinePlayers()) {
			player.setScoreboard(sneakyBoard);
			updateSneaking(player);
		}

	}

	@EventHandler
	public void onArmorEquip(ArmorEquipEvent event) {

		new BukkitRunnable() {
			@Override
			public void run() {

				updateSneaking(event.getPlayer());
				
			}
		}.runTask(plugin);
		
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		player.setScoreboard(sneakyBoard);
		updateSneaking(player);

	}

	@EventHandler(priority = EventPriority.LOW)
	public void onCommandPreProcess(PlayerCommandPreprocessEvent event) {

		if (event.getMessage().toLowerCase().startsWith("/scoreboard")) {

			new BukkitRunnable() {
				@Override
				public void run() {
					for (Player player : Bukkit.getOnlinePlayers()) {
						
						updateSneaking(player);

					}
				}
			}.runTask(plugin);

		}
	}

	public void updateSneaking(Player player) {

		String name = player.getName();

		Team team = board.getEntryTeam(name);
		Team sneakyTeam = sneakyBoard.getTeam(name);

		if (sneakyTeam == null) {
			sneakyTeam = sneakyBoard.registerNewTeam(name);
			sneakyTeam.addEntry(name);
		}
		
		if (team != null)
			sneakyTeam.setPrefix(team.getPrefix());
		else
			sneakyTeam.setPrefix("");

		ItemStack[] armor = player.getEquipment().getArmorContents();
		if (armor[3] != null && armor[3].getType() == Material.LEATHER_HELMET && armor[2] != null
				&& armor[2].getType() == Material.LEATHER_CHESTPLATE && armor[1] != null
				&& armor[1].getType() == Material.LEATHER_LEGGINGS && armor[0] != null
				&& armor[0].getType() == Material.LEATHER_BOOTS) {
			sneakyTeam.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.NEVER);
		} else
			sneakyTeam.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.ALWAYS);

	}
}
