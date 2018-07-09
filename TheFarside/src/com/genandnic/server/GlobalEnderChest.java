package com.genandnic.server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_12_R1.BlockPosition;

public class GlobalEnderChest implements Listener {

	private static World world = Bukkit.getWorlds().get(0);
	private static Inventory enderchest;

	private static FileConfiguration enderConfig = new YamlConfiguration();
	private static File enderConfigFile = new File(world.getWorldFolder(), "enderchest.yml");

	public GlobalEnderChest() {

		enderchest = Bukkit.createInventory(null, 27, "Public Ender Chest");

		if (!enderConfigFile.exists())
			return;

		try {
			enderConfig.load(enderConfigFile);
			List<?> list = (List<?>) enderConfig.get("Inventory");
			enderchest.setContents(list.toArray(new ItemStack[0]));
		} catch (Exception e) {
			return;
		}

		// System.out.println("Loaded global enderchest.");

	}

	private static HashMap<Player, Block> map = new HashMap<>();

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Block block = event.getClickedBlock();
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK || block.getType() != Material.ENDER_CHEST)
			return;

		event.setCancelled(true);

		Player player = event.getPlayer();
		if (!ejectPrivateItems(player)) {
			player.openInventory(enderchest);

			setChestOpen(block, true);
			map.put(player, block);
		}
	}

	@EventHandler
	public void onOpenInventory(InventoryOpenEvent event) {
		if (event.getInventory().getType() == InventoryType.ENDER_CHEST) {
			event.setCancelled(true);

			Player player = (Player) event.getPlayer();
			if (!ejectPrivateItems(player))
				event.getPlayer().openInventory(enderchest);
		}
	}

	private boolean ejectPrivateItems(Player player) {
		Inventory enderchest = player.getEnderChest();
		ArrayList<ItemStack> contents = new ArrayList<ItemStack>();
		contents.addAll(Arrays.asList(enderchest.getStorageContents()));

		while (contents.remove(null))
			;
		if (contents.size() > 0) {
			player.sendMessage(ChatColor.RED + "This enderchest is no longer private! Your items are not safe.");

			Location location = player.getLocation();
			World world = player.getWorld();

			player.playSound(location, Sound.ENTITY_ENDERMEN_TELEPORT, 0.3F, 1.0F);
			for (ItemStack item : contents) {
				world.dropItem(location, item);
			}
			enderchest.clear();
			return true;
		}
		return false;
	}

	@EventHandler
	public void onCloseInventory(InventoryCloseEvent event) {
		if (event.getInventory().equals(enderchest)) {
			Player player = (Player) event.getPlayer();
			Block block = map.get(player);

			if (block != null)
				setChestOpen(block, false);
			else
				player.playSound(player.getLocation(), Sound.BLOCK_ENDERCHEST_CLOSE, 0.3F, 1.0F);
		}
	}

	private static void setChestOpen(Block chest, boolean open) {
		if (chest == null)
			return;

		((CraftWorld) chest.getWorld()).getHandle().playBlockAction(new BlockPosition(chest.getX(), chest.getY(), chest.getZ()), CraftMagicNumbers.getBlock(chest), 1,
				open ? 1 : 0);

		map.values().remove(chest);

	}

	@EventHandler
	public void onWorldSave(WorldSaveEvent event) {

		enderConfig.set("Inventory", enderchest.getStorageContents());
		try {
			enderConfig.save(enderConfigFile);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

	}

	public static void saveEnderChests() {

		enderConfig.set("Inventory", enderchest.getStorageContents());
		try {
			enderConfig.save(enderConfigFile);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		for (Player player : Bukkit.getOnlinePlayers()) {

			if (player.getOpenInventory().getTopInventory().equals(enderchest)) {
				setChestOpen(map.get(player), false);
				player.playSound(player.getLocation(), Sound.BLOCK_ENDERCHEST_CLOSE, 0.3F, 1.0F);
				player.closeInventory();
			}

		}
		map = null;

		// System.out.println("Saved global enderchest.");
	}
}
