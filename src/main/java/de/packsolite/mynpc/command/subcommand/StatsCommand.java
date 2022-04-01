package de.packsolite.mynpc.command.subcommand;

import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.liquiddev.command.CommandFailException;
import de.liquiddev.command.CommandVisibility;
import de.liquiddev.command.adapter.bukkit.Arguments;
import de.liquiddev.command.adapter.bukkit.ConsoleSubCommand;
import de.liquiddev.util.bukkit.labymod.LabymodController;
import de.packsolite.mynpc.MyNpc;
import de.packsolite.mynpc.Texts;

public class StatsCommand extends ConsoleSubCommand {

	private MyNpc mynpc = MyNpc.getInstance();

	public StatsCommand() {
		super("stats", "");
		this.setVisibility(CommandVisibility.HIDDEN);
	}

	@Override
	public void onCommand(CommandSender sender, Arguments args) throws CommandFailException {
		sender.sendMessage(Texts.PREFIX + "§8 --- §bNPC Statistics §8 ---");
		sender.sendMessage("§7Total NPCs §8» §b" + getTotalNps());
		sender.sendMessage("§7Active NPCs §8» §b" + getViwedNpcs() + "§8/§7" + getActiveNpcs());
		if (sender instanceof Player) {
			sender.sendMessage("§7In your range §8» §b" + getNpcsInRangeOf((Player) sender));
		}
		sender.sendMessage("§7LabyMod User cache §8» §b" + getLabyModUsersSize());
	}

	private int getTotalNps() {
		return mynpc.getNpcmanager()
				.getNpcs()
				.size();
	}

	/**
	 * Returns the number of NPCs that are in the range of at least one player.
	 */
	private int getActiveNpcs() {
		return (int) mynpc.getNpcmanager()
				.getNpcs()
				.stream()
				.filter(o -> !o.getInRangePlayers()
						.isEmpty())
				.count();
	}

	/**
	 * Returns the number of NPCs that are visible for at least one player.
	 */
	private int getViwedNpcs() {
		return (int) mynpc.getNpcmanager()
				.getNpcs()
				.stream()
				.filter(o -> !o.getRecipients()
						.isEmpty())
				.count();
	}

	private int getLabyModUsersSize() {
		Optional<LabymodController> lmc = MyNpc.getInstance()
				.getLabymodController();
		return lmc.isPresent() ? lmc.get()
				.getLabymodUserCount() : 0;
	}

	private int getNpcsInRangeOf(Player player) {
		return (int) mynpc.getNpcmanager()
				.getNpcs()
				.stream()
				.filter(o -> o.getInRangePlayers()
						.contains(player))
				.count();
	}
}