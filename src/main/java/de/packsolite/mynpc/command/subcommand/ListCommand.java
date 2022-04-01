package de.packsolite.mynpc.command.subcommand;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import de.liquiddev.command.CommandFailException;
import de.liquiddev.command.adapter.bukkit.Arguments;
import de.liquiddev.command.adapter.bukkit.PlayerSubCommand;
import de.liquiddev.command.autocomplete.Autocomplete;
import de.liquiddev.util.common.AsyncExecutor;
import de.liquiddev.util.common.uuid.UuidUtil;
import de.packsolite.mynpc.MyNpc;
import de.packsolite.mynpc.Texts;
import de.packsolite.mynpc.gui.menu.PlayerNpcsMenu;

public class ListCommand extends PlayerSubCommand {

	public ListCommand() {
		super("list", "<Spieler>");
		this.setDescription("NPCs eines Spielers");
		this.setAutocompleter(0, Autocomplete.players());
	}

	@Override
	public void onCommand(Player player, Arguments args) throws CommandFailException {

		if (args.length() == 0) {
			PlayerNpcsMenu menu = new PlayerNpcsMenu(MyNpc.getInstance()
					.getMenuManager(), null,
					player.getUniqueId()
							.toString(),
					player.getName());
			MyNpc.getInstance()
					.getMenuManager()
					.setCurrentScreen(player, menu);
			return;
		} else {
			String target = args.get(0);

			// run skin fetcher async
			AsyncExecutor.execute(() -> {
				UUID targetUuid = UuidUtil.getDefaultProvider()
						.getUuidOrNull(target);

				// run list sync
				Bukkit.getScheduler()
						.runTask(MyNpc.getInstance(), () -> {

							if (targetUuid == null) {
								player.sendMessage(Texts.PREFIX + Texts.PLAYER_NOT_FOUND);
								return;
							}
							String uuidStr = targetUuid.toString();
							String targetCap = target.substring(0, 1)
									.toUpperCase()
									+ target.substring(1)
											.toLowerCase();
							PlayerNpcsMenu menu = new PlayerNpcsMenu(MyNpc.getInstance()
									.getMenuManager(), null, uuidStr, targetCap);
							MyNpc.getInstance()
									.getMenuManager()
									.setCurrentScreen(player, menu);

						});
			});
		}
	}

}
