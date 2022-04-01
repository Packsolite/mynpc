package de.packsolite.mynpc.bukkitlistener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import de.liquiddev.util.bukkit.HastebinReporter;
import de.packsolite.mynpc.MyNpc;

public class PlayerRespawnListener implements Listener {

	private MyNpc mynpc;

	public PlayerRespawnListener(MyNpc mynpc) {
		this.mynpc = mynpc;
	}

	@EventHandler
	public void onRespawn(PlayerRespawnEvent event) {
		try {
			Player player = event.getPlayer();
			this.mynpc.getNpcmanager()
					.despawnAllNpcs(player);
			this.mynpc.getNpcmanager()
					.getPlayersMoved()
					.remove(player);
		} catch (Exception ex) {
			ex.printStackTrace();
			HastebinReporter.getDefaultReporter()
					.reportError(this.getClass(), ex, "error handling join event");
		}
	}

	@EventHandler
	public void teleport(PlayerTeleportEvent e) {
		try {
			if (e.getFrom()
					.getWorld()
					.equals(e.getTo()
							.getWorld())
					&& e.getFrom()
							.distanceSquared(e.getTo()) < 2500) {
				return;
			}
			Player player = e.getPlayer();
			this.mynpc.getNpcmanager()
					.despawnAllNpcs(player);
			this.mynpc.getNpcmanager()
					.getPlayersMoved()
					.remove(player);
		} catch (Exception ex) {
			ex.printStackTrace();
			HastebinReporter.getDefaultReporter()
					.reportError(this.getClass(), ex, "error handling teleport event");
		}
	}
}