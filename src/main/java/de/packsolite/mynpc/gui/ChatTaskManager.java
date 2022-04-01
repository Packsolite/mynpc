package de.packsolite.mynpc.gui;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import de.packsolite.mynpc.MyNpc;
import de.packsolite.mynpc.Texts;

public class ChatTaskManager {

	public static interface ChatTask {
		public boolean onChatMessage(String message);
	}

	private HashMap<Player, ChatTask> tasks = new HashMap<Player, ChatTask>();

	public ChatTaskManager() {
	}

	public void addTask(Player player, ChatTask task) {
		this.tasks.put(player, task);
	}

	public void clearTasks(Player player) {
		tasks.remove(player);
	}

	public boolean onChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();

		if (!this.tasks.containsKey(player)) {
			return false;
		}

		Runnable syncTask = () -> {
			ChatTask task = this.tasks.get(player);
			String message = event.getMessage();

			if (!task.onChatMessage(message)) {
				player.sendMessage(Texts.PREFIX + Texts.TASK_CANCELED);
			}

			this.tasks.remove(player);
		};

		if (event.isAsynchronous()) {
			Bukkit.getScheduler()
					.runTask(MyNpc.getInstance(), syncTask);
		} else {
			syncTask.run();
		}
		return true;
	}
}
