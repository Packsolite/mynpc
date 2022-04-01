package de.packsolite.mynpc.command.subcommand;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.entity.Player;

import de.liquiddev.command.AbstractCommandSender;
import de.liquiddev.command.CommandFailException;
import de.liquiddev.command.adapter.bukkit.Arguments;
import de.liquiddev.command.adapter.bukkit.PlayerSubCommand;
import de.liquiddev.command.autocomplete.Autocompleter;
import de.packsolite.mynpc.MyNpc;
import de.packsolite.mynpc.Texts;
import de.packsolite.mynpc.npc.Npc;
import de.packsolite.mynpc.util.LabymodEmote;

public class EmoteCommand extends PlayerSubCommand {

	public EmoteCommand() {
		super("emote", "<Emote>");
		this.setDescription("Setze einen LabyMod Emote");
		this.setAutocompleter(0, new EmoteCompleter());
	}

	@Override
	public void onCommand(Player player, Arguments args) throws CommandFailException {
		String emoteName = args.join(" ");
		LabymodEmote emote = LabymodEmote.getByName(emoteName);

		if (emote == null) {
			player.sendMessage(Texts.PREFIX + Texts.EMOTE_NOT_FOUND);
			return;
		}

		Npc npc = MyNpc.getInstance()
				.getNpcmanager()
				.getNearestNpc(player.getLocation());

		if (npc == null) {
			player.sendMessage(Texts.PREFIX + Texts.NO_NPC_NEARBY);
			return;
		}

		MyNpc.getInstance()
				.getNpcmanager()
				.setEmote(player, npc, emote);
	}

	static class EmoteCompleter implements Autocompleter<Player> {
		@Override
		public Collection<String> autocomplete(AbstractCommandSender<? extends Player> sender, String str) {
			Collection<String> list = new ArrayList<>();
			for (LabymodEmote emote : LabymodEmote.values()) {
				String name = emote.getName();
				if (name.startsWith(str)) {
					if (sender.hasPermission(emote.getPermission())) {
						list.add(name);
					}
				}
			}
			return list;
		}
	}
}