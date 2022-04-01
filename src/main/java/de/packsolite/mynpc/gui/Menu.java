package de.packsolite.mynpc.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import lombok.Getter;

@Getter
public abstract class Menu {

	private MenuManager menueManager;
	private Menu parentMenu;
	private Inventory inventory;

	public Menu(MenuManager manager, Menu parent) {
		this.menueManager = manager;
		this.parentMenu = parent;
	}

	public abstract Inventory buildInventory();

	public void initGui() {
		this.inventory = this.buildInventory();
	}

	public abstract void onClick(InventoryClickEvent event);

	public void onOpen(Player player) {
	}
}
