package de.packsolite.mynpc.gui.menu;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.packsolite.mynpc.gui.Menu;
import de.packsolite.mynpc.gui.MenuManager;
import de.packsolite.mynpc.npc.Npc;

public class PlayerNpcsMenu extends ListMenu<Npc> {

	private List<ListItem<Npc>> entries;
	private String title;

	public PlayerNpcsMenu(MenuManager manager, Menu parent, String profileUuid, String profileName) {
		super(manager, parent);

		this.title = "§e" + profileName;
		this.entries = new ArrayList<>();

		Stream<Npc> allNpcs = this.getMenueManager()
				.getMynpc()
				.getNpcmanager()
				.getNpcs()
				.stream();
		Stream<Npc> playerNpcs = allNpcs.filter(npc -> profileUuid.equalsIgnoreCase(npc.getCreatorUuid()));

		playerNpcs.forEach(npc -> {
			ListItem<Npc> listItem = new ListItem<Npc>() {
				@Override
				public Npc getItemObject() {
					return npc;
				}

				@Override
				public ItemStack toItemStack() {
					return PlayerNpcsMenu.this.getMenueManager()
							.getItemStackByNpc(npc, false);
				}

			};
			entries.add(listItem);
		});
	}

	@Override
	public void buildInventory(Inventory inventory) {
	}

	@Override
	public String[] getDescriptorLore() {
		return new String[] { "§eNPCs: §f" + NumberFormat.getInstance(Locale.GERMANY)
				.format(this.entries.size()), "§eSeite: §f" + (this.getPage() + 1) + " §7/ "
						+ ((this.getEntries()
								.size() / 35) + 1) };
	}

	@Override
	public List<ListItem<Npc>> getEntries() {
		return this.entries;
	}

	@Override
	public String getTitle() {
		return this.title;
	}

	@Override
	public void onBoarderClick(InventoryClickEvent event) {
	}

	@Override
	public void onListItemClick(InventoryClickEvent event, ListItem<Npc> listItem) {
		Player player = (Player) event.getWhoClicked();
		Npc npc = listItem.getItemObject();
		NpcInfoMenu npcMenu = new NpcInfoMenu(this.getMenueManager(), this, npc, player);
		this.getMenueManager()
				.setCurrentScreen(player, npcMenu);
	}
}
