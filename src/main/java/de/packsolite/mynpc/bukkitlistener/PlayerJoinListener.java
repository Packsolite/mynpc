package de.packsolite.mynpc.bukkitlistener;

import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.util.Vector;

import de.liquiddev.util.bukkit.HastebinReporter;
import de.packsolite.mynpc.MyNpc;
import de.packsolite.mynpc.npc.Npc;

public class PlayerJoinListener implements Listener {
	private MyNpc mynpc;

	public PlayerJoinListener(MyNpc mynpc) {
		this.mynpc = mynpc;
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		try {
			Player player = event.getPlayer();
			String playerUuid = player.getUniqueId()
					.toString();
			String playerName = player.getName();

			Set<Npc> npcs = this.mynpc.getNpcmanager()
					.getNpcs();

			for (Npc w : npcs) {
				if (!playerUuid.equalsIgnoreCase(w.getCreatorUuid()))
					continue;

				// update creator name
				if (!w.getCreatorName()
						.equalsIgnoreCase(playerName)) {
					w.setCreatorName(playerName);
					this.mynpc.getFileManager()
							.getNpcRepo()
							.updateNpc(w);
				}
			}
			player.setVelocity(new Vector(0, 0.15, 0));
		} catch (Exception ex) {
			ex.printStackTrace();
			HastebinReporter.getDefaultReporter()
					.reportError(this.getClass(), ex, "error handling join event");
		}
	}
}
