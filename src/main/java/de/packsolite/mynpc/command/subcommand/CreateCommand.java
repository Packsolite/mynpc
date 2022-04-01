package de.packsolite.mynpc.command.subcommand;

import org.bukkit.entity.Player;

import de.liquiddev.command.CommandFailException;
import de.liquiddev.command.adapter.bukkit.Arguments;
import de.liquiddev.command.adapter.bukkit.PlayerSubCommand;
import de.packsolite.mynpc.MyNpc;
import de.packsolite.mynpc.gui.Menu;
import de.packsolite.mynpc.gui.menu.SelectSkinMenu;

public class CreateCommand extends PlayerSubCommand {

	public CreateCommand() {
		super("create", "");
		this.setDescription("Erstellt einen NPC");
	}

	@Override
	public void onCommand(Player player, Arguments args) throws CommandFailException {
		if (!MyNpc.getInstance()
				.getNpcmanager()
				.canCreateNPC(player)) {
			return;
		}

		Menu skinSelect = new SelectSkinMenu(MyNpc.getInstance()
				.getMenuManager(), null, null, player);
		MyNpc.getInstance()
				.getMenuManager()
				.setCurrentScreen(player, skinSelect);
	}
}