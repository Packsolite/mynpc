package de.packsolite.mynpc.file.repository.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;

import de.packsolite.mynpc.Texts;
import de.packsolite.mynpc.file.FileManager;
import de.packsolite.mynpc.file.repository.NpcRepository;
import de.packsolite.mynpc.npc.Npc;

public class SqliteRepository implements NpcRepository {

	private Connection sqlConnection;
	private FileManager manager;

	public SqliteRepository(FileManager manager) throws SQLException {
		this.manager = manager;
		this.connect(3);
	}

	private void connect(int maxAttempts) throws SQLException {
		boolean connected = false;
		int attempts = 0;

		while (!connected) {
			attempts++;
			try {
				this.connect();
				connected = true;
			} catch (SQLException ex) {
				if (attempts >= maxAttempts) {
					throw ex;
				} else {
					ex.printStackTrace();
					Bukkit.getConsoleSender()
							.sendMessage(Texts.PREFIX + "Could not connect to sqlite, retry in 3 seconds...");
					try {
						Thread.sleep(TimeUnit.SECONDS.toMillis(3));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void connect() throws SQLException {
		try {
			File path = new File(this.manager.getMyNpcConfigpath());
			if (!path.exists()) {
				path.mkdirs();
			}
			Class.forName("org.sqlite.JDBC");
			sqlConnection = DriverManager.getConnection("jdbc:sqlite:" + this.manager.getMyNpcConfigpath() + "database.db");
			sqlConnection.setAutoCommit(false);
			this.initialize();
		} catch (Exception ex) {
			throw new SQLException("Could not connect");
		}
	}

	private void initialize() throws SQLException {
		Statement state = sqlConnection.createStatement();
		state.execute("CREATE TABLE IF NOT EXISTS npcs (npc_id INTEGER PRIMARY KEY, npc_obj BLOB NOT NULL)");
	}

	@Override
	public Set<Npc> getNpcs() {
		HashSet<Npc> npcSet = new HashSet<Npc>();

		try {
			Statement state = sqlConnection.createStatement();
			ResultSet res = state.executeQuery("SELECT * FROM npcs");

			while (res.next()) {
				try {
					ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(res.getBytes("npc_obj")));
					Npc npc = (Npc) objIn.readObject();
					npcSet.add(npc);
				} catch (Exception ex) {
					System.out.println("Could not read npc " + res.getInt("npc_id"));
					ex.printStackTrace();
				}
			}

			Bukkit.getConsoleSender()
					.sendMessage(Texts.PREFIX + "Loading " + npcSet.size() + " npcs from sqlite repo...");

		} catch (SQLException ex) {
			ex.printStackTrace();
		}

		return npcSet;
	}

	@Override
	public void updateNpc(Npc npc) {
		try {
			PreparedStatement psm = sqlConnection.prepareStatement("INSERT OR REPLACE INTO npcs VALUES (" + npc.getId() + ",?)");
			ByteArrayOutputStream bao = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bao);
			oos.writeObject(npc);
			oos.close();
			psm.setBytes(1, bao.toByteArray());
			psm.execute();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void deleteNpc(Npc npc) {
		try {
			PreparedStatement psm = sqlConnection.prepareStatement("DELETE FROM npcs WHERE npc_id=" + npc.getId());
			psm.execute();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void saveChanges() {
		try {
			this.sqlConnection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
