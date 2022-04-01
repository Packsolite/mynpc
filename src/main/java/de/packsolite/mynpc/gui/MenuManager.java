package de.packsolite.mynpc.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.liquiddev.util.bukkit.ItemFactory;
import de.liquiddev.util.bukkit.MultiVersion;
import de.liquiddev.util.common.GermanTimeUtil;
import de.packsolite.mynpc.MyNpc;
import de.packsolite.mynpc.Texts;
import de.packsolite.mynpc.gui.menu.NpcEquipMenu;
import de.packsolite.mynpc.npc.Npc;
import lombok.Getter;

@Getter
public class MenuManager {

	private MyNpc mynpc;
	private Map<Player, Menu> currentScreens = new HashMap<>();

	/**
	 * final stuff
	 */

	private Inventory mainMenueInv;

	private ItemStack emptySlotItem, boarderSlotItem, backButton, ownNpcsButton, createNpcButton, nearbyNpcButton, previousPageButton, nextPageButton, teleportButton, editNpcButton, moveButton,
			renameButton, deleteButton, personalizeButton, equipmentButton, lookEnabledButton, lookDisabledButton, sneakEnabledButton, sneakDisabledButton, creatorEnabledButton, creatorDisabledButton,
			helmetItem, chestItem, legsItem, bootsItem, legacyHandItem, handItem, offHandItem, skullPlaceholder;

	public MenuManager(MyNpc mynpc) {
		this.mynpc = mynpc;
		this.prepareInventories();
	}

	public Inventory buildLayoutInventory(String title, int size) {
		String trimmedTitle = Texts.INVENTORY_PREFIX + title;
		if (trimmedTitle.length() > 32) {
			trimmedTitle = trimmedTitle.substring(0, 32);
		}

		Inventory inv = Bukkit.createInventory(null, size, trimmedTitle);

		if (size > 27) {
			for (int i = 0; i < size / 9; i++) {
				inv.setItem(9 * i, this.boarderSlotItem);
				inv.setItem(9 * i + 8, this.boarderSlotItem);
			}
			for (int i = size - 8; i < size; i++) {
				inv.setItem(i, this.boarderSlotItem);
			}
		} else {
			for (int i = 0; i < 9; i++) {
				inv.setItem(i, this.boarderSlotItem);
				inv.setItem(i + 18, this.boarderSlotItem);
			}
		}
		inv.setItem(0, this.backButton);
		return inv;
	}

	/**
	 * Called if a player disconnects.
	 * 
	 * @param player The player disconnecting
	 */
	public void clearMenu(Player player) {
		this.currentScreens.remove(player);
	}

	public Menu getCurrentScreen(Player player) {
		return currentScreens.get(player);
	}

	public ItemStack getItemStackByNpc(Npc npc, boolean details) {
		String name = Texts.ITEM_PREFIX + "§fNPC ID: §b#" + npc.getId();

		ArrayList<String> lore = new ArrayList<String>();
		lore.add("§fName: §b" + npc.getName());

		if (details) {
			String position = "§fPosition: §b" + Math.round(npc.getX()) + " " + Math.round(npc.getY()) + " " + Math.round(npc.getZ());
			String dateCreated = "§fErstellt: §b" + GermanTimeUtil.formatDate(npc.getTimeCreated());
			String owner = "§fBesitzer: §b" + npc.getCreatorName();
			lore.add(position);
			lore.add(dateCreated);
			lore.add(owner);
		}

		String skin = npc.getSkinValue();
		String head = skin == null ? "" : npc.getTextureUrl();

		return ItemFactory.getCustomSkull(name, head, lore.toArray(new String[lore.size()]));
	}

	public List<String> getLoreFromString(String string) {
		List<String> words = Arrays.asList(string.split(" "));
		List<String> lore = new ArrayList<>();

		StringBuilder currentLine = new StringBuilder();

		for (String word : words) {
			int lineLength = currentLine.length();
			if (lineLength == 0) {
				currentLine.append("§7" + word);
			} else if (lineLength < 30) {
				currentLine.append(" " + word);
			} else {
				// new line
				lore.add(currentLine.toString());
				currentLine.setLength(0);
				currentLine.append("§7" + word);
			}
		}
		lore.add(currentLine.toString());
		return lore;
	}

	public void onMenuClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();

		// has menu screen open?
		if (currentScreens.containsKey(player)) {

			Menu currentScreen = currentScreens.get(player);

			// clicked own inventory at bottom?
			if (player.getInventory()
					.equals(event.getClickedInventory())) {
				if (currentScreen instanceof NpcEquipMenu) {
					currentScreen.onClick(event);
				}
				return;
			}

			// null?
			if (event.getCurrentItem() == null && !(currentScreen instanceof NpcEquipMenu)) {
				return;
			}

			if (event.getCurrentItem() != null && event.getSlot() == 0) {
				Menu parentMenu = currentScreen.getParentMenu();

				if (parentMenu != null) {
					this.setCurrentScreen(player, currentScreen.getParentMenu());
				} else {
					player.closeInventory();
					player.updateInventory();
				}

			} else {
				currentScreen.onClick(event);
			}
		}
	}

	private void prepareInventories() {
		this.emptySlotItem = new ItemStack(Material.AIR);
		this.boarderSlotItem = ItemFactory.getItem(MultiVersion.MATERIAL_BLACK_STAINED_GLASS_PANE, " ", 1, (byte) 15);
		this.backButton = ItemFactory.getCustomSkull("§7« §cZurück", ItemFactory.ARROW_LEFT_HEAD_URL);
		this.previousPageButton = ItemFactory.getCustomSkull("§7« §eVorherige Seite", ItemFactory.ARROW_LEFT_HEAD_URL);
		this.nextPageButton = ItemFactory.getCustomSkull("§eNächste Seite §7»", ItemFactory.ARROW_RIGHT_HEAD_URL);

		this.ownNpcsButton = ItemFactory.getItem(MultiVersion.MATERIAL_BED, Texts.ITEM_PREFIX + "§9Eigene NPCs", "§7Alle deine NPCs auf einen Blick.");
		this.nearbyNpcButton = ItemFactory.getItem(Material.NAME_TAG, Texts.ITEM_PREFIX + "§eNPC anzeigen", "§7Zeigt den NPC in deiner Nähe an.");
		this.createNpcButton = ItemFactory.getItem(Material.SLIME_BALL, Texts.ITEM_PREFIX + "§aNPC erstellen", "§7Erstelle einen neuen NPC.");

		this.teleportButton = ItemFactory.getItem(Material.ENDER_PEARL, Texts.ITEM_PREFIX + "§bTeleportieren", "§7Teleportiere dich zu dem NPC.");

		this.editNpcButton = ItemFactory.getItem(MultiVersion.MATERIAL_COMMAND, Texts.ITEM_PREFIX + "§eNPC editieren", "§7Editiere den NPC.");

		this.moveButton = ItemFactory.getItem(MultiVersion.MATERIAL_PISTON, Texts.ITEM_PREFIX + "§bNPC verschieben", 1, "§7Verschiebe den NPC zu deiner Position.");
		this.renameButton = ItemFactory.getItem(Material.NAME_TAG, Texts.ITEM_PREFIX + "§eNPC umbenennen", "§7Benenne den NPC um.");
		this.deleteButton = ItemFactory.getItem(Material.BARRIER, Texts.ITEM_PREFIX + "§cNPC löschen", 1, "§7Lösche den NPC.");
		this.equipmentButton = ItemFactory.getItem(MultiVersion.LEATHER_CHESTPLATE, Texts.ITEM_PREFIX + "§5NPC ausrüsten", 1, "§7Rüste den NPC mit Items aus.");
		this.lookEnabledButton = ItemFactory.getItem(MultiVersion.MATERIAL_EYE_OF_ENDER, Texts.ITEM_PREFIX + "§9Spieler anschauen §7(§aAn§7)", 1, "§7In richtung der Spieler schauen.");
		this.lookDisabledButton = ItemFactory.getItem(MultiVersion.MATERIAL_EYE_OF_ENDER, Texts.ITEM_PREFIX + "§9Spieler anschauen §7(§cAus§7)", 1, "§7In richtung der Spieler schauen.");
		this.sneakEnabledButton = ItemFactory.getItem(MultiVersion.MATERIAL_SHULKER_SHELL, Texts.ITEM_PREFIX + "§9Sneaken §7(§aAn§7)", 1, "§7Den NPC ducken lassen.");
		this.sneakDisabledButton = ItemFactory.getItem(MultiVersion.MATERIAL_SHULKER_SHELL, Texts.ITEM_PREFIX + "§9Sneaken §7(§cAus§7)", 1, "§7Den NPC ducken lassen.");
		this.creatorEnabledButton = ItemFactory.getItem(MultiVersion.MATERIAL_SIGN, Texts.ITEM_PREFIX + "§9Besitzer Anzeigen §7(§aAn§7)", 1, "§7Name des Besitzers über dem NPC anzeigen.");
		this.creatorDisabledButton = ItemFactory.getItem(MultiVersion.MATERIAL_SIGN, Texts.ITEM_PREFIX + "§9Besitzer Anzeigen §7(§cAus§7)", 1, "§7Name des Besitzers über dem NPC anzeigen.");

		this.helmetItem = ItemFactory.getItem(MultiVersion.MATERIAL_SIGN, Texts.ITEM_PREFIX + "§bHelm", 1);
		this.chestItem = ItemFactory.getItem(MultiVersion.MATERIAL_SIGN, Texts.ITEM_PREFIX + "§bBrustplatte", 1);
		this.legsItem = ItemFactory.getItem(MultiVersion.MATERIAL_SIGN, Texts.ITEM_PREFIX + "§bHose", 1);
		this.bootsItem = ItemFactory.getItem(MultiVersion.MATERIAL_SIGN, Texts.ITEM_PREFIX + "§bSchuhe", 1);
		this.legacyHandItem = ItemFactory.getItem(MultiVersion.MATERIAL_SIGN, Texts.ITEM_PREFIX + "§bHand", 1);
		this.handItem = ItemFactory.getItem(MultiVersion.MATERIAL_SIGN, Texts.ITEM_PREFIX + "§bRechte Hand", 1);
		this.offHandItem = ItemFactory.getItem(MultiVersion.MATERIAL_SIGN, Texts.ITEM_PREFIX + "§bLinke Hand", 1);
		this.skullPlaceholder = ItemFactory.getItem(MultiVersion.MATERIAL_SKULL_ITEM, "§8Loading...");
		/*
		 * personalizeButton, equipmentButton, lookEnabledButton, lookDisabledButton,
		 * sneakEnabledButton, sneakDisabledButton;
		 */

		this.mainMenueInv = Bukkit.createInventory(null, 27, Texts.INVENTORY_PREFIX + "Menü");
		this.mainMenueInv.setItem(10, this.ownNpcsButton);
		this.mainMenueInv.setItem(13, this.nearbyNpcButton);
		this.mainMenueInv.setItem(16, this.createNpcButton);

		for (int i = 0; i < 9; i++) {
			this.mainMenueInv.setItem(i, this.boarderSlotItem);
			this.mainMenueInv.setItem(i + 18, this.boarderSlotItem);
		}
	}

	public void setCurrentScreen(Player player, Menu currentScreen) {
		player.closeInventory();
		currentScreen.initGui();
		player.openInventory(currentScreen.getInventory());
		this.currentScreens.put(player, currentScreen);
		currentScreen.onOpen(player);
	}
}
