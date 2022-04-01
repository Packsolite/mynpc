package de.packsolite.mynpc.bukkitlistener;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import de.packsolite.mynpc.MyNpc;
import de.packsolite.mynpc.npc.NpcManager;

public class PlayerMoveListener implements Listener {

	private MyNpc mynpc;

	public PlayerMoveListener(MyNpc mynpc) {
		this.mynpc = mynpc;
	}

	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		NpcManager manager = mynpc.getNpcmanager();

		if (manager.getPlayersMoved()
				.add(player)) {
			manager.updatePlayer(player);
		} else {
			Location from = event.getFrom();
			Location to = event.getTo();
			int chunkFromX = (int) from.getX() / 16;
			int chunkToX = (int) to.getX() / 16;
			int chunkFromZ = (int) from.getZ() / 16;
			int chunkToZ = (int) to.getZ() / 16;

			// crossed chunk boarder
			if (chunkFromX != chunkToX || chunkFromZ != chunkToZ) {
				manager.updatePlayer(player);
			}
		}
	}
}
