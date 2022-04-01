package de.packsolite.mynpc.npc;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import de.liquiddev.util.bukkit.ItemStackSerializer;
import de.liquiddev.util.bukkit.MultiVersion;
import de.liquiddev.util.bukkit.TextureUtil;
import de.liquiddev.util.bukkit.holo.HoloLine;
import de.liquiddev.util.bukkit.labymod.LabymodController;
import de.liquiddev.util.common.math.MathUtil;
import de.packsolite.mynpc.MyNpc;
import de.packsolite.mynpc.Texts;
import de.packsolite.mynpc.gui.Menu;
import de.packsolite.mynpc.gui.menu.NpcInfoMenu;
import de.packsolite.mynpc.npc.impl.NmsNpc13;
import de.packsolite.mynpc.npc.impl.NmsNpc8;
import lombok.Getter;

@Getter
public class Npc implements Serializable {
	private static final long serialVersionUID = 1L;

	private String id, name, creatorUuid, creatorName, world, skinValue, skinSignature, equipment, clickActionText;

	private double x, y, z;
	private float yaw, pitch;
	private long timeCreated;
	private boolean sneak, followHead, showCreator;
	private int emoteId;

	private transient NmsNpc nmsnpc;
	private transient HoloLine npcTitleHolo;
	private transient ItemStack[] equipmentStack;
	private transient String textureUrl;
	private transient long lastClick;
	private transient Player lookTarget;
	private transient Set<Player> inRangePlayers;

	public Npc(String id, String name, String creatorUuid, String creatorName, String world, double x, double y, double z, float yaw, float pitch, String[] skin) {
		this.id = id;
		this.name = name;
		this.creatorUuid = creatorUuid;
		this.creatorName = creatorName;
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
		this.timeCreated = System.currentTimeMillis();
		this.clickActionText = "";
		this.sneak = false;
		this.followHead = false;
		this.showCreator = true;

		if (skin != null) {
			this.skinValue = skin[0];
			this.skinSignature = skin[1];
		}

		ItemStack[] equipmentStack = new ItemStack[6];
		ItemStack emtpyStack = new ItemStack(Material.AIR);

		for (int i = 0; i < equipmentStack.length; i++) {
			equipmentStack[i] = emtpyStack;
		}

		this.equipment = ItemStackSerializer.getString(equipmentStack);
		this.createNmsNpc();
	}

	// no args constructor for gson to override old fields with default
	public Npc() {
		this.showCreator = true;
	}

	public void setLocation(Location loc) {
		this.x = loc.getX();
		this.y = loc.getY();
		this.z = loc.getZ();
		this.yaw = loc.getYaw();
		this.pitch = loc.getPitch();
		this.world = loc.getWorld()
				.getName();
		this.nmsnpc.teleport(loc, true);
		this.nmsnpc.rotateHead(loc.getPitch(), loc.getYaw());

		// only if show creator is enabled
		if (this.isShowCreator()) {
			this.npcTitleHolo.setLocation(loc);
			for (Player all : this.nmsnpc.getRecipients()) {
				this.npcTitleHolo.updateLocation(all);
			}
		}
	}

	public void setCreatorName(String name) {
		this.creatorName = name;
	}

	public void setClickActionText(String name) {
		this.clickActionText = name;
	}

	public void setEmoteId(int emoteId) {
		this.emoteId = emoteId;
		this.doEmote(emoteId);
	}

	public void setEquipment(int slot, ItemStack item) {
		this.nmsnpc.setEquipment(slot, item);
		ItemStack[] equipmentStack = this.getEquipmentStack();
		equipmentStack[slot] = item;
		this.equipment = ItemStackSerializer.getString(equipmentStack);
	}

	public void setSneaking(boolean b) {
		this.sneak = b;
		this.nmsnpc.setSneaking(b);

		// only if show creator is enabled
		if (this.isShowCreator()) {
			this.npcTitleHolo.setYOffset(getHoloOffset());

			for (Player all : this.nmsnpc.getRecipients()) {
				this.npcTitleHolo.updateLocation(all);
			}
		}
	}

	public void setFollowHead(boolean b) {
		this.followHead = b;
	}

