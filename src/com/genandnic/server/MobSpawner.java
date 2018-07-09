package com.genandnic.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;

public class MobSpawner implements Listener {

	private final Random random = new Random();
	private final List<EntityType> list = new ArrayList<EntityType>();

	public MobSpawner() {
		ShapedRecipe spawner = new ShapedRecipe(new ItemStack(Material.MOB_SPAWNER));
		spawner.shape("XXX", "XYX", "XXX");
		spawner.setIngredient('X', Material.IRON_FENCE);
		spawner.setIngredient('Y', Material.MONSTER_EGG);
		Bukkit.getServer().addRecipe(spawner);

		for (EntityType type : EntityType.values()) {
			if (type.isAlive() && type.isSpawnable() && type != EntityType.ENDER_DRAGON && type != EntityType.WITHER) {
				list.add(type);
			}
		}
	}

	@EventHandler
	public void onCraftItem(PrepareItemCraftEvent event) {
		CraftingInventory inventory = event.getInventory();

		ItemStack result = inventory.getResult();
		if (result != null && result.getType() == Material.MOB_SPAWNER) {

			for (ItemStack item : inventory.getMatrix()) {
				if (item.getType() == Material.MONSTER_EGG) {

					SpawnEggMeta meta = (SpawnEggMeta) item.getItemMeta();
					EntityType type = meta.getSpawnedType();

					inventory.setResult(getSpawnerItem(type));
					return;
				}
			}

		}
	}

	@EventHandler
	public void onSpawnerSpawn(SpawnerSpawnEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof LivingEntity)
			((LivingEntity) entity).setRemoveWhenFarAway(true);

	}

	@EventHandler
	public void onVillagerLevelUp(VillagerAcquireTradeEvent event) {
		Villager villager = event.getEntity();
		if (villager.getProfession() == Profession.PRIEST) {

			EntityType type = list.get(random.nextInt(list.size()));

			ItemStack result = new ItemStack(Material.MONSTER_EGG);
			SpawnEggMeta meta = (SpawnEggMeta) result.getItemMeta();
			meta.setSpawnedType(type);
			result.setItemMeta(meta);

			MerchantRecipe recipe = new MerchantRecipe(result, 100);
			recipe.addIngredient(new ItemStack(Material.MONSTER_EGG));
			recipe.addIngredient(new ItemStack(Material.EMERALD, Math.max(7, random.nextInt(22))));
			event.setRecipe(recipe);
		}

	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();

		if (block.getType() != Material.MOB_SPAWNER || event.isCancelled())
			return;

		Player player = event.getPlayer();
		if (player.getGameMode() == GameMode.CREATIVE)
			return;

		EntityType type = ((CreatureSpawner) block.getState()).getSpawnedType();
		if (event.getPlayer().getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH)) {
			event.setExpToDrop(0);
			block.getWorld().dropItemNaturally(block.getLocation(), getSpawnerItem(type));
			return;
		}

		ItemStack spawnEgg = new ItemStack(Material.MONSTER_EGG);
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
			CreatureSpawner state = (CreatureSpawner) event.getBlock().getState();
			String name = item.getItemMeta().getLocalizedName();

			if (name != null) {
				EntityType type = EntityType.valueOf(name.toUpperCase().replace(" ", "_").replace("_SPAWNER", ""));
				if (type.isSpawnable())
					state.setSpawnedType(type);
			}

			state.update();
		}
	}

	private ItemStack getSpawnerItem(EntityType type) {
		ItemStack item = new ItemStack(Material.MOB_SPAWNER);

		ItemMeta meta = item.getItemMeta();
		meta.setLocalizedName(WordUtils.capitalizeFully(type.name().replace("_"," ")).concat(" Spawner"));
		item.setItemMeta(meta);

		return item;
	}

}
