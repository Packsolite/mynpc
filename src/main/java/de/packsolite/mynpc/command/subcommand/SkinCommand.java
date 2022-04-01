package de.packsolite.mynpc.command.subcommand;

import org.bukkit.entity.Player;

import de.liquiddev.command.CommandFailException;
import de.liquiddev.command.adapter.bukkit.Arguments;
import de.liquiddev.command.adapter.bukkit.PlayerSubCommand;
import de.packsolite.mynpc.MyNpc;
import de.packsolite.mynpc.Texts;
import de.packsolite.mynpc.gui.Menu;
import de.packsolite.mynpc.gui.menu.SelectSkinMenu;
import de.packsolite.mynpc.npc.Npc;

public class SkinCommand extends PlayerSubCommand {

	public SkinCommand() {
		super("skin", "");
		this.setDescription("Setzt den Skin eines NPC");
	}

	@Override
	public void onCommand(Player player, Arguments args) throws CommandFailException {
		Npc npc = MyNpc.getInstance()
				.getNpcmanager()
				.getNearestNpc(player.getLocation());

		if (npc == null) {
			player.sendMessage(Texts.PREFIX + Texts.NO_NPC_NEARBY);
			return;
		}

		if (!MyNpc.getInstance()
				.getNpcmanager()
				.canEditNpc(player, npc)) {
			player.sendMessage(Texts.PREFIX + Texts.NOT_YOUR_NPC);
			return;
		}

		Menu currentScreen = new SelectSkinMenu(MyNpc.getInstance()
				.getMenuManager(), null, npc, player);
		MyNpc.getInstance()
				.getMenuManager()
				.setCurrentScreen(player, currentScreen);
	}
}
