package de.packsolite.mynpc.gui.menu;

import java.text.NumberFormat;
import java.util.Locale;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.liquiddev.util.bukkit.ItemFactory;
import de.liquiddev.util.bukkit.MultiVersion;
import de.packsolite.mynpc.Texts;
import de.packsolite.mynpc.gui.Menu;
import de.packsolite.mynpc.gui.MenuManager;
import de.packsolite.mynpc.npc.Npc;
import de.packsolite.mynpc.util.Result;

public class NpcInfoMenu extends Menu {

	private static final int teleportButton = 20;
	private static final int editButton = 22;
	private static final int ownerButton = 24;

	private Npc npc;
	private Player player;
	private ItemStack creatorHead;

	public NpcInfoMenu(MenuManager manager, Menu parent, Npc npc, Player player) {
		super(manager, parent);
		this.npc = npc;
		this.player = player;
		String creatorTitle = Texts.ITEM_PREFIX + "§fBesitzer: §b" + npc.getCreatorName();
		int npcCountInt = manager.getMynpc()
				.getNpcmanager()
				.countNpcs(npc.getCreatorUuid());
		String npcCountFormat = NumberFormat.getInstance(Locale.GERMANY)
				.format(npcCountInt);
		String npcCount = "§fNPCs: §b" + npcCountFormat;
		this.creatorHead = ItemFactory.getSkull(creatorTitle, npc.getCreatorName(), npcCount);
	}

	@Override
	public Inventory buildInventory() {
		Inventory inv = this.getMenueManager()
				.buildLayoutInventory("§eNPC #" + this.npc.getId(), 45);

		for (int i = 1; i < 10; i++) {
			inv.setItem(i, this.getMenueManager()
					.getBoarderSlotItem());
		}

		// title icon
		inv.setItem(4, this.getMenueManager()
				.getItemStackByNpc(npc, true));

		// set skull placeholder to avoid loading time on inventory open
		inv.setItem(ownerButton, this.getMenueManager()
				.getSkullPlaceholder());

		if (this.getMenueManager()
				.getMynpc()
				.getNpcmanager()
				.canEditNpc(player, npc)) {
			inv.setItem(teleportButton, this.getMenueManager()
					.getTeleportButton());
			inv.setItem(editButton, this.getMenueManager()
					.getEditNpcButton());
		}
		return inv;
	}

	@Override
	public void onOpen(Player player) {
		// replace placeholder with players head
		this.getInventory()
				.setItem(ownerButton, this.creatorHead);
		super.onOpen(player);
	}

	@Override
	public void onClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		MenuManager menuManager = this.getMenueManager();
		int slot = event.getSlot();

		if (slot == ownerButton) {

			PlayerNpcsMenu npcsMenu = new PlayerNpcsMenu(menuManager, this, this.npc.getCreatorUuid(), this.npc.getCreatorName());
			menuManager.setCurrentScreen(player, npcsMenu);

		}

		if (this.getMenueManager()
				.getMynpc()
				.getNpcmanager()
				.canEditNpc(player, npc)) {
			if (slot == editButton) {

				EditNpcMenu menu = new EditNpcMenu(menuManager, this, npc, player);
				menuManager.setCurrentScreen(player, menu);

			} else if (slot == teleportButton) {

				Result result = menuManager.getMynpc()
						.getNpcmanager()
						.teleportPlayer(player, this.npc);
				if (result == Result.SUCCESS) {
					player.closeInventory();
				} else {
					player.playSound(player.getLocation(), MultiVersion.SOUND_NOTE_BASS, 1.0f, 1.0f);
				}
			}
		}
	}
}