	/**
	 * Called every 500ms (10th tick) to update the NPC and play emotes.
	 */
	public void update() {
		final Location npcLoc = this.getLocation();
		final World npcWorld = npcLoc.getWorld();
		final NpcManager npcManager = MyNpc.getInstance()
				.getNpcmanager();
		final Collection<Player> recipients = this.getRecipients();
		final Set<Player> playersMoved = npcManager.getPlayersMoved();
		final Optional<LabymodController> lmc = MyNpc.getInstance()
				.getLabymodController();

		// save nearest for updateLook()
		Player nearestPlayer = null;
		double nearestDist = 25;

		for (Player player : getInRangePlayers()) {
			if (!player.getWorld()
					.equals(npcWorld)) {
				continue;
			}

			Location playerLoc = player.getLocation();
			boolean isSpawned = recipients.contains(player);

			if (!isSpawned) {
				/* LabyMod has a PlayerList cache */
				boolean canSpawn = (lmc.isPresent() && lmc.get()
						.isLabyModUser(player)) || (isOnScreen(playerLoc, npcLoc) && playersMoved.contains(player));
				if (canSpawn) {
					this.spawnNpc(player);
				}
			} else {
				if (!this.followHead && emoteId == 0) {
					continue;
				}

				double distSqrt = playerLoc.distanceSquared(npcLoc);
				if (distSqrt < nearestDist) {
					nearestDist = distSqrt;
					nearestPlayer = player;
				}
			}
		}

		// prepare for updateLook() and do emote
		if (nearestPlayer != lookTarget) {
			lookTarget = nearestPlayer;

			// if somebody approaches the NPC
			if (nearestPlayer != null) {
				this.doEmote(this.emoteId);
			}
			// reset yaw if lootTarget is null and yaw has changed
			else if (nmsnpc.getLocation()
					.getYaw() != this.yaw) {
				npcLoc.setYaw(yaw);
				npcLoc.setPitch(pitch);
				this.nmsnpc.rotateHead(pitch, yaw);
			}
		}
	}

	public void updateInRange(Player player) {
		final MyNpc mynpc = MyNpc.getInstance();
		final Location npcLoc = this.getLocation();
		final Collection<Player> recipients = this.getRecipients();
		final Set<Player> playersInRange = getInRangePlayers();

		// is in other world?
		if (!player.getWorld()
				.equals(npcLoc.getWorld())) {
			recipients.remove(player);
			playersInRange.remove(player);
			return;
		}

		// distance
		Location playerLoc = player.getLocation();
		int xDiff = Math.abs(playerLoc.getBlockX() - npcLoc.getBlockX());
		int zDiff = Math.abs(playerLoc.getBlockZ() - npcLoc.getBlockZ());
		double chunkDist = Math.max(xDiff, zDiff);
		boolean isInRange = chunkDist < mynpc.getViewDistance();

		if (isInRange) {
			if (playersInRange.add(player)) {
				Set<Player> playersMoved = mynpc.getNpcmanager()
						.getPlayersMoved();
				boolean canSpawn = isOnScreen(player.getLocation(), npcLoc) && playersMoved.contains(player);
				if (canSpawn) {
					this.spawnNpc(player);
				}
			}
		} else {
			if (playersInRange.remove(player)) {
				boolean isSpawned = recipients.contains(player);
				if (isSpawned) {
					this.destroyNpc(player);
				}
			}
		}
	}

	public void updateLook() {
		if (lookTarget == null || !this.followHead) {
			return;
		}

		Location npcLoc = this.getLocation();
		Location playerLoc = lookTarget.getLocation();
		float yaw = (float) MathUtil.toDegrees(Math.atan2((float) npcLoc.getX() - (float) playerLoc.getX(), (float) playerLoc.getZ() - (float) npcLoc.getZ()));

		if (npcLoc.getYaw() == yaw) {
			return;
		}

		float pitch = (float) MathUtil.toDegrees(Math.atan2((float) npcLoc.getY() - (float) playerLoc.getY(), 3.5f));

		// apply offset to slightly look down
		pitch += 5;

		npcLoc.setYaw(yaw);
		npcLoc.setPitch(pitch);
		// this.nmsnpc.teleport(npcLoc, true);
		this.nmsnpc.rotateHead(pitch, yaw);
	}
	// uuid: 94927d63ae2d47ffbf3c58ecf5fb6061

	private boolean isOnScreen(Location player, Location location) {
		double deltaX = location.getX() - player.getX();
		double deltaY = location.getY() - player.getY();
		double deltaZ = location.getZ() - player.getZ();
		Vector n = new Vector(deltaX, deltaY, deltaZ).normalize();
		Vector n2 = player.getDirection();
		double cross = n.dot(n2);
		return cross > 0.2;
	}

	public void createNmsNpc() {
		Location loc = new Location(Bukkit.getWorld(this.world), this.x, this.y, this.z, this.yaw, this.pitch);

		if (MultiVersion.isVersionHigherThan(1, 8)) {
			this.nmsnpc = new NmsNpc13(this.name, loc, MyNpc.getInstance());
		} else {
			this.nmsnpc = new NmsNpc8(this, new HashSet<Player>(), this.name, new UUID(new Random().nextLong(), 0), loc, "", false);
		}

		this.nmsnpc.setTablistName("");

		// skin
		if (this.skinValue != null) {
			this.nmsnpc.setSkin(this.skinValue, this.skinSignature);
		}

		// equipment
		ItemStack[] equipmentStack = this.getEquipmentStack();

		if (equipmentStack != null) {
			for (int i = 0; i < equipmentStack.length; i++) {
				if (equipmentStack[i] != null && equipmentStack[i].getType() != Material.AIR) {
					this.nmsnpc.setEquipment(i, equipmentStack[i]);
				}
			}
		}

		// sneak
		if (this.sneak) {
			this.nmsnpc.setSneaking(true);
		}

		// spawn title tag
		this.npcTitleHolo = new HoloLine(this.getLocation(), Texts.NPC_HEADER.replace("%s", this.creatorName), getHoloOffset());
	}

