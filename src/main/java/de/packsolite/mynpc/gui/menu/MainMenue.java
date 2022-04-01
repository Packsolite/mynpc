package de.packsolite.mynpc.gui.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.liquiddev.util.bukkit.MultiVersion;
import de.packsolite.mynpc.Texts;
import de.packsolite.mynpc.gui.Menu;
import de.packsolite.mynpc.gui.MenuManager;
import de.packsolite.mynpc.npc.Npc;
import de.packsolite.mynpc.npc.NpcManager;

public class MainMenue extends Menu {

	public MainMenue(MenuManager manager) {
		super(manager, null);
	}

	@Override
	public Inventory buildInventory() {
		return this.getMenueManager()
				.getMainMenueInv();
	}

	@Override
	public void onClick(InventoryClickEvent event) {

		ItemStack clicked = event.getCurrentItem();
		Player player = (Player) event.getWhoClicked();
		MenuManager menuManager = this.getMenueManager();

		if (clicked.equals(menuManager.getOwnNpcsButton())) {
			String profileUuid = player.getUniqueId()
					.toString();
			String profileName = player.getName();
			PlayerNpcsMenu menu = new PlayerNpcsMenu(menuManager, this, profileUuid, profileName);
			this.getMenueManager()
					.setCurrentScreen((Player) event.getWhoClicked(), menu);
		} else if (clicked.equals(menuManager.getCreateNpcButton())) {
			if (!this.getMenueManager()
					.getMynpc()
					.getNpcmanager()
					.canCreateNPC(player)) {
				player.playSound(player.getLocation(), MultiVersion.SOUND_NOTE_BASS, 1.0f, 1.0f);
				return;
			}
			this.getMenueManager()
					.setCurrentScreen(player, new SelectSkinMenu(this.getMenueManager(), this, null, player));
			player.playSound(player.getLocation(), MultiVersion.SOUND_ORB_PICKUP, 0.8f, 1.0f);
		} else if (clicked.equals(menuManager.getNearbyNpcButton())) {

			NpcManager npcmanager = this.getMenueManager()
					.getMynpc()
					.getNpcmanager();
			Npc npc = npcmanager.getNearestNpc(player.getLocation());

			if (npc == null) {
				player.sendMessage(Texts.PREFIX + Texts.NO_NPC_NEARBY);
				player.playSound(player.getLocation(), MultiVersion.SOUND_NOTE_BASS, 1.0f, 1.0f);
				return;
			}

			if (!npcmanager.canEditNpc(player, npc)) {
				player.playSound(player.getLocation(), MultiVersion.SOUND_NOTE_BASS, 1.0f, 1.0f);
				player.sendMessage(Texts.PREFIX + Texts.NOT_YOUR_NPC);
				return;
			}

			Menu menu = new NpcInfoMenu(this.getMenueManager(), this, npc, player);
			menuManager.setCurrentScreen(player, menu);
		}
	}
}
