package com.genandnic.server;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.genandnic.server.armor.ArmorListener;
import com.genandnic.server.armor.GoldenArmor;
import com.genandnic.server.armor.SneakyArmor;
import com.genandnic.server.commands.Encase;
import com.genandnic.server.commands.Kill;

public class Main extends JavaPlugin {

	private static Plugin plugin;
	private FileConfiguration config;

	@Override
	public void onEnable() {

		plugin = this;
		config = getConfig();

		setupConfig();

		registerCommands();
		registerEvents();
		registerRecipes();

	}

	@Override
	public void onDisable() {

		getServer().clearRecipes();

	}

	public void setupConfig() {

		saveDefaultConfig();
		getConfig().options().copyDefaults(true);
		saveConfig();

	}

	private void registerCommands() {
		getCommand("encase").setExecutor(new Encase());
	}

	private void registerEvents() {
		PluginManager pm = getServer().getPluginManager();

		if (config.getBoolean("silkSpawners"))
			pm.registerEvents(new MobSpawner(), this);
		if (config.getBoolean("bombArrows"))
			pm.registerEvents(new BombArrows(this), this);
		if (config.getBoolean("lightningArrows"))
			pm.registerEvents(new LightningArrows(), this);
		if (config.getBoolean("multishotBow"))
			pm.registerEvents(new MultishotBow(this), this);
		if (config.getBoolean("goldenArmor"))
			pm.registerEvents(new GoldenArmor(), this);
		if (config.getBoolean("sneakyArmor")) {
			pm.registerEvents(new ArmorListener(), this);
			pm.registerEvents(new SneakyArmor(this), this);
		}

		pm.registerEvents(new Kill(), this);
		pm.registerEvents(new PlayerEvent(this), this);

	}

	private void registerRecipes() {

		if (config.getBoolean("customRecipes")) {
			Server server = Bukkit.getServer();

			ShapelessRecipe flint = new ShapelessRecipe(new ItemStack(Material.FLINT));
			flint.addIngredient(Material.GRAVEL);
			server.addRecipe(flint);

			ShapelessRecipe nametag = new ShapelessRecipe(new ItemStack(Material.NAME_TAG));
			nametag.addIngredient(Material.STRING);
			nametag.addIngredient(Material.IRON_INGOT);
			server.addRecipe(nametag);

			ShapedRecipe saddle = new ShapedRecipe(new ItemStack(Material.SADDLE));
			saddle.shape("X X", "XXX");
			saddle.setIngredient('X', Material.LEATHER);
			server.addRecipe(saddle);

			Material ingredient = Material.IRON_FENCE;

			ItemStack helmet = new ItemStack(Material.CHAINMAIL_HELMET);
			ShapedRecipe chainHelmet = new ShapedRecipe(helmet);
			chainHelmet.shape("XXX", "X X");
			chainHelmet.setIngredient('X', ingredient);
			server.addRecipe(chainHelmet);

			ItemStack chestplate = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
			ShapedRecipe chainChestplate = new ShapedRecipe(chestplate);
			chainChestplate.shape("X X", "XXX", "XXX");
			chainChestplate.setIngredient('X', ingredient);
			server.addRecipe(chainChestplate);

			ItemStack leggings = new ItemStack(Material.CHAINMAIL_LEGGINGS);
			ShapedRecipe chainLeggings = new ShapedRecipe(leggings);
			chainLeggings.shape("XXX", "X X", "X X");
			chainLeggings.setIngredient('X', ingredient);
			server.addRecipe(chainLeggings);

			ItemStack boots = new ItemStack(Material.CHAINMAIL_BOOTS);
			ShapedRecipe chainBoots = new ShapedRecipe(boots);
			chainBoots.shape("X X", "X X");
			chainBoots.setIngredient('X', ingredient);
			server.addRecipe(chainBoots);
		}
	}

	public static Plugin getPlugin() {
		return plugin;
	}

}
