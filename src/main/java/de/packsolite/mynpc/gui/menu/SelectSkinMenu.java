package de.packsolite.mynpc.gui.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.liquiddev.util.bukkit.ItemFactory;
import de.liquiddev.util.bukkit.MultiVersion;
import de.liquiddev.util.common.AsyncExecutor;
import de.packsolite.mynpc.MyNpc;
import de.packsolite.mynpc.Texts;
import de.packsolite.mynpc.gui.ChatTaskManager;
import de.packsolite.mynpc.gui.Menu;
import de.packsolite.mynpc.gui.MenuManager;
import de.packsolite.mynpc.npc.Npc;
import de.packsolite.mynpc.npc.Skins;
import de.packsolite.mynpc.util.Result;

public class SelectSkinMenu extends Menu {

	private static ItemStack[] skinStacks = new ItemStack[Skins.values().length];

	static {
		for (int i = 0; i < skinStacks.length; i++) {
			Skins skin = Skins.values()[i];
			skinStacks[i] = ItemFactory.getCustomSkull("§a" + skin.toString(), skin.getTextureURL());
		}
	}

	private Npc npc;
	private Player player;

	public SelectSkinMenu(MenuManager manager, Menu parent, Npc npc, Player player) {
		super(manager, parent);
		this.npc = npc;
		this.player = player;
	}

	@Override
	public Inventory buildInventory() {
		Inventory inv = this.getMenueManager()
				.buildLayoutInventory("§eSkin auswählen", 27);

		for (int i = 1; i < 9; i++) {
			inv.setItem(i, this.getMenueManager()
					.getBoarderSlotItem());
		}

		int slot = 0;
		for (int i = 0; i < Skins.values().length; i++) {
			if (i == 7 || i == 14) {
				slot += 3;
			} else {
				slot++;
			}
			Skins skin = Skins.values()[i];
			ItemStack skull = Skins.Du.equals(skin) ? ItemFactory.getSkull("§a" + this.player.getName(), this.player.getName(), 1) : skinStacks[i];
			inv.setItem(9 + slot, skull);
		}
		return inv;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		int slot = event.getSlot();

		if (event.getCurrentItem() == null || event.getCurrentItem()
				.getType() != MultiVersion.MATERIAL_SKULL_ITEM)
			return;

		int id = slot - 10;

		if (slot > 25) {
			id -= 4;
		} else if (slot > 16) {
			id -= 2;
		}

		if (id < 0 || id >= Skins.values().length)
			return;

		Skins skin = Skins.values()[id];

		if (Skins.Custom.equals(skin)) {

			player.closeInventory();
			player.sendTitle(Texts.ENTER_SKIN_TO_CHAT_TITLE, Texts.ENTER_SKIN_TO_CHAT_SUBTITLE);
			player.sendMessage(Texts.PREFIX + Texts.ENTER_SKIN_TO_CHAT);
			player.playSound(player.getLocation(), MultiVersion.SOUND_ORB_PICKUP, 1.0f, 1.0f);
			ChatTaskManager taskManager = this.getMenueManager()
					.getMynpc()
					.getChatTaskManager();

			taskManager.addTask(player, message -> {

				// valid player skin name?
				if (!this.getMenueManager()
						.getMynpc()
						.getNpcmanager()
						.isNameValid(message)) {
					player.sendMessage(Texts.PREFIX + Texts.SKIN_NOT_FOUND);
					player.playSound(player.getLocation(), MultiVersion.SOUND_NOTE_BASS, 1.0f, 1.0f);
					return false;
				}

				// run skin fetcher async
				AsyncExecutor.execute(() -> {

					skin.loadCustom(message);

					// set skin sync
					Bukkit.getScheduler()
							.runTask(MyNpc.getInstance(), () -> {
								this.setSkin(player, skin, message);
							});
				});
				return true;
			});
		} else {
			this.setSkin(player, skin, Skins.Du.equals(skin) ? player.getName() : skin.toString());
		}
	}

	private boolean setSkin(Player player, Skins skin, String name) {

		if (this.npc == null) {
			name = "§7§l" + name;
			if (name.length() > 16) {
				name = name.substring(0, 16);
			}
			Result result = this.getMenueManager()
					.getMynpc()
					.getNpcmanager()
					.createNpc(player, name, skin);

			if (result != Result.SUCCESS) {
				player.playSound(player.getLocation(), MultiVersion.SOUND_NOTE_BASS, 1.0f, 1.0f);
				return false;
			} else {
				player.closeInventory();
				return true;
			}
		} else {
			Result result = this.getMenueManager()
					.getMynpc()
					.getNpcmanager()
					.setSkin(player, this.npc, skin);
			if (result != Result.SUCCESS) {
				player.playSound(player.getLocation(), MultiVersion.SOUND_NOTE_BASS, 1.0f, 1.0f);
				return false;
			} else {
				player.closeInventory();
				return true;
			}
		}

	}
}
