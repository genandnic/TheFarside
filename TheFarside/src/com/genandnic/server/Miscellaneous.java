package com.genandnic.server;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.spigotmc.event.entity.EntityMountEvent;

public class Miscellaneous implements Listener {

	private Main plugin;
	private FileConfiguration config;

	private final Random random = new Random();

	private final boolean dropPlayerHeads;
	private final boolean piggybackRides;
	private final boolean hideSpectators;
	private final double elytraSprintBoost;

	public Miscellaneous(Main plugin) {
		this.plugin = plugin;
		this.config = plugin.getConfig();

		dropPlayerHeads = config.getBoolean("dropPlayerHeads");
		piggybackRides = config.getBoolean("piggybackRides");
		hideSpectators = config.getBoolean("hideSpectators");
		elytraSprintBoost = config.getDouble("elytraSprintBoost");
	}

	// A chime is played upon a player logging in, spectators join silently
	@EventHandler
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

	// Spectators leave silently, prevents player from logging out with the player they're riding
	@EventHandler
	public void onPlayerDisconnect(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		if (player.getGameMode() == GameMode.SPECTATOR)
			event.setQuitMessage(null);

		if (player.getVehicle() instanceof Player)
			player.leaveVehicle();
	}

	// Spectators are hidden from the tab menu, to give the impression of absence
	@EventHandler
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

	// Horses are tamed instantly, Players can give each other piggy back rides if sneaking while right clicked
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		Entity entity = event.getRightClicked();
		if (entity instanceof AbstractHorse) {
			((AbstractHorse) entity).setTamed(true);
		} else if (piggybackRides && entity instanceof Player) {
			Player player = (Player) event.getRightClicked();

			if (player.isSneaking() && player.getLocation().getBlock().getRelative(0, 2, 0).getType().isTransparent())
				player.addPassenger(event.getPlayer());
		}
	}

	// Damage from small falls are cancelled, Leashed mobs are also immune to fall damage
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {

		if (event.getCause() == DamageCause.FALL && (event.getDamage() < 5 || ((LivingEntity) event.getEntity()).isLeashed()))
			event.setCancelled(true);

	}

	// Projectiles like snowballs and eggs give knockback upon hit, enderpearls do less damage
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (!event.isCancelled() && event.getEntity() instanceof LivingEntity) {

			LivingEntity entity = (LivingEntity) event.getEntity();

			if (event.getDamage() == 0) {
				event.setDamage(0.1);
				return;
			}

			Entity damager = event.getDamager();
			if (damager instanceof EnderPearl) {

				event.setCancelled(true);
				entity.damage(1);

				Location location = entity.getLocation();
				location.getWorld().playSound(location, Sound.ENTITY_ENDERMEN_TELEPORT, 1F, 1F);

				return;
			}

			if (damager instanceof Player && entity instanceof Player) {
				damager.eject();
				return;
			}

		}
	}

	// Player heads are dropped upon being killed by another player
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (dropPlayerHeads && event.getEntity().getKiller() instanceof Player) {

			ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
			SkullMeta meta = (SkullMeta) head.getItemMeta();
			meta.setOwner(event.getEntity().getName());
			head.setItemMeta(meta);

			event.getDrops().add(head);
		}
	}

	// Enderdragon drops head and wings upon death
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		if (event.getEntityType() == EntityType.ENDER_DRAGON) {
			event.getDrops().add(new ItemStack(Material.ELYTRA));
			event.getDrops().add(new ItemStack(Material.SKULL_ITEM, 1, (byte) 5));
		}
	}

	// Villagers have a chance to sell various records
	@EventHandler
	public void onVillagerLevelUp(VillagerAcquireTradeEvent event) {
		Villager villager = event.getEntity();
		if (villager.getProfession() == Profession.FARMER && random.nextInt(7) == 0) {
			String record = "RECORD_" + Math.max(3, random.nextInt(13));
			MerchantRecipe recipe = new MerchantRecipe(new ItemStack(Material.valueOf(record)), 1);
			recipe.addIngredient(new ItemStack(Material.EMERALD, Math.max(7, random.nextInt(22))));
			event.setRecipe(recipe);
		}
	}

	// Minecarts are given a minor speed boost once mounted
	public void onEntityMount(EntityMountEvent event) {
		Entity vehicle = event.getMount();
		if (vehicle instanceof Minecart) {
			Minecart minecart = (Minecart) vehicle;
			minecart.setMaxSpeed(.5);
			minecart.setFlyingVelocityMod(new Vector(10, 1, 10));
			minecart.setDerailedVelocityMod(new Vector(.9, .9, .9));
		}

	}

	// Reverts the changes made to zombies in minecraft 1.6.1, making them less horde like
	@EventHandler
	public void onEntitySpawn(CreatureSpawnEvent event) {
		LivingEntity entity = event.getEntity();
		if (entity.getType() == EntityType.ZOMBIE) {
			entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(16);
			entity.getAttribute(Attribute.ZOMBIE_SPAWN_REINFORCEMENTS).setBaseValue(0);
			entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(0);
		}
	}

	// Players with elytras automatically glide when using a rocket in mid-air
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (!player.isOnGround() && event.getAction() == Action.RIGHT_CLICK_AIR && event.getMaterial() == Material.FIREWORK) {
			// player.setCooldown(Material.FIREWORK, 20);

			ItemStack chestplate = player.getEquipment().getChestplate();
			if (chestplate != null && chestplate.getType() == Material.ELYTRA)
				player.setGliding(true);
		}

	}

	// Sprinting when using the elytra grants a speed boost, allowing infinite flight
	// Sneaking when using the elytra allows one to stall in mid-air
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (player.isGliding()) {
			Vector velocity = player.getVelocity();

			if (player.isSneaking() && velocity.getY() < -0.1) {
				player.setVelocity(velocity.setY(-0.1));
			} else if (player.isSprinting()) {
				Vector vector = new Vector(0, player.getLocation().getDirection().getY(), 0);
				player.setVelocity(velocity.add(vector.multiply(elytraSprintBoost)));
			}

			if (velocity.length() > 0.75)
				player.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, player.getLocation(), 0);
		}
	}

	// Stop elytra from activating on the ground, Plays a sound signaling when the elytra has been open/closed.
	@EventHandler
	public void onEntityGlide(EntityToggleGlideEvent event) {
		Entity entity = event.getEntity();
		if (event.isGliding() && entity.getLocation().getBlock().getRelative(0, -2, 0).getType().isSolid()) {
			event.setCancelled(true);
			return;
		}

		entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ENDERDRAGON_FLAP, 0.5F, 1.5F);
	}

	// All mobs can be leashed if clicked on with a lead
	@EventHandler
	public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		ItemStack item = player.getEquipment().getItemInMainHand();

		if (item == null || item.getType() != Material.LEASH)
			item = player.getEquipment().getItemInOffHand();

		Entity entity = event.getRightClicked();
		if (item != null && item.getType() == Material.LEASH && entity instanceof LivingEntity) {

			if (player.getCooldown(Material.LEASH) > 0) {
				event.setCancelled(true);
				return;
			}

			if (((LivingEntity) entity).isLeashed())
				return;

			if (((LivingEntity) entity).setLeashHolder(player)) {
				player.setCooldown(Material.LEASH, 1);

				if (item.getAmount() > 0)
					item.setAmount(item.getAmount() - 1);

				player.updateInventory();
				event.setCancelled(true);
			}
		}

	}
}
