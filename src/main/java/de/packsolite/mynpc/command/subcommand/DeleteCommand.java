package de.packsolite.mynpc.command.subcommand;

import org.bukkit.entity.Player;

import de.liquiddev.command.CommandFailException;
import de.liquiddev.command.adapter.bukkit.Arguments;
import de.liquiddev.command.adapter.bukkit.PlayerSubCommand;
import de.packsolite.mynpc.MyNpc;
import de.packsolite.mynpc.Texts;
import de.packsolite.mynpc.npc.Npc;

public class DeleteCommand extends PlayerSubCommand {

	public DeleteCommand() {
		super("delete", "");
		this.setDescription("Löscht einen NPC");
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

		MyNpc.getInstance()
				.getNpcmanager()
				.deleteNpc(player, npc);
	}
}
