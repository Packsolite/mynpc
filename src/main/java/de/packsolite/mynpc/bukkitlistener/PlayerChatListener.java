package de.packsolite.mynpc.bukkitlistener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import de.liquiddev.util.bukkit.HastebinReporter;
import de.packsolite.mynpc.MyNpc;

public class PlayerChatListener implements Listener {

	private MyNpc mynpc;

	public PlayerChatListener(MyNpc mynpc) {
		this.mynpc = mynpc;
	}

	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		try {
			if (this.mynpc.getChatTaskManager()
					.onChat(event)) {
				event.setCancelled(true);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			HastebinReporter.getDefaultReporter()
					.reportError(this.getClass(), ex, "error handling chat event");
		}
	}
}
