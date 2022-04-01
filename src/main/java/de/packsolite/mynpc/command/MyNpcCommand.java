package de.packsolite.mynpc.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.liquiddev.command.AbstractCommandSender;
import de.liquiddev.command.CommandFailHandler;
import de.liquiddev.command.CommandNode;
import de.liquiddev.command.InvalidCommandArgException;
import de.liquiddev.command.adapter.bukkit.Arguments;
import de.liquiddev.command.adapter.bukkit.ConsoleCommand;
import de.liquiddev.command.example.HelpCommand;
import de.liquiddev.command.example.HelpCommand.HelpCommandBuilder;
import de.packsolite.mynpc.MyNpc;
import de.packsolite.mynpc.Texts;
import de.packsolite.mynpc.command.subcommand.CreateCommand;
import de.packsolite.mynpc.command.subcommand.DeleteCommand;
import de.packsolite.mynpc.command.subcommand.EmoteCommand;
import de.packsolite.mynpc.command.subcommand.EquipCommand;
import de.packsolite.mynpc.command.subcommand.InfoCommand;
import de.packsolite.mynpc.command.subcommand.ListCommand;
import de.packsolite.mynpc.command.subcommand.MoveCommand;
import de.packsolite.mynpc.command.subcommand.ReloadCommand;
import de.packsolite.mynpc.command.subcommand.RenameCommand;
import de.packsolite.mynpc.command.subcommand.ReportErrorsCommand;
import de.packsolite.mynpc.command.subcommand.SkinCommand;
import de.packsolite.mynpc.command.subcommand.StatsCommand;
import de.packsolite.mynpc.gui.menu.MainMenue;

public class MyNpcCommand extends ConsoleCommand implements CommandFailHandler<CommandSender> {

	private MyNpc myholo;

	public MyNpcCommand(MyNpc mynpc) {
		super("mynpc", "help");
		this.setPrefix(Texts.PREFIX);
		this.addAlias("mn");
		this.setDescription("Listet Befehle auf");
		this.myholo = mynpc;
		this.addSubCommand(new CreateCommand());
		this.addSubCommand(new DeleteCommand());
		this.addSubCommand(new InfoCommand());
		this.addSubCommand(new MoveCommand());
		this.addSubCommand(new EquipCommand());
		this.addSubCommand(new SkinCommand());
		this.addSubCommand(new RenameCommand());
		this.addSubCommand(new ListCommand());
		this.addSubCommand(new EmoteCommand());
		this.addSubCommand(new StatsCommand());

		this.addSubCommand(new ReportErrorsCommand());
		this.addSubCommand(new ReloadCommand());

		HelpCommandBuilder builder = HelpCommand.newBuilder(this);
		builder.header(Texts.MULTILINE_SEPERATOR + Texts.HELP_HEADER);
		builder.footer(Texts.MULTILINE_SEPERATOR);
		builder.format(Texts.HELP_BODY_COMMAND);
		this.addSubCommand(builder.create());

		if (mynpc.getFileManager()
				.isUsePermission()) {
			this.setPermission(Texts.PERMISSION_USE);
		}

		this.setFailHandler(this);
	}

	@Override
	public void onInvalidArgument(AbstractCommandSender<CommandSender> sender, CommandNode<CommandSender> command, Class<?> required, String provided) {
		if (required.equals(Player.class)) {
			sender.sendMessage(Texts.PREFIX + Texts.PLAYER_NOT_FOUND);
		} else if (required.equals(Integer.class) || required.equals(Long.class)) {
			sender.sendMessage(Texts.PREFIX + Texts.NOT_A_NUMBER);
		} else {
			sender.sendMessage(Texts.PREFIX + Texts.WRONG_ARGS.replace("%s", command.getHint()));
		}
	}

	@Override
	public void onMissingArgument(AbstractCommandSender<CommandSender> sender, CommandNode<CommandSender> command, Class<?> required, int index) {
		sender.sendMessage(Texts.PREFIX + Texts.WRONG_ARGS.replace("%s", command.getHint()));
	}

	@Override
	public void onCommand(CommandSender sender, Arguments args) throws InvalidCommandArgException {
		if (args.length() == 0 && sender instanceof Player) {
			this.myholo.getMenuManager()
					.setCurrentScreen((Player) sender, new MainMenue(this.myholo.getMenuManager()));
		} else {
			sender.sendMessage(Texts.PREFIX + Texts.HELP_INVALID_COMMAND);
		}
	}

	@Override
	public void onPermissionFail(AbstractCommandSender<CommandSender> sender, CommandNode<CommandSender> command) {
		sender.sendMessage(Texts.PREFIX + Texts.NO_PERMISSION);
	}

	@Override
	public void onCommandFail(AbstractCommandSender<CommandSender> sender, CommandNode<CommandSender> command, String reason) {
		sender.sendMessage(Texts.PREFIX + reason);
	}

	@Override
	public void onUnsupportedCommandSender(AbstractCommandSender<CommandSender> sender, CommandNode<CommandSender> command, Class<?> requiredSenderType) {
		sender.sendMessage(Texts.PREFIX + Texts.ONLY_FOR_PLAYERS);
	}

	@Override
	public void onRateLimitExceeded(AbstractCommandSender<CommandSender> sender, CommandNode<CommandSender> command) {
		sender.sendMessage(Texts.PREFIX + "§cPlease slow down!");
	}
}
