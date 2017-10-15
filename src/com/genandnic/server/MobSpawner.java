package com.genandnic.server;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;

public class MobSpawner implements Listener {

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();

		if (block.getType() != Material.MOB_SPAWNER || event.isCancelled())
			return;

		EntityType type = ((CreatureSpawner) block.getState()).getSpawnedType();

		if (event.getPlayer().getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH)) {
			block.getWorld().dropItemNaturally(block.getLocation(), getSpawnerItem(type));
			return;
		}
		
		ItemStack spawnEgg = new ItemStack(Material.MONSTER_EGG, 1);
		SpawnEggMeta meta = (SpawnEggMeta) spawnEgg.getItemMeta();
		meta.setSpawnedType(type);
		spawnEgg.setItemMeta(meta);
		
		block.getWorld().dropItemNaturally(block.getLocation(), spawnEgg);
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		ItemStack item = event.getItemInHand();

		if (item == null)
			return;

		if (item.getType().equals(Material.MOB_SPAWNER)) {
			String type;

			if (item.getItemMeta().getLocalizedName() == null) {
				type = "Pig Spawner";
			} else {
				type = item.getItemMeta().getLocalizedName();
			}

			CreatureSpawner state = (CreatureSpawner) event.getBlock().getState();
			state.setSpawnedType(EntityType.valueOf(type.split(" ")[0].toUpperCase()));
			state.setDelay(0);
			state.update();
		}
	}

	private ItemStack getSpawnerItem(EntityType type) {
		ItemStack item = new ItemStack(Material.MOB_SPAWNER, 1);

		String name = type.toString();
		name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase() + " Spawner";
		
		ItemMeta meta = item.getItemMeta();
		meta.setLocalizedName(name);
		item.setItemMeta(meta);

		return item;
	}

}
