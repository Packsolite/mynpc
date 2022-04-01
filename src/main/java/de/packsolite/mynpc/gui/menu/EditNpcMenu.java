package de.packsolite.mynpc.gui.menu;

import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.liquiddev.util.bukkit.ItemFactory;
import de.liquiddev.util.bukkit.MultiVersion;
import de.liquiddev.util.bukkit.labymod.LabymodController;
import de.packsolite.mynpc.MyNpc;
import de.packsolite.mynpc.Texts;
import de.packsolite.mynpc.gui.ChatTaskManager;
import de.packsolite.mynpc.gui.Menu;
import de.packsolite.mynpc.gui.MenuManager;
import de.packsolite.mynpc.gui.ChatTaskManager.ChatTask;
import de.packsolite.mynpc.npc.Npc;
import de.packsolite.mynpc.util.LabymodEmote;
import de.packsolite.mynpc.util.Result;

public class EditNpcMenu extends Menu {

	private static final int changeSkinButton = 11;
	private static final int changeEquipmentButton = 12;
	private static final int changeActionTextButton = 13;
	private static final int toggleFollowHeadButton = 14;
	private static final int toggleSneakButton = 15;

	private static final int toggleCreatorButton = 20;
	private static final int renameButton = 21;
	private static final int moveButton = 23;
	private static final int deleteButton = 24;

	private static final int changeEmoteButton = 22;

	private Npc npc;
	private Player view;
	private long lastClick;

	public EditNpcMenu(MenuManager manager, Menu parent, Npc npc, Player player) {
		super(manager, parent);
		this.npc = npc;
		this.view = player;
	}

