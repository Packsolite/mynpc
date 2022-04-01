package de.packsolite.mynpc.command.subcommand;

import org.bukkit.command.CommandSender;

import de.liquiddev.command.CommandVisibility;
import de.liquiddev.command.InvalidCommandArgException;
import de.liquiddev.command.adapter.bukkit.Arguments;
import de.liquiddev.command.adapter.bukkit.ConsoleSubCommand;
import de.liquiddev.util.bukkit.PluginUtil;
import de.packsolite.mynpc.MyNpc;
import de.packsolite.mynpc.Texts;

public class ReloadCommand extends ConsoleSubCommand {

	public ReloadCommand() {
		super("reload", "");
		this.setPermission(Texts.PERMISSION_ADMIN);
		this.setVisibility(CommandVisibility.HIDDEN);
	}

	@Override
	public void onCommand(CommandSender sender, Arguments args) throws InvalidCommandArgException {
		sender.sendMessage(Texts.PREFIX + "Reloading Plugin...");
		PluginUtil.reload(MyNpc.getInstance());
		sender.sendMessage(Texts.PREFIX + "§aDone!");
	}
}