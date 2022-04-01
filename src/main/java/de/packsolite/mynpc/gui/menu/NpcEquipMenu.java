package de.packsolite.mynpc.gui.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.liquiddev.util.bukkit.MultiVersion;
import de.packsolite.mynpc.gui.Menu;
import de.packsolite.mynpc.gui.MenuManager;
import de.packsolite.mynpc.npc.Npc;
import lombok.Getter;

@Getter
public class NpcEquipMenu extends Menu {

	private Npc npc;

	public NpcEquipMenu(MenuManager manager, Menu parent, Npc npc) {
		super(manager, parent);
		this.npc = npc;

		// close other equip menus
		for (Player all : this.getMenueManager()
				.getCurrentScreens()
				.keySet()) {
			Menu menu = this.getMenueManager()
					.getCurrentScreens()
					.get(all);

			if (menu instanceof NpcEquipMenu) {
				NpcEquipMenu equipMenu = (NpcEquipMenu) menu;

				if (this.npc.equals(equipMenu.getNpc())) {
					this.getMenueManager()
							.setCurrentScreen(all, new MainMenue(this.getMenueManager()));
				}
			}
		}
	}

	@Override
	public Inventory buildInventory() {
		Inventory inv = this.getMenueManager()
				.buildLayoutInventory("§5Equipment #" + npc.getId(), 54);

		for (int i = 1; i < 45; i++) {
			inv.setItem(i, this.getMenueManager()
					.getBoarderSlotItem());
		}

		ItemStack[] equipment = this.npc.getEquipmentStack();

		for (int i = 0; i < equipment.length; i++) {
			if (equipment[i] == null)
				equipment[i] = new ItemStack(Material.AIR);
		}

		inv.setItem(10, this.getMenueManager()
				.getHelmetItem());
		inv.setItem(11, equipment[5]);

		inv.setItem(19, this.getMenueManager()
				.getChestItem());
		inv.setItem(20, equipment[4]);

		inv.setItem(28, this.getMenueManager()
				.getLegsItem());
		inv.setItem(29, equipment[3]);

		inv.setItem(37, this.getMenueManager()
				.getBootsItem());
		inv.setItem(38, equipment[2]);

		// 2 hands only if 1.9 or higher
		if (MultiVersion.isVersionHigherThan(1, 8)) {
			inv.setItem(23, this.getMenueManager()
					.getOffHandItem());
			inv.setItem(32, equipment[1]);

			inv.setItem(24, this.getMenueManager()
					.getHandItem());
			inv.setItem(33, equipment[0]);
		} else {
			inv.setItem(23, this.getMenueManager()
					.getLegacyHandItem());
			inv.setItem(32, equipment[1]);
		}
		return inv;
	}

	@Override
	public void onClick(InventoryClickEvent event) {

		if (event.getClickedInventory() == null || event.getAction() == InventoryAction.COLLECT_TO_CURSOR || event.getAction() == InventoryAction.DROP_ONE_SLOT)
			return;

		Player player = (Player) event.getWhoClicked();

		if (event.getClickedInventory()
				.equals(player.getInventory())) {

			if (event.isShiftClick())
				return;

			event.setCancelled(false);
			return;
		}

		if (event.getAction() == InventoryAction.PICKUP_HALF || event.getAction() == InventoryAction.PICKUP_SOME || event.getAction() == InventoryAction.PICKUP_ONE
				|| event.getAction() == InventoryAction.PLACE_SOME || (event.isShiftClick() && event.getCursor()
						.getType() != Material.AIR))
			return;

		int slot = event.getSlot();
		int equipSlot;

		ItemStack item = event.getCursor()
				.clone();

		if (slot == 11) {
			equipSlot = 5;
		} else if (slot == 20) {
			equipSlot = 4;

			if (item != null && item.getType() != Material.AIR && !item.getType()
					.toString()
					.toLowerCase()
					.contains("chestplate")) {
				event.setCancelled(true);
				return;
			}

		} else if (slot == 29) {
			equipSlot = 3;

			if (item != null && item.getType() != Material.AIR && !item.getType()
					.toString()
					.toLowerCase()
					.contains("leggings")) {
				event.setCancelled(true);
				return;
			}

		} else if (slot == 38) {
			equipSlot = 2;

			if (item != null && item.getType() != Material.AIR && !item.getType()
					.toString()
					.toLowerCase()
					.contains("boots")) {
				event.setCancelled(true);
				return;
			}
		} else if (slot == 32) {
			equipSlot = 1;
		} else if (slot == 33 && MultiVersion.isVersionHigherThan(1, 8)) { // second hand only if 1.9 or higher
			equipSlot = 0;
		} else {
			return;
		}

		ItemStack[] equipment = this.npc.getEquipmentStack();
		event.setCancelled(false);
		equipment[equipSlot] = item;

		if (event.getAction() == InventoryAction.PLACE_ONE) {
			int amount = event.getCurrentItem() == null ? 0
					: event.getCurrentItem()
							.getAmount();
			equipment[equipSlot].setAmount(1 + amount);
		}

		this.npc.setEquipment(equipSlot, equipment[equipSlot]);
		this.getMenueManager()
				.getMynpc()
				.getFileManager()
				.getNpcRepo()
				.updateNpc(this.npc);
	}
}
