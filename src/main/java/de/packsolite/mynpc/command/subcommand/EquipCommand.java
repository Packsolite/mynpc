package de.packsolite.mynpc.command.subcommand;

import org.bukkit.entity.Player;

import de.liquiddev.command.CommandFailException;
import de.liquiddev.command.adapter.bukkit.Arguments;
import de.liquiddev.command.adapter.bukkit.PlayerSubCommand;
import de.packsolite.mynpc.MyNpc;
import de.packsolite.mynpc.Texts;
import de.packsolite.mynpc.gui.Menu;
import de.packsolite.mynpc.gui.menu.NpcEquipMenu;
import de.packsolite.mynpc.npc.Npc;

public class EquipCommand extends PlayerSubCommand {

	public EquipCommand() {
		super("equip", "");
		this.setDescription("Gibt einem NPC Items");
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

		Menu menu = new NpcEquipMenu(MyNpc.getInstance()
				.getMenuManager(), null, npc);
		MyNpc.getInstance()
				.getMenuManager()
				.setCurrentScreen(player, menu);
	}
}