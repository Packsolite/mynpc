package de.packsolite.mynpc.file.repository;

import java.util.Set;

import de.packsolite.mynpc.npc.Npc;

public interface NpcRepository {
	public Set<Npc> getNpcs();

	public void updateNpc(Npc holo);

	public void deleteNpc(Npc npc);

	public void saveChanges();
}
