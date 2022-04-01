package de.packsolite.mynpc;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import de.liquiddev.util.bukkit.MultiVersion;
import de.liquiddev.util.bukkit.labymod.LabymodController;
import de.packsolite.mynpc.bukkitlistener.InventoryClickListener;
import de.packsolite.mynpc.bukkitlistener.PlayerChatListener;
import de.packsolite.mynpc.bukkitlistener.PlayerJoinListener;
import de.packsolite.mynpc.bukkitlistener.PlayerMoveListener;
import de.packsolite.mynpc.bukkitlistener.PlayerQuitListener;
import de.packsolite.mynpc.bukkitlistener.PlayerRespawnListener;
import de.packsolite.mynpc.command.MyNpcCommand;
import de.packsolite.mynpc.file.FileManager;
import de.packsolite.mynpc.gui.ChatTaskManager;
import de.packsolite.mynpc.gui.MenuManager;
import de.packsolite.mynpc.npc.Npc;
import de.packsolite.mynpc.npc.NpcManager;
import de.packsolite.mynpc.npc.NpcPacketListener;
import de.packsolite.mynpc.util.LandsWrapper;
import lombok.Getter;

@Getter
public class MyNpc extends JavaPlugin {

	/**
	 * Instance of the Main class
	 */
	private static MyNpc instance;

	/**
	 * API access from outside of the jar.
	 * 
	 * @return {@link MyNpc} isntance or <code>null</code>
	 */
	public static MyNpc getInstance() {
		return instance;
	}

	/**
	 * The maximum distance in blocks a player can see the npc.
	 */
	private final int viewDistance = 64;

	/**
	 * Diligent little managers that are managing all the plugin's tasks.
	 */
	private NpcManager npcmanager;
	private FileManager fileManager;
	private MyNpcCommand mynpcCommand;
	private MenuManager menuManager;
	private ChatTaskManager chatTaskManager;
	private NpcPacketListener packetListener;
	private Optional<LabymodController> labymodController;

	/**
	 * ShutdownMyNpc
	 */
	@Override
	public void onDisable() {
		this.getFileManager()
				.getNpcRepo()
				.saveChanges();
		NpcPacketListener.uninjectAll();
		LandsWrapper.disable();
		for (Npc npc : this.getNpcmanager()
				.getNpcs()) {
			npc.removeForAll();
		}
		if (labymodController.isPresent())
			this.labymodController.get()
					.disable();
		super.onDisable();
	}

	/**
	 * Startup MyNpc
	 */
	@Override
	public void onEnable() {
		long startupTimeStamp = System.currentTimeMillis();
		instance = this;

		/* Say hello to console */
		this.printWelcomeMessage();

		/* initialize MyNpc */
		MultiVersion.init();
		this.fileManager = new FileManager(this);
		this.npcmanager = new NpcManager(this.fileManager.getNpcRepo()
				.getNpcs(), this);
		this.menuManager = new MenuManager(this);
		this.chatTaskManager = new ChatTaskManager();
		this.packetListener = new NpcPacketListener(this, true);
		try {
			LabymodController lmc = new LabymodController(this);
			lmc.enable();
			this.labymodController = Optional.of(lmc);
		} catch (UnsupportedOperationException ex) {
			Bukkit.getConsoleSender()
					.sendMessage(Texts.PREFIX + "§eLabyMod support is disabled for your bukkit version!");
			this.labymodController = Optional.empty();
		}

		/* Register Bukkit Listeners */
		Bukkit.getPluginManager()
				.registerEvents(new InventoryClickListener(this), this);
		Bukkit.getPluginManager()
				.registerEvents(new PlayerChatListener(this), this);
		Bukkit.getPluginManager()
				.registerEvents(new PlayerJoinListener(this), this);
		Bukkit.getPluginManager()
				.registerEvents(new PlayerQuitListener(this), this);
		Bukkit.getPluginManager()
				.registerEvents(new PlayerRespawnListener(this), this);
		Bukkit.getPluginManager()
				.registerEvents(new PlayerMoveListener(this), this);

		/* Register commands */
		mynpcCommand = new MyNpcCommand(this);
		mynpcCommand.register(this);

		/* We are ready! */
		long startupTime = System.currentTimeMillis() - startupTimeStamp;
		Bukkit.getConsoleSender()
				.sendMessage(Texts.PREFIX + "Done! (" + startupTime + "ms)");
		super.onEnable();
	}

	private void printWelcomeMessage() {
		String myNpcAsciiArt = "\n\r§3  __  __       §b_   _ _____   _____ \r\n" + "§3 |  \\/  |     §b| \\ | |  __ \\ / ____|\r\n" + "§3 | \\  / |_   _§b|  \\| | |__) | |     \r\n"
				+ "§3 | |\\/| | | | §b| . ` |  ___/| |     \r\n" + "§3 | |  | | |_| §b| |\\  | |    | |____ \r\n" + "§3 |_|  |_|\\__, §b|_| \\_|_|     \\_____|\r\n"
				+ "§3          __/ |                    \r\n" + "§3         |___/         §7by packsolite\n\r";

		Bukkit.getConsoleSender()
				.sendMessage(myNpcAsciiArt);

		Bukkit.getConsoleSender()
				.sendMessage(Texts.PREFIX + "Starting up MyNpc...");
	}

	public Optional<LabymodController> getLabymodController() {
		return labymodController;
	}

}
