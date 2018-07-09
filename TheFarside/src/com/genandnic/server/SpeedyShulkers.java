package com.genandnic.server;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BlockStateMeta;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class SpeedyShulkers implements Listener {

	private static BiMap<Inventory, ItemStack> map = HashBiMap.create();

	// if two players open an empty shulker box of the same color, errors occur (value already present) ???
	// exiting a container within another container fails to remove the lore from the contained container

	public SpeedyShulkers() {

	}

	private static final String message = "Currently viewing...";

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {

		if (event.getAction() != Action.RIGHT_CLICK_AIR)
			return;

		// System.out.println(event.getPlayer().getInventory().getHeldItemSlot());

		Player player = event.getPlayer();

		// if (event.getMaterial() == Material.ENDER_CHEST) {
		// player.playSound(player.getLocation(), Sound.BLOCK_ENDERCHEST_OPEN, 0.3F, 1.0F);
		// player.openInventory(player.getEnderChest());
		// return;
		// }

		PlayerInventory inventory = player.getInventory();

		if (openContainer(player, new InventoryClick(inventory, inventory.getHeldItemSlot())))
			event.setCancelled(true);

	}

	class InventoryClick {

		private InventoryView invView;
		private int slot;

		public InventoryClick(InventoryView invView, int slot) {

			this.invView = invView;
			this.slot = slot;

		}

		public InventoryClick(Inventory inventory, int slot) {

			this.invView = new InventoryView() {

				@Override
				public Inventory getBottomInventory() {
					return inventory;
				}

				@Override
				public HumanEntity getPlayer() {
					return null;
				}

				@Override
				public Inventory getTopInventory() {
					return null;
				}

				@Override
				public InventoryType getType() {
					return null;
				}
			};

			this.slot = slot;
		}

		public InventoryView getInventoryView() {
			return invView;
		}

		public int getSlot() {
			return slot;
		}

		public ItemStack getItem() {
			return invView.getBottomInventory().getItem(slot);
		}

	}

	private static Set<InventoryClick> set = new HashSet<InventoryClick>();

	@EventHandler
	public void onClick(InventoryClickEvent event) {

		// System.out.println("inventory: " + event.getSlot());
		// Inventory inventory = event.getView().getTopInventory();

		ItemStack current = event.getCurrentItem();
		if (current == null)
			return;

		// Prevents you from opening a container that is already being viewed
		// ItemMeta meta = current.getItemMeta();
		// if (meta != null && meta.hasLore() && meta.getLore().contains(message)) {
		// event.setCancelled(true);
		// return;
		// }

		// if (map.containsKey(inventory) && meta != null && meta.hasLore() && meta.getLore().contains(message)) {
		// event.setCancelled(true);
		// return;
		// }

		// Opens containers right clicked within the player's inventory or enderchest
		Player player = (Player) event.getWhoClicked();
		Inventory clickedInv = event.getClickedInventory();

		if (player == clickedInv.getHolder() || clickedInv.getType() == InventoryType.ENDER_CHEST) {

			InventoryClick invClick = new InventoryClick(clickedInv, event.getSlot());
			for (InventoryClick click : set) {
				// System.out.println("Check inventory: " + click.getItem().hashCode());
				// System.out.println("Check slot: " + click.getSlot());
				if (click.getSlot() == invClick.getSlot()) {
					System.out.println("Container is already open!");
					event.setCancelled(true);
					return;
				}
			}

			if ((event.isRightClick() && current.getAmount() == 1) || event.getClick() == ClickType.MIDDLE) {
				if (event.getCursor().getType() == Material.AIR) {

					// if (current.getType() == Material.ENDER_CHEST) {
					// if (inventory.getType() == InventoryType.ENDER_CHEST || inventory.getTitle().contains("Ender Chest"))
					// return;
					// event.setCancelled(true);
					// player.playSound(player.getLocation(), Sound.BLOCK_ENDERCHEST_OPEN, 0.3F, 1.0F);
					// player.openInventory(player.getEnderChest());
					// return;
					// }

					if (openContainer(player, invClick))
						event.setCancelled(true);
				}
			}
		}

		return;
		// if (player == clicked.getHolder() || clicked.getType() == InventoryType.ENDER_CHEST) {
		// if ((event.isRightClick() && current.getAmount() == 1) || event.getClick() == ClickType.MIDDLE) {
		// if (event.getCursor().getType() == Material.AIR) {
		//
		// // if (current.getType() == Material.ENDER_CHEST) {
		// // if (inventory.getType() == InventoryType.ENDER_CHEST || inventory.getTitle().contains("Ender Chest"))
		// // return;
		// // event.setCancelled(true);
		// // player.playSound(player.getLocation(), Sound.BLOCK_ENDERCHEST_OPEN, 0.3F, 1.0F);
		// // player.openInventory(player.getEnderChest());
		// // return;
		// // }
		//
		// if (openContainer(player, current))
		// event.setCancelled(true);
		// }
		// }
		// }

	}

	@EventHandler
	public void onClose(InventoryCloseEvent event) {
		// System.out.println("close Event");
		saveOpenContainer(event.getView());
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		// saveOpenContainer(event.getEntity().getOpenInventory());
	}

	private boolean openContainer(Player player, InventoryClick invClick) {

		ItemStack item = invClick.getItem();

		System.out.println(item.getType().name());
		if (!(item.getItemMeta() instanceof BlockStateMeta))
			return false;
		BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();

		// List<String> lore = new ArrayList<String>();
		// if (meta.hasLore())
		// lore = meta.getLore();

		Inventory inventory = null;

		if (item.getType() == Material.ENDER_CHEST) {

			inventory = player.getEnderChest();
			player.playSound(player.getLocation(), Sound.BLOCK_ENDERCHEST_OPEN, 0.3F, 1.0F);

		} else if (meta.getBlockState() instanceof Container) {

			Container container = (Container) meta.getBlockState();
			Inventory itemInventory = container.getInventory();

			if (container instanceof ShulkerBox) {

				// Sets the title of the shulker box for clarity (e.g. Yellow Shulker Box)
				String title = meta.getDisplayName();
				if (title == null)
					title = WordUtils.capitalizeFully(item.getType().name().replace("_", " "));

				inventory = Bukkit.createInventory(player, InventoryType.SHULKER_BOX, title);
				player.playSound(player.getLocation(), Sound.BLOCK_SHULKER_BOX_OPEN, 0.3F, 1.0F);

				// if (meta.getDisplayName() != null)
				// meta.setDisplayName(ChatColor.RESET + ChatColor.stripColor(meta.getDisplayName()));

			} else if (player.getGameMode() == GameMode.CREATIVE) {

				inventory = Bukkit.createInventory(player, itemInventory.getType());
				if (container instanceof Chest)
					player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.3F, 1.0F);

			} else
				return false;

			inventory.setContents(itemInventory.getContents());

			itemInventory.clear();
			meta.setBlockState(container);

		} else
			return false;

		// lore.add(message);
		// meta.setLore(lore);
		// item.setItemMeta(meta);

		set.add(invClick);
		player.openInventory(inventory);
		Bukkit.broadcastMessage("Opened container " + item.getType().name() + " [" + set.size() + "]");
		return true;
	}

	private static void saveOpenContainer(InventoryView invView) {

		// if inventory equals public ender chest, inventory = player.getEnderChest()

		Inventory inventory = invView.getBottomInventory();

		// System.out.println("view check: " + inventory.getName());
		InventoryClick invClick = null;
		for (InventoryClick activeView : set) {

			// System.out.println("activeView check: " + activeView.getInventoryView().getBottomInventory().getName());

			if (activeView.getInventoryView().getBottomInventory() == invView.getBottomInventory())
				invClick = activeView;

		}

		if (invClick == null) {
			System.out.println("view is null");
			return;
		}

		set.remove(invClick);

		Player player = (Player) inventory.getViewers().get(0);

		ItemStack item = invClick.getItem();
		BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
		if (item.getType() == Material.ENDER_CHEST) {

			player.playSound(player.getLocation(), Sound.BLOCK_ENDERCHEST_CLOSE, 0.3F, 1.0F);

		} else if (meta.getBlockState() instanceof Container) {
			Container container = (Container) meta.getBlockState();
			container.getInventory().setContents(invView.getTopInventory().getContents());

			if (item.getType().name().contains("SHULKER_BOX"))
				player.playSound(player.getLocation(), Sound.BLOCK_SHULKER_BOX_CLOSE, 0.3F, 1.0F);
			else {
				if (item.getType().name().contains("CHEST"))
					player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.3F, 1.0F);
				// if (!lore.contains("(+NBT)"))
				// lore.add("(+NBT)");
			}

			meta.setBlockState(container);
		}

		// meta.setLore(lore);
		item.setItemMeta(meta);

		player.getInventory().setItem(invClick.getSlot(), item);

		Bukkit.broadcastMessage("Saved to container " + item.getType().name() + "[" + set.size() + "]");
		set.remove(invClick);
	}

	public static void saveShulkerBoxes() {
		// for (Player player : Bukkit.getOnlinePlayers()) {
		// Inventory inventory = player.getOpenInventory().getTopInventory();
		// // if inventory equals public ender chest, inventory = player.getEnderchest()
		//
		// if (map.containsKey(inventory)) {
		// player.closeInventory();
		// saveOpenContainer(player);
		// }
		// }
		//
		// map = null;
	}

}
