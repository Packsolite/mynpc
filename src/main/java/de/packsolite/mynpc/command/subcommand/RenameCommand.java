package de.packsolite.mynpc.command.subcommand;

import org.bukkit.entity.Player;

import de.liquiddev.command.CommandFailException;
import de.liquiddev.command.adapter.bukkit.Arguments;
import de.liquiddev.command.adapter.bukkit.PlayerSubCommand;
import de.packsolite.mynpc.MyNpc;
import de.packsolite.mynpc.Texts;
import de.packsolite.mynpc.npc.Npc;

public class RenameCommand extends PlayerSubCommand {

	public RenameCommand() {
		super("rename", "<Neuer Name>");
		this.setDescription("Benennt einen NPC um");
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

		String name = args.get(0)
				.replace("&", "§");
		if (!player.hasPermission(Texts.PERMISSION_ADMIN)) {
			name = name.replace("§4", "§c");
		}
		MyNpc.getInstance()
				.getNpcmanager()
				.renameNpc(player, npc, name);
	}
}