	private double getHoloOffset() {
		return this.sneak ? 0.85f : 1.1f /* 1.075 would be exact */;
	}

	public ItemStack[] getEquipmentStack() {
		if (this.equipmentStack == null) {
			this.equipmentStack = ItemStackSerializer.getItemStackArray(this.equipment);
		}
		return this.equipmentStack;
	}

	public Collection<Player> getRecipients() {
		return this.nmsnpc.getRecipients();
	}

	public Location getLocation() {
		return this.nmsnpc.getLocation();
	}

	public void destroyNpc(Player player) {
		this.nmsnpc.destroy(player);

		// only if show creator is enabled
		if (this.isShowCreator()) {
			this.npcTitleHolo.remove(player);
		}
	}

	public void spawnNpc(Player player) {
		Optional<LabymodController> lmc = MyNpc.getInstance()
				.getLabymodController();
		boolean labymod = lmc.isPresent() && lmc.get()
				.isLabyModUser(player);

		/* LabyMod has a PlayerList cache */
		int tabDuration = labymod ? 1 : 40;
		this.nmsnpc.spawn(false, false, player, tabDuration);

		// only if show creator is enabled
		if (this.isShowCreator()) {
			this.npcTitleHolo.spawn(player);
		}
	}

	public void removeForAll() {
		// remove creator holo
		if (this.isShowCreator()) {
			for (Player all : this.nmsnpc.getRecipients()) {
				this.npcTitleHolo.remove(all);
			}
		}

		// remove npc
		this.nmsnpc.destroyAll();
	}

	public void setShowCreator(boolean showCreator) {
		this.showCreator = showCreator;

		if (showCreator) {
			for (Player all : this.nmsnpc.getRecipients()) {
				this.npcTitleHolo.spawn(all);
			}
		} else {
			for (Player all : this.nmsnpc.getRecipients()) {
				this.npcTitleHolo.remove(all);
			}
		}
	}

	public void setName(String name) {
		this.name = name;
		try {
			this.nmsnpc.setDisplayName(name);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.nmsnpc.teleport(this.nmsnpc.getLocation(), true);
	}

	public void setSkin(String[] skin) {
		this.removeForAll();
		this.skinValue = skin[0];
		this.skinSignature = skin[1];
		this.textureUrl = TextureUtil.getTextureUrl(this.skinValue);
		this.createNmsNpc();
		this.update();
	}

	public String getTextureUrl() {
		if (this.textureUrl == null) {
			this.textureUrl = TextureUtil.getTextureUrl(this.skinValue);
		}
		return this.textureUrl;
	}

	public Set<Player> getInRangePlayers() {
		if (this.inRangePlayers == null) {
			inRangePlayers = new HashSet<>();
		}
		return inRangePlayers;
	}

	public void onRightClick(Player player) {

		if (this.clickActionText == null || this.clickActionText.length() == 0) {
			return;
		}

		if (System.currentTimeMillis() - lastClick < 300)
			return;

		this.lastClick = System.currentTimeMillis();

		String[] components = clickActionText.split("§§");

		for (String action : components) {
			action = action.trim();

			boolean isCommand = action.startsWith("/");

			if (isCommand) {
				String command = action.substring(1);
				dispatchCommand(player, command);
			} else {
				player.sendMessage("§8[§lNPC§8] §7" + this.name + " §7» " + action);
			}
		}
	}

	private void dispatchCommand(Player player, String command) {
		command = command.replace("{player}", player.getName())
				.replace("{npcuuid}", this.nmsnpc.getNpcUuid()
						.toString());
		if (command.startsWith("console:")) {
			command = command.substring(8);
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
		} else {
			player.performCommand(command);
		}
	}

	public void onLeftClick(Player player) {
		MyNpc myNpc = MyNpc.getInstance();

		if (myNpc.getNpcmanager()
				.canEditNpc(player, this)) {
			Menu infoMenu = new NpcInfoMenu(myNpc.getMenuManager(), null, this, player);
			myNpc.getMenuManager()
					.setCurrentScreen(player, infoMenu);
		}
	}

	public void doEmote(int emoteId) {
		if (emoteId != 0) {
			Optional<LabymodController> lmc = MyNpc.getInstance()
					.getLabymodController();
			if (lmc.isPresent()) {
				lmc.get()
						.playEmote(this.nmsnpc.getNpcUuid(), emoteId, this.getRecipients()
								.toArray(new Player[] {}));
			}
		}
	}

	public void stopEmote() {
		doEmote(-1);
	}
}
