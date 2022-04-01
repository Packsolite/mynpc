package de.packsolite.mynpc.file.repository.impl;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;

import com.google.gson.Gson;

import de.liquiddev.util.bukkit.HastebinReporter;
import de.packsolite.mynpc.Texts;
import de.packsolite.mynpc.file.FileManager;
import de.packsolite.mynpc.file.repository.NpcRepository;
import de.packsolite.mynpc.npc.Npc;
import lombok.Getter;

@Getter
public class FileRepository implements NpcRepository {

	static final String path = "plugins/myNpc/npcs";

	private FileManager fm;
	private Gson gson;

	public FileRepository(FileManager fm) {
		this.fm = fm;
		this.gson = fm.getGson();
	}

	public void deleteNpc(Npc npc) {
		File file = new File(path + "/" + npc.getId()
				.toLowerCase() + ".json");
		file.delete();
	}

	private Npc readFile(File file) {

		try (FileReader reader = new FileReader(file)) {
			Npc npc = this.gson.fromJson(reader, Npc.class);

			if (!file.getName()
					.toLowerCase()
					.equalsIgnoreCase(npc.getId() + ".json")) {
				HastebinReporter.getDefaultReporter()
						.reportError(this.getClass(), "inconsistency error", "filename " + file.getName() + " does not match npcid " + npc.getId());
				return null;
			}

			return npc;
		} catch (Exception e) {
			HastebinReporter.getDefaultReporter()
					.reportError(this.getClass(), e, "could not read npc " + file.getName());
			e.printStackTrace();
			return null;
		}
	}

	public Set<Npc> getNpcs() {
		File fp = new File(path);

		Set<Npc> npcs = new HashSet<>();

		if (fp.exists()) {

			File[] files = fp.listFiles();

			Bukkit.getConsoleSender()
					.sendMessage(Texts.PREFIX + "Loading " + files.length + " npcs from file repo...");

			for (int i = 0; i < files.length; i++) {

				Npc npc = this.readFile(files[i]);

				if (npc == null) {
					continue;
				}

				npcs.add(npc);
			}
		} else {
			fp.mkdirs();
		}

		return npcs;
	}

	public void updateNpc(Npc npc) {
		File file = new File(path + "/" + npc.getId()
				.toLowerCase() + ".json");
		try (FileWriter fw = new FileWriter(file)) {
			this.gson.toJson(npc, fw);
		} catch (IOException e) {
			HastebinReporter.getDefaultReporter()
					.reportError(this.getClass(), e, "could not save npc " + npc.getId() + " from " + npc.getCreatorName() + " in world " + npc.getWorld());
			e.printStackTrace();
		}
	}

	public File getPath() {
		return new File(path);
	}

	@Override
	public void saveChanges() {
	}
}
