package de.packsolite.mynpc.command.subcommand;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import org.bukkit.entity.Player;

import de.liquiddev.command.CommandFailException;
import de.liquiddev.command.adapter.bukkit.Arguments;
import de.liquiddev.command.adapter.bukkit.PlayerSubCommand;
import de.packsolite.mynpc.MyNpc;
import de.packsolite.mynpc.Texts;
import de.packsolite.mynpc.npc.Npc;

public class MoveCommand extends PlayerSubCommand {

	private Map<UUID, Npc> moving = new WeakHashMap<>();

	public MoveCommand() {
		super("move", "");
		this.setDescription("Verschiebt einen NPC");
	}

	@Override
	public void onCommand(Player player, Arguments args) throws CommandFailException {
		UUID uuid = player.getUniqueId();
		if (!moving.containsKey(uuid)) {

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

			moving.put(uuid, npc);
			player.sendMessage(Texts.PREFIX + Texts.TYPE_MOVE_AGAIN);
		} else {
			MyNpc.getInstance()
					.getNpcmanager()
					.moveNpc(player, moving.get(uuid));
			moving.remove(uuid);
		}
	}
}
