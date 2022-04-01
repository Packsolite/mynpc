package de.packsolite.mynpc.gui.menu;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.liquiddev.util.bukkit.ItemFactory;
import de.packsolite.mynpc.gui.Menu;
import de.packsolite.mynpc.gui.MenuManager;
import lombok.Getter;

@Getter
public abstract class ListMenu<T> extends Menu {

	public interface ListItem<U> {
		public abstract U getItemObject();

		public abstract ItemStack toItemStack();
	}

	private int page;
	private ItemStack infoBook;

	private long lastAction = System.currentTimeMillis();

	public ListMenu(MenuManager manager, Menu parent) {
		super(manager, parent);
		this.page = 0;
	}

	@Override
	public Inventory buildInventory() {
		Inventory inv = this.getMenueManager()
				.buildLayoutInventory(this.getTitle(), 54);
		this.infoBook = ItemFactory.getItem(Material.PAPER, "§3* " + this.getTitle() + " §3*", this.page + 1, this.getDescriptorLore());

		this.buildPage(inv);
		this.buildInventory(inv);
		return inv;
	}

	public abstract void buildInventory(Inventory inventory);

	private void buildPage(Inventory inv) {
		this.infoBook.setAmount(this.page + 1);
		ItemMeta infoMeta = infoBook.getItemMeta();
		infoMeta.setLore(Arrays.asList(this.getDescriptorLore()));
		this.infoBook.setItemMeta(infoMeta);
		inv.setItem(49, this.infoBook);

		if (this.page == 0) {
			inv.setItem(18, this.getMenueManager()
					.getBoarderSlotItem());
		} else {
			inv.setItem(18, this.getMenueManager()
					.getPreviousPageButton());
		}

		if (this.page == this.getEntries()
				.size() / 35) {
			inv.setItem(26, this.getMenueManager()
					.getBoarderSlotItem());
		} else {
			inv.setItem(26, this.getMenueManager()
					.getNextPageButton());
		}

		for (int i = 0; i < 35; i++) {
			int id = 35 * this.page + i;
			int slot = this.getSlotFromEntryId(id);

			if (id < this.getEntries()
					.size()) {
				ListItem<T> entry = this.getEntries()
						.get(id);
				inv.setItem(slot, entry.toItemStack());
			} else {
				inv.setItem(slot, this.getMenueManager()
						.getEmptySlotItem());
			}
		}
	}

	public abstract String[] getDescriptorLore();

	public abstract List<ListItem<T>> getEntries();

	public ListItem<T> getEntryFromSlot(int slot) {
		int entryId = this.getEntryIdFromSlot(slot);
		if (entryId < this.getEntries()
				.size())
			return this.getEntries()
					.get(entryId);
		return null;
	}

	public int getEntryIdFromSlot(int slot) {
		int pageOffset = page * 35;
		int line = slot / 9;
		int row = slot - line * 9;
		int id = line * 7 + row - 1;
		return pageOffset + id;
	}

	public int getSlotFromEntryId(int entryId) {
		int pageOffset = page * 35;
		int localId = entryId - pageOffset;
		int line = localId / 7;
		int row = localId - line * 7;
		return line * 9 + row + 1;
	}

	public abstract String getTitle();

	public boolean isSlotInsideList(int slot) {
		int line = slot / 9;
		int row = slot - line * 9;
		return line < 5 && row > 0 && row < 8;
	}

	public abstract void onBoarderClick(InventoryClickEvent event);

	@Override
	public void onClick(InventoryClickEvent event) {
		// spam protection
		if (System.currentTimeMillis() - lastAction < 100) {
			return;
		}

		lastAction = System.currentTimeMillis();

		int slot = event.getSlot();

		if (this.isSlotInsideList(slot)) {

			ListItem<T> listItem = this.getEntryFromSlot(event.getSlot());

			if (listItem != null) {
				this.onListItemClick(event, listItem);
			}

		} else if (slot == 18) {
			if (this.page > 0) {
				this.setPage(this.page - 1);
			}
		} else if (slot == 26) {
			if (this.page < this.getEntries()
					.size() / 35) {
				this.setPage(this.page + 1);
			}
		} else {
			this.onBoarderClick(event);
		}
	}

	public abstract void onListItemClick(InventoryClickEvent event, ListItem<T> listItem);

	public void setPage(int page) {
		this.page = page;
		this.updatePage();
	}

	public void updatePage() {
		this.buildPage(this.getInventory());
	}
}
