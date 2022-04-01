package de.packsolite.mynpc.gui.menu;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.liquiddev.util.bukkit.ItemFactory;
import de.liquiddev.util.bukkit.MultiVersion;
import de.packsolite.mynpc.Texts;
import de.packsolite.mynpc.gui.Menu;
import de.packsolite.mynpc.gui.MenuManager;
import de.packsolite.mynpc.npc.Npc;
import de.packsolite.mynpc.util.LabymodEmote;
import de.packsolite.mynpc.util.Result;

public class EmoteMenu extends ListMenu<LabymodEmote> {

	private Npc npc;
	private Player view;

	public EmoteMenu(MenuManager manager, Menu parent, Npc npc, Player view) {
		super(manager, parent);
		this.npc = npc;
		this.view = view;
	}

	@Override
	public void buildInventory(Inventory inventory) {

	}

	@Override
	public String[] getDescriptorLore() {
		return new String[] { "§eEmotes: §f" + NumberFormat.getInstance(Locale.GERMANY)
				.format(this.getEntries()
						.size()),
				"§eSeite: §f" + (this.getPage() + 1) + " §7/ " + ((this.getEntries()
						.size() / 35) + 1) };
	}

	@Override
	public List<ListItem<LabymodEmote>> getEntries() {
		List<ListItem<LabymodEmote>> list = new ArrayList<ListMenu.ListItem<LabymodEmote>>();
		for (LabymodEmote emote : LabymodEmote.values()) {
			if (this.view.hasPermission(emote.getPermission())) {
				ListItem<LabymodEmote> item = new ListItem<LabymodEmote>() {
					@Override
					public LabymodEmote getItemObject() {
						return emote;
					}

					@Override
					public ItemStack toItemStack() {
						ItemStack stack = emoteToItem(emote);
						if (emote.getId() == npc.getEmoteId()) {
							stack.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
							ItemMeta meta = stack.getItemMeta();
							meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
							stack.setItemMeta(meta);
						}
						return stack;
					}
				};
				list.add(item);
			}
		}
		list.sort(new Comparator<ListItem<LabymodEmote>>() {
			@Override
			public int compare(ListItem<LabymodEmote> o1, ListItem<LabymodEmote> o2) {
				LabymodEmote e1 = o1.getItemObject();
				LabymodEmote e2 = o2.getItemObject();
				if (e1 == LabymodEmote.NONE) {
					return -1;
				} else if (e2 == LabymodEmote.NONE) {
					return 1;
				} else {
					return Integer.compare(e1.getCategroy()
							.ordinal(),
							e2.getCategroy()
									.ordinal());
				}
			}
		});
		return list;
	}

	@Override
	public String getTitle() {
		return "§eEmotes";
	}

	@Override
	public void onBoarderClick(InventoryClickEvent event) {

	}

	@Override
	public void onListItemClick(InventoryClickEvent event, ListItem<LabymodEmote> listItem) {
		LabymodEmote emote = listItem.getItemObject();
		Result result = this.getMenueManager()
				.getMynpc()
				.getNpcmanager()
				.setEmote(view, npc, emote);

		if (result == Result.SUCCESS) {
			view.playSound(view.getLocation(), MultiVersion.SOUND_ORB_PICKUP, 1.0f, 1.0f);
			view.closeInventory();
		} else {
			view.playSound(view.getLocation(), MultiVersion.SOUND_NOTE_BASS, 1.0f, 1.0f);
		}
	}

	private ItemStack emoteToItem(LabymodEmote emote) {
		if (emote == LabymodEmote.NONE) {
			return ItemFactory.getItem(Material.BARRIER, Texts.ITEM_PREFIX + "§7Kein Emote");
		} else {
			return ItemFactory.getItem(Material.ARMOR_STAND, Texts.ITEM_PREFIX + emote.getDisplayName(), "§fKategorie: " + emote.getCategroy()
					.getName());
		}
	}
}
