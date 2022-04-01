package de.packsolite.mynpc.command.subcommand;

import org.bukkit.entity.Player;

import de.liquiddev.command.CommandFailException;
import de.liquiddev.command.adapter.bukkit.Arguments;
import de.liquiddev.command.adapter.bukkit.PlayerSubCommand;
import de.packsolite.mynpc.MyNpc;
import de.packsolite.mynpc.Texts;
import de.packsolite.mynpc.gui.menu.NpcInfoMenu;
import de.packsolite.mynpc.npc.Npc;

public class InfoCommand extends PlayerSubCommand {

	public InfoCommand() {
		super("info", "");
		this.setDescription("Informationen zum NPC");
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

		NpcInfoMenu menu = new NpcInfoMenu(MyNpc.getInstance()
				.getMenuManager(), null, npc, player);
		MyNpc.getInstance()
				.getMenuManager()
				.setCurrentScreen(player, menu);
	}
}
