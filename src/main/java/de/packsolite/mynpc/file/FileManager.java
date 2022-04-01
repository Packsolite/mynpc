package de.packsolite.mynpc.file;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

import org.bukkit.Bukkit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.liquiddev.util.bukkit.HastebinReporter;
import de.packsolite.mynpc.MyNpc;
import de.packsolite.mynpc.Texts;
import de.packsolite.mynpc.file.repository.NpcRepository;
import de.packsolite.mynpc.file.repository.impl.FileRepository;
import de.packsolite.mynpc.file.repository.impl.SqliteRepository;
import de.packsolite.mynpc.npc.Npc;
import lombok.Getter;

@Getter
public class FileManager {
	private Gson gson = new GsonBuilder().setPrettyPrinting()
			.create();
	private NpcRepository npcRepo;
	private MyNpc myNpc;

	private final String myNpcConfigpath = "plugins/myNpc/";
	private File configFile = new File(myNpcConfigpath + "mynpc.json");
	private MyNpcConfig config;

	public FileManager(MyNpc myNpc) {
		this.myNpc = myNpc;

		try {
			// use SQLite if possible
			this.npcRepo = new SqliteRepository(this);
			FileRepository fileRepo = new FileRepository(this);
			if (fileRepo.getPath()
					.exists()) {
				this.transferRepo(new FileRepository(this), this.npcRepo);
			}
		} catch (SQLException ex) {
			Bukkit.getConsoleSender()
					.sendMessage(Texts.PREFIX + "§cSomething went wrong with sqlite, falling back to file repository...");
			HastebinReporter.getDefaultReporter()
					.reportError(this.getClass(), ex, "error initializing sqlite, using file repository instead");
			// fallback to file repository
			this.npcRepo = new FileRepository(this);
		}

		if (configFile.exists()) {
			try (FileReader reader = new FileReader(configFile)) {
				this.config = this.gson.fromJson(reader, MyNpcConfig.class);
			} catch (Exception e) {
				this.config = new MyNpcConfig(System.currentTimeMillis(), true);
				HastebinReporter.getDefaultReporter()
						.reportError(this.getClass(), e, "can't read config");
				e.printStackTrace();
			}
		} else {
			this.config = new MyNpcConfig(0, true);
			this.saveConfig();
		}
	}

	private void transferRepo(NpcRepository from, NpcRepository to) {
		Set<Npc> oldRepoNpcs = from.getNpcs();

		if (oldRepoNpcs.isEmpty()) {
			return;
		}

		Bukkit.broadcastMessage("Transfering Npcs from " + from.getClass()
				.getSimpleName() + " to "
				+ to.getClass()
						.getSimpleName()
				+ "...");

		for (Npc npc : oldRepoNpcs) {
			this.npcRepo.updateNpc(npc);
			from.deleteNpc(npc);
		}
	}

	public long getNpcId() {
		return this.config.getId();
	}

	public boolean getCheckPlaceholder() {
		return this.config.getCheckPlaceholder();
	}

	public long countNpcId() {
		long id = this.config.countId();
		if (this.saveConfig())
			return id;
		else
			return System.currentTimeMillis();
	}

	private boolean saveConfig() {

		File path = new File(myNpcConfigpath);

		if (!path.exists()) {
			path.mkdirs();
		}

		try (FileWriter writer = new FileWriter(this.configFile)) {
			this.gson.toJson(this.config, writer);
			return true;
		} catch (IOException ex) {
			HastebinReporter.getDefaultReporter()
					.reportError(this.getClass(), ex, "can't save config");
			ex.printStackTrace();
			return false;
		}
	}

	public boolean isUsePermission() {
		return config.isUsePermission();
	}
}