	@Override
	public Inventory buildInventory() {
		MenuManager menuman = getMenueManager();
		Inventory inv = this.getMenueManager()
				.buildLayoutInventory("§eEditiere #" + this.npc.getId(), 36);
		for (int i = 1; i < 10; i++) {
			inv.setItem(i, this.getMenueManager()
					.getBoarderSlotItem());
		}

		inv.setItem(moveButton, this.getMenueManager()
				.getMoveButton());
		inv.setItem(renameButton, this.getMenueManager()
				.getRenameButton());
		inv.setItem(deleteButton, this.getMenueManager()
				.getDeleteButton());
		inv.setItem(changeEquipmentButton, this.getMenueManager()
				.getEquipmentButton());

		// change skin button
		ItemStack skin = ItemFactory.getCustomSkull(Texts.ITEM_PREFIX + "§eSkin ändern", this.npc.getTextureUrl(), "§7Ändere den Skin des NPCs");
		inv.setItem(changeSkinButton, skin);

		// click action button
		if (this.npc.getClickActionText()
				.length() > 0) {
			ItemStack clickAction = ItemFactory.getItem(Material.PAPER, Texts.ITEM_PREFIX + "§dRechtsklick-Text", this.getMenueManager()
					.getLoreFromString(this.npc.getClickActionText()));
			inv.setItem(changeActionTextButton, clickAction);
		} else {
			ItemStack clickAction = ItemFactory.getItem(Material.PAPER, Texts.ITEM_PREFIX + "§dRechtsklick-Text", "§7Lass den NPC etwas sagen.");
			inv.setItem(changeActionTextButton, clickAction);
		}

		// labymod emote button
		Optional<LabymodController> lmc = MyNpc.getInstance()
				.getLabymodController();
		if (lmc.isPresent() && lmc.get()
				.isLabyModUser(view)) {
			LabymodEmote emote = LabymodEmote.getById(npc.getEmoteId());
			ItemStack emoteStack = ItemFactory.getItem(Material.DROPPER, Texts.ITEM_PREFIX + "§6Labymod Emote §7(" + emote.getDisplayName() + "§7)", "§7Ändere den Emote des NPCs.");
			inv.setItem(changeEmoteButton, emoteStack);
		}

		// toggle buttons
		inv.setItem(toggleCreatorButton, this.npc.isShowCreator() ? menuman.getCreatorEnabledButton() : menuman.getCreatorDisabledButton());
		inv.setItem(toggleFollowHeadButton, this.npc.isFollowHead() ? menuman.getLookEnabledButton() : menuman.getLookDisabledButton());
		inv.setItem(toggleSneakButton, this.npc.isSneak() ? menuman.getSneakEnabledButton() : menuman.getSneakDisabledButton());
		return inv;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(InventoryClickEvent event) {
		MenuManager menuman = getMenueManager();
		Player player = (Player) event.getWhoClicked();

		if (event.getCurrentItem() == null || event.getCurrentItem()
				.getType() == Material.AIR) {
			return;
		}

		// prevent spam
		if (System.currentTimeMillis() - lastClick < 500)
			return;

		if (!this.getMenueManager()
				.getMynpc()
				.getNpcmanager()
				.canEditNpc(player, npc)) {
			player.sendMessage(Texts.PREFIX + Texts.NOT_YOUR_NPC);
			player.playSound(player.getLocation(), MultiVersion.SOUND_NOTE_BASS, 1.0f, 1.0f);
			return;
		}

		int slot = event.getSlot();

		if (slot == moveButton) {
			this.getMenueManager()
					.getMynpc()
					.getNpcmanager()
					.moveNpc(player, npc);
			player.closeInventory();
		} else if (slot == renameButton) {
			player.closeInventory();
			player.sendTitle(Texts.ENTER_SKIN_TO_CHAT_TITLE, Texts.ENTER_NEW_NAME_TO_CHAT_SUBTITLE);
			player.sendMessage(Texts.PREFIX + Texts.ENTER_NEW_NAME_TO_CHAT);
			player.playSound(player.getLocation(), MultiVersion.SOUND_ORB_PICKUP, 1.0f, 1.0f);
			ChatTaskManager taskManager = this.getMenueManager()
					.getMynpc()
					.getChatTaskManager();

			taskManager.addTask(player, message -> {
				Result result = this.getMenueManager()
						.getMynpc()
						.getNpcmanager()
						.renameNpc(player, npc, message.replace("&", "§")
								.replace("§4", "§c"));

				if (result == Result.SUCCESS) {
					return true;
				} else {
					player.playSound(player.getLocation(), MultiVersion.SOUND_NOTE_BASS, 1.0f, 1.0f);
					return false;
				}
			});
		} else if (slot == deleteButton) {

			player.closeInventory();
			player.sendTitle(Texts.CONFIRM_DELETE_IN_CHAT_TITLE, Texts.CONFIRM_DELETE_IN_CHAT_SUBTITLE);
			player.sendMessage(Texts.PREFIX + Texts.CONFIRM_DELETE_IN_CHAT);
			player.playSound(player.getLocation(), MultiVersion.SOUND_ORB_PICKUP, 1.0f, 1.0f);

			this.getMenueManager()
					.getMynpc()
					.getChatTaskManager()
					.addTask(player, new ChatTask() {
						@Override
						public boolean onChatMessage(String message) {

							// was deleted while he was in gui?
							if (!getMenueManager().getMynpc()
									.getNpcmanager()
									.getNpcs()
									.contains(npc)) {
								player.sendMessage(Texts.PREFIX + Texts.NPC_NOT_FOUND);
								return false;
							}

							if (!message.equalsIgnoreCase("confirm")) {
								menuman.setCurrentScreen(player, EditNpcMenu.this);
								player.playSound(player.getLocation(), MultiVersion.SOUND_NOTE_BASS, 1.0f, 1.0f);
								return false;
							}

							Result result = menuman.getMynpc()
									.getNpcmanager()
									.deleteNpc(player, npc);

							if (result == Result.SUCCESS) {
								// create new parent screen to update existing lists
								if (getParentMenu() == null || getParentMenu().getParentMenu() == null)
									return true;

								Menu parentScreen = new PlayerNpcsMenu(getMenueManager(), getParentMenu().getParentMenu()
										.getParentMenu(), npc.getCreatorUuid(), npc.getCreatorName());
								menuman.setCurrentScreen(player, parentScreen);
								return true;
							} else {
								Menu parentScreen = getParentMenu();
								menuman.setCurrentScreen(player, parentScreen);
								player.playSound(player.getLocation(), MultiVersion.SOUND_NOTE_BASS, 1.0f, 1.0f);
								return false;
							}
						}
					});
		} else if (slot == changeActionTextButton) {
			player.closeInventory();
			player.sendTitle(Texts.ENTER_TEXT_TO_CHAT_TITLE, Texts.ENTER_TEXT_TO_CHAT_SUBTITLE);
			player.sendMessage(Texts.PREFIX + Texts.ENTER_TEXT_TO_CHAT);
			player.playSound(player.getLocation(), MultiVersion.SOUND_ORB_PICKUP, 1.0f, 1.0f);

			ChatTaskManager taskManager = this.getMenueManager()
					.getMynpc()
					.getChatTaskManager();
			taskManager.addTask(player, message -> {
				Result result = this.getMenueManager()
						.getMynpc()
						.getNpcmanager()
						.setClickActionText(player, npc, message.replace("&", "§")
								.replace("§4", "§c"));
				if (result == Result.SUCCESS) {
					this.getMenueManager()
							.setCurrentScreen(player, this);
					return true;
				}
				player.playSound(player.getLocation(), MultiVersion.SOUND_NOTE_BASS, 1.0f, 1.0f);
				return false;
			});

		} else if (slot == changeSkinButton) {

			Menu skinMenu = new SelectSkinMenu(this.getMenueManager(), this, this.npc, player);
			menuman.setCurrentScreen(player, skinMenu);

		} else if (slot == changeEquipmentButton) {

			Menu equipMenu = new NpcEquipMenu(this.getMenueManager(), this, this.npc);
			menuman.setCurrentScreen(player, equipMenu);

		} else if (slot == changeEmoteButton) {

			Menu menu = new EmoteMenu(menuman, this, this.npc, player);
			menuman.setCurrentScreen(player, menu);

		} else if (slot == toggleCreatorButton) {
			player.closeInventory();

			if (npc.isShowCreator()) {
				player.sendMessage(Texts.PREFIX + Texts.SHOW_CREATOR_DISABLED);
				npc.setShowCreator(false);
			} else {
				player.sendMessage(Texts.PREFIX + Texts.SHOW_CREATOR_ENABLED);
				npc.setShowCreator(true);
			}

			this.getMenueManager()
					.getMynpc()
					.getFileManager()
					.getNpcRepo()
					.updateNpc(npc);
			player.openInventory(this.buildInventory());
		} else if (slot == toggleFollowHeadButton) {

			this.getMenueManager()
					.getMynpc()
					.getNpcmanager()
					.setFollowHead(player, npc, !this.npc.isFollowHead());
			player.openInventory(this.buildInventory());

		} else if (slot == toggleSneakButton) {

			this.getMenueManager()
					.getMynpc()
					.getNpcmanager()
					.setSneak(player, npc, !this.npc.isSneak());
			player.openInventory(this.buildInventory());

		} else {
			// clicked empty
			return;
		}

		this.lastClick = System.currentTimeMillis();
	}
}
