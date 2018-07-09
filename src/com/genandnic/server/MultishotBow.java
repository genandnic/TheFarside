package com.genandnic.server;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Arrow.PickupStatus;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TippedArrow;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionData;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class MultishotBow implements Listener {

	private Plugin plugin;

	public MultishotBow(Main plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onBowShoot(EntityShootBowEvent event) {

		if (event.getForce() < 0.75)
			return;

		ItemStack bow = event.getBow();

		if (bow.containsEnchantment(Enchantment.SWEEPING_EDGE)) {

			ProjectileSource entity = event.getEntity();
			Arrow arrow = (Arrow) event.getProjectile();
			Vector vector = arrow.getVelocity();

			Class<? extends Arrow> instance = arrow.getClass();

			boolean critical = arrow.isCritical();
			boolean gravity = arrow.hasGravity();

			int fire = arrow.getFireTicks();
			int knockback = arrow.getKnockbackStrength();

			PotionData potion = null;
			if (arrow instanceof TippedArrow)
				potion = ((TippedArrow) arrow).getBasePotionData();

			Location location = ((Entity) entity).getLocation();
			World world = location.getWorld();

			int level = bow.getEnchantmentLevel(Enchantment.SWEEPING_EDGE);

			for (int i = -level; i <= level; i = i + 1) {

				final int angle = i;
				final PotionData effect = potion;

				new BukkitRunnable() {
					@Override
					public void run() {

						if (angle != 0) {
							world.playSound(location, Sound.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F);
							Arrow arrow = entity.launchProjectile(instance, rotateYAxis(vector, angle));

							arrow.setPickupStatus(PickupStatus.CREATIVE_ONLY);
							arrow.setCritical(critical);
							arrow.setFireTicks(fire);
							arrow.setKnockbackStrength(knockback);
							arrow.setGravity(gravity);

							if (effect != null)
								((TippedArrow) arrow).setBasePotionData(effect);
						}

					}
				}.runTaskLater(plugin, Math.abs(angle));

			}
		}
	}

	@EventHandler
	public void onHit(ProjectileHitEvent event) {
		Projectile arrow = event.getEntity();
		if (!(arrow instanceof Arrow) || ((Arrow) arrow).getPickupStatus().equals(PickupStatus.ALLOWED))
			return;

		if (event.getHitEntity() != null) {
			arrow.remove();
		} else {

			new BukkitRunnable() {
				@Override
				public void run() {
					arrow.remove();
				}
			}.runTaskLater(plugin, 200);

		}
	}

	@EventHandler
	public void onVillagerLevelUp(VillagerAcquireTradeEvent event) {
		Villager villager = event.getEntity();
		if (villager.getProfession() == Profession.BLACKSMITH && random.nextInt(7) == 0) {

			int level = Math.max(1, random.nextInt(4));

			ItemStack result = new ItemStack(Material.BOW);
			result.addUnsafeEnchantment(Enchantment.SWEEPING_EDGE, level);

			MerchantRecipe recipe = new MerchantRecipe(result, 100);
			recipe.addIngredient(new ItemStack(Material.EMERALD, level * 14));
			event.setRecipe(recipe);

		}
	}

	private final Random random = new Random();

	private static Vector rotateYAxis(Vector dir, double angleD) {
		double angleR = Math.toRadians(angleD);
		double x = dir.getX();
		double z = dir.getZ();
		double cos = Math.cos(angleR);
		double sin = Math.sin(angleR);
		return (new Vector(x * cos + z * (-sin), dir.getY(), x * sin + z * cos));
	}
}
