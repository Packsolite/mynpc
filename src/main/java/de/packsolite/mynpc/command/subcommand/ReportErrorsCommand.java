package de.packsolite.mynpc.command.subcommand;

import org.bukkit.command.CommandSender;

import de.liquiddev.command.CommandVisibility;
import de.liquiddev.command.InvalidCommandArgException;
import de.liquiddev.command.adapter.bukkit.Arguments;
import de.liquiddev.command.adapter.bukkit.ConsoleSubCommand;
import de.liquiddev.util.bukkit.HastebinReporter;

public class ReportErrorsCommand extends ConsoleSubCommand {

	public ReportErrorsCommand() {
		super("reporterrors", "");
		this.setVisibility(CommandVisibility.HIDDEN);
	}

	@Override
	public void onCommand(CommandSender sender, Arguments args) throws InvalidCommandArgException {
		sender.sendMessage(HastebinReporter.getDefaultReporter()
				.getReportText());
	}
}
