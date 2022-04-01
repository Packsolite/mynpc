package de.packsolite.mynpc.bukkitlistener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.liquiddev.util.bukkit.HastebinReporter;
import de.liquiddev.util.bukkit.MultiVersion;
import de.packsolite.mynpc.MyNpc;
import de.packsolite.mynpc.Texts;

public class InventoryClickListener implements Listener {

	private MyNpc mynpc;

	public InventoryClickListener(MyNpc mynpc) {
		this.mynpc = mynpc;
	}

	/* cache to improve performance */
	private Class<?> inventoryViewClass = null;

	@EventHandler
	public void onClick(InventoryClickEvent event) {
		try {
			if (event.getClickedInventory() == null) {
				return;
			}

			String title = "";
			if (MultiVersion.isVersionHigherThan(1, 13)) {

				if (this.inventoryViewClass == null) {
					this.inventoryViewClass = Class.forName("org.bukkit.inventory.InventoryView");
				}

				Object inventoryView = InventoryClickEvent.class.getMethod("getView")
						.invoke(event);
				title = (String) this.inventoryViewClass.getMethod("getTitle")
						.invoke(inventoryView);
			} else {
				title = (String) Inventory.class.getMethod("getTitle")
						.invoke(event.getInventory());
			}

			if (title == null) {
				return;
			}

			if (title.startsWith(Texts.INVENTORY_PREFIX)) {
				event.setCancelled(true);
				this.mynpc.getMenuManager()
						.onMenuClick(event);
			}
		} catch (Exception ex) {
			event.setCancelled(true);
			ex.printStackTrace();
			HastebinReporter.getDefaultReporter()
					.reportError(this.getClass(), ex, "error handling menu click");
		}
	}

	// cancel drag
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onDrag(InventoryDragEvent event) {
		try {
			if (event.getInventory() == null) {
				return;
			}
			String title = "";
			if (MultiVersion.isVersionHigherThan(1, 13)) {

				if (this.inventoryViewClass == null) {
					this.inventoryViewClass = Class.forName("org.bukkit.inventory.InventoryView");
				}

				Object inventoryView = InventoryClickEvent.class.getMethod("getView")
						.invoke(event);
				title = (String) this.inventoryViewClass.getMethod("getTitle")
						.invoke(inventoryView);
			} else {
				title = (String) Inventory.class.getMethod("getTitle")
						.invoke(event.getInventory());
			}

			if (title == null) {
				return;
			}

			if (title.startsWith(Texts.INVENTORY_PREFIX)) {
				if (event.getInventorySlots()
						.size() == 1) {

					if ((int) event.getRawSlots()
							.toArray()[0] > 53)
						return;

					int slot = (int) event.getInventorySlots()
							.toArray()[0];

					InventoryClickEvent click = new InventoryClickEvent(event.getView(), SlotType.CONTAINER, slot, ClickType.LEFT, InventoryAction.PLACE_ALL);
					click.setCursor(event.getNewItems()
							.values()
							.toArray(new ItemStack[1])[0]);
					this.onClick(click);
					event.setCancelled(click.isCancelled());
				} else {
					event.setCancelled(true);
				}

			}
		} catch (Exception ex) {
			ex.printStackTrace();
			HastebinReporter.getDefaultReporter()
					.reportError(this.getClass(), ex, "error handling menu click");
			event.setCancelled(true);
		}

	}
}
