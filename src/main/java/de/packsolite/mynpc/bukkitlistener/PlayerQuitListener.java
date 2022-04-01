package de.packsolite.mynpc.bukkitlistener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.liquiddev.util.bukkit.HastebinReporter;
import de.packsolite.mynpc.MyNpc;
import de.packsolite.mynpc.npc.Npc;

public class PlayerQuitListener implements Listener {

	private MyNpc mynpc;

	public PlayerQuitListener(MyNpc mynpc) {
		this.mynpc = mynpc;
	}

	// clear maps & lists
	private void onDisconnect(Player player) {
		try {
			this.mynpc.getChatTaskManager()
					.clearTasks(player);
			this.mynpc.getMenuManager()
					.clearMenu(player);
			this.mynpc.getNpcmanager()
					.getPlayersMoved()
					.remove(player);

			for (Npc npc : this.mynpc.getNpcmanager()
					.getNpcs()) {
				npc.getRecipients()
						.remove(player);
				npc.getInRangePlayers()
						.remove(player);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			HastebinReporter.getDefaultReporter()
					.reportError(this.getClass(), ex, "error handling disconnect event");
		}
	}

	@EventHandler
	public void onKick(PlayerKickEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Player player = event.getPlayer();
		this.onDisconnect(player);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		this.onDisconnect(player);
	}
}
