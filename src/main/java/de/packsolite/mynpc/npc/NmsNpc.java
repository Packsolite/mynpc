package de.packsolite.mynpc.npc;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface NmsNpc {
	void teleport(Location npcLoc, boolean b);

	void setSkin(String skinValue, String skinSignature);

	void setEquipment(int i, ItemStack itemStack);

	void setSneaking(boolean b);

	Collection<Player> getRecipients();

	Location getLocation();

	void destroy(Player player);

	void spawn(boolean b, boolean c, Player player, int tabRemoveDelay);

	void setDisplayName(String name) throws IOException;

	void rotateHead(float pitch, float yaw);

	int getEntityId();

	void destroyAll();

	void setTablistName(String name);

	UUID getNpcUuid();
}