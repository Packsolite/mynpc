package de.packsolite.mynpc.npc;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import de.liquiddev.util.bukkit.HastebinReporter;
import de.liquiddev.util.bukkit.MultiVersion;
import de.packsolite.mynpc.MyNpc;
import de.packsolite.mynpc.Texts;
import de.packsolite.mynpc.util.LabymodEmote;
import de.packsolite.mynpc.util.MyNpcPlaces;
import de.packsolite.mynpc.util.Result;
import lombok.Getter;

@Getter
public class NpcManager {

	private static final Pattern NAME_PATTERN = Pattern.compile("[\\wäÄöÖüÜß§ ]{1,16}");

	private Set<Npc> npcs;
	private MyNpc myNpc;
	private Set<Player> playersMoved = new HashSet<Player>();

	public NpcManager(Set<Npc> npcs, MyNpc myNpc) {
		this.npcs = npcs;
		this.myNpc = myNpc;

		// fix npcs
		this.fixNpcs();

		Bukkit.getScheduler()
				.scheduleSyncRepeatingTask(myNpc, () -> {
					for (Npc npc : npcs) {
						try {
							npc.update();
						} catch (Exception ex) {
							ex.printStackTrace();
							HastebinReporter.getDefaultReporter()
									.reportError(NpcManager.class, ex, "could not update npc #" + npc.getId());
						}
					}
				}, 9, 9 /* use uncommon value to distribute lag ticks */);

		Bukkit.getScheduler()
				.scheduleSyncRepeatingTask(myNpc, () -> {
					for (Npc npc : npcs) {
						try {
							npc.updateLook();
						} catch (Exception ex) {
							ex.printStackTrace();
							HastebinReporter.getDefaultReporter()
									.reportError(NpcManager.class, ex, "could not update look for npc  #" + npc.getId());
						}
					}
				}, 2, 2);
	}

	public void updatePlayer(Player player) {
		for (Npc npc : npcs) {
			try {
				npc.updateInRange(player);
			} catch (Exception ex) {
				ex.printStackTrace();
				HastebinReporter.getDefaultReporter()
						.reportError(NpcManager.class, ex, "could not update npc #" + npc.getId() + " for player " + player.getName());
			}
		}
	}

	private void fixNpcs() {
		Iterator<Npc> itr = npcs.iterator();

		while (itr.hasNext()) {
			Npc npc = itr.next();

			// was the world deleted?
			if (Bukkit.getWorld(npc.getWorld()) == null) {
				HastebinReporter.getDefaultReporter()
						.reportError(this.getClass(), "inconsistency error", "world " + npc.getWorld() + " from npc " + npc.getId() + " not found");
				this.myNpc.getFileManager()
						.getNpcRepo()
						.deleteNpc(npc);
				itr.remove();
				continue;
			}

			/*
			 * long threshold = this.myNpc.getDeleteOldNpcsThreshold();
			 * 
			 * if (threshold > -1 && System.currentTimeMillis() - npc.getLastClick() >
			 * threshold) { Bukkit.getConsoleSender().sendMessage(Texts.PREFIX +
			 * "Deleting old npc: " + npc.getId());
			 * this.myNpc.getFileManager().getNpcRepo().deleteNpc(npc); itr.remove();
			 * continue; }
			 */

			// create
			npc.createNmsNpc();
		}
	}

	public void despawnAllNpcs(Player player) {
		for (Npc npc : this.npcs) {
			if (npc.getRecipients()
					.contains(player)) {
				npc.destroyNpc(player);
			}
		}
	}

	public boolean canEditNpc(Player player, Npc npc) {
		if (myNpc.getFileManager()
				.isUsePermission() && !player.hasPermission(Texts.PERMISSION_USE)) {
			return false;
		}
		String playerUuid = player.getUniqueId()
				.toString();
		return playerUuid.equalsIgnoreCase(npc.getCreatorUuid()) || player.hasPermission(Texts.PERMISSION_ADMIN);
	}

	public int countNpcs(String playerUuid) {
		return (int) this.npcs.stream()
				.filter(npc -> playerUuid.equalsIgnoreCase(npc.getCreatorUuid()))
				.count();
	}

	public Npc getNearestNpc(Location location) {
		final World world = location.getWorld();
		Npc nearest = null;
		double distanceSq = 9;

		for (Npc npc : this.npcs) {
			final Location npcLocation = npc.getLocation();

			if (!world.equals(npcLocation.getWorld()))
				continue;

			double newDistanceSq = location.distanceSquared(npcLocation);

			if (newDistanceSq < distanceSq) {
				nearest = npc;
				distanceSq = newDistanceSq;
			}
		}
		return nearest;
	}

	public boolean canCreateNPC(Player player) {

		long count = this.getNpcsCount(player);

		// has permission?
		if (!player.hasPermission(Texts.PERMISSION_ADMIN) && count >= getMaxNpcs(player)) {
			player.sendMessage(Texts.PREFIX + Texts.NPC_LIMIT_REACHED);
			return false;
		}

		Location l = player.getLocation();

		// too close?
		Npc nearestNpc = this.getNearestNpc(l);
		if (nearestNpc != null && nearestNpc.getLocation()
				.distanceSquared(l) < 1) {
			player.sendMessage(Texts.PREFIX + Texts.NPC_TOO_CLOSE);
			return false;
		}

		// can set here?
		if (!player.hasPermission(Texts.PERMISSION_ADMIN) && !MyNpcPlaces.canPlace(player, l)) {
			player.sendMessage(Texts.PREFIX + Texts.CANT_PLACE_HERE);
			return false;
		}
		return true;
	}

	public Result createNpc(Player player, String name, Skins skin) {

		if (!this.canCreateNPC(player)) {
			return Result.FAIL;
		}

		// valid name?
		if (!this.isNameValid(name)) {
			player.sendMessage(Texts.PREFIX + Texts.NPC_NAME_INVALID);
			return Result.FAIL;
		}

		// generate id
		String id = String.valueOf(this.getMyNpc()
				.getFileManager()
				.countNpcId());

		while (this.getNpcById(id) != null) {
			id = String.valueOf(System.currentTimeMillis());
		}

		// create npc & save to file
		Location l = player.getLocation();
		Npc npc = new Npc(id, name, player.getUniqueId()
				.toString(), player.getName(),
				player.getWorld()
						.getName(),
				l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch(), skin.getSkin(player));
		this.npcs.add(npc);
		this.myNpc.getFileManager()
				.getNpcRepo()
				.updateNpc(npc);

		// spawn npc
		for (Player all : Bukkit.getOnlinePlayers()) {
			npc.updateInRange(all);
		}

		player.sendMessage(Texts.PREFIX + Texts.NPC_CREATED);
		return Result.SUCCESS;
	}

	public Result renameNpc(Player player, Npc npc, String newName) {

		// was deleted while he was in gui?
		if (!this.npcs.contains(npc)) {
			player.sendMessage(Texts.PREFIX + Texts.NPC_NOT_FOUND);
			return Result.FAIL;
		}

		// permission
		if (!player.hasPermission(Texts.PERMISSION_ADMIN) && !npc.getCreatorUuid()
				.equalsIgnoreCase(player.getUniqueId()
						.toString())) {
			player.sendMessage(Texts.PREFIX + Texts.NOT_YOUR_NPC);
			return Result.FAIL;
		}

		// valid name?
		if (!this.isNameValid(newName)) {
			player.sendMessage(Texts.PREFIX + Texts.NPC_NAME_INVALID);
			return Result.FAIL;
		}

		String oldName = npc.getName();
		this.getMyNpc()
				.getFileManager()
				.getNpcRepo()
				.deleteNpc(npc);
		npc.setName(newName);
		this.getMyNpc()
				.getFileManager()
				.getNpcRepo()
				.updateNpc(npc);
		player.sendMessage(Texts.PREFIX + Texts.NPC_RANAMED.replace("%w", oldName)
				.replace("%s", npc.getName()));
		return Result.SUCCESS;
	}

	public boolean isNameValid(String name) {
		return NAME_PATTERN.matcher(name)
				.matches();
	}

	public Result deleteNpc(Player player, String id) {
		Npc w = this.getNpcById(id);

		if (w == null) {
			player.sendMessage(Texts.PREFIX + Texts.NPC_NOT_FOUND);
			return Result.FAIL;
		}

		return this.deleteNpc(player, w);
	}

	public Result deleteNpc(Player player, Npc npc) {

		// was deleted while he was in gui?
		if (!this.npcs.contains(npc)) {
			player.sendMessage(Texts.PREFIX + Texts.NPC_NOT_FOUND);
			return Result.FAIL;
		}

		if (!canEditNpc(player, npc)) {
			player.sendMessage(Texts.PREFIX + Texts.NOT_YOUR_NPC);
			return Result.FAIL;
		}

		// remove npc
		this.npcs.remove(npc);
		this.myNpc.getFileManager()
				.getNpcRepo()
				.deleteNpc(npc);
		npc.removeForAll();

		player.sendMessage(Texts.PREFIX + Texts.NPC_DELETED);
		return Result.SUCCESS;
	}

	public int getMaxNpcs(Player player) {
		int max = 0;
		for (PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
			if (pai.getPermission()
					.startsWith(Texts.PERMISSION_LIMIT)) {
				String count = pai.getPermission()
						.substring(Texts.PERMISSION_LIMIT.length());
				if (count.equalsIgnoreCase("*")) {
					return Integer.MAX_VALUE;
				}
				try {
					int i = Integer.parseInt(count);
					if (i > max)
						max = i;
				} catch (NumberFormatException ex) {
					Bukkit.getConsoleSender()
							.sendMessage(Texts.PREFIX + "§cInvalid permission: " + pai.getPermission());
				}
			}
		}
		return max;
	}

	public Npc getNpcById(String name) {
		return npcs.stream()
				.filter(w -> w.getId()
						.equalsIgnoreCase(name))
				.findFirst()
				.orElse(null);
	}

	private long getNpcsCount(Player player) {
		return npcs.stream()
				.filter(w -> w.getCreatorUuid()
						.equalsIgnoreCase(player.getUniqueId()
								.toString()))
				.count();
	}

	public Result moveNpc(Player player, String id) {
		Npc w = this.getNpcById(id);

		if (w == null) {
			player.sendMessage(Texts.PREFIX + Texts.NPC_NOT_FOUND);
			return Result.FAIL;
		}

		return this.moveNpc(player, w);
	}

	public Result moveNpc(Player player, Npc npc) {

		// was deleted while he was in gui?
		if (!this.npcs.contains(npc)) {
			player.sendMessage(Texts.PREFIX + Texts.NPC_NOT_FOUND);
			return Result.FAIL;
		}

		if (!canEditNpc(player, npc)) {
			player.sendMessage(Texts.PREFIX + Texts.NOT_YOUR_NPC);
			return Result.FAIL;
		}

		// too close?
		this.npcs.remove(npc);
		Npc nearestNpc = this.getNearestNpc(player.getLocation());
		if (nearestNpc != null && nearestNpc.getLocation()
				.distanceSquared(player.getLocation()) < 1) {
			player.sendMessage(Texts.PREFIX + Texts.NPC_TOO_CLOSE);
			this.npcs.add(npc);
			return Result.FAIL;
		}
		this.npcs.add(npc);

		// can set here?
		if (!player.hasPermission(Texts.PERMISSION_ADMIN) && !MyNpcPlaces.canPlace(player, player.getLocation())) {
			player.sendMessage(Texts.PREFIX + Texts.CANT_PLACE_HERE);
			return Result.FAIL;
		}

		// move npc
		npc.setLocation(player.getLocation());
		this.getMyNpc()
				.getFileManager()
				.getNpcRepo()
				.updateNpc(npc);

		// respawn npc
		for (Player all : Bukkit.getOnlinePlayers()) {
			if (npc.getRecipients()
					.contains(all)) {
				npc.destroyNpc(all);
			}
			npc.updateInRange(all);
		}

		player.sendMessage(Texts.PREFIX + Texts.NPC_MOVED);
		return Result.SUCCESS;
	}

	public Result setSkin(Player player, Npc npc, Skins skin) {

		// skin valid?
		if (Skins.Custom.equals(skin) && (skin.getValue() == null || skin.getValue()
				.length() == 0)) {
			player.sendMessage(Texts.PREFIX + Texts.SKIN_NOT_FOUND);
			return Result.FAIL;
		}

		// was deleted while he was in gui?
		if (!this.npcs.contains(npc)) {
			player.sendMessage(Texts.PREFIX + Texts.NPC_NOT_FOUND);
			return Result.FAIL;
		}

		if (!canEditNpc(player, npc)) {
			player.sendMessage(Texts.PREFIX + Texts.NOT_YOUR_NPC);
			return Result.FAIL;
		}

		// set npc skin
		npc.setSkin(skin.getSkin(player));
		this.getMyNpc()
				.getFileManager()
				.getNpcRepo()
				.updateNpc(npc);
		player.sendMessage(Texts.PREFIX + Texts.NPC_SKIN_SET.replace("%s", skin.toString()));
		return Result.SUCCESS;
	}

	public Result setSneak(Player player, Npc npc, boolean sneak) {

		// was deleted while he was in gui?
		if (!this.npcs.contains(npc)) {
			player.sendMessage(Texts.PREFIX + Texts.NPC_NOT_FOUND);
			return Result.FAIL;
		}

		if (!canEditNpc(player, npc)) {
			player.sendMessage(Texts.PREFIX + Texts.NOT_YOUR_NPC);
			return Result.FAIL;
		}

		// set npc skin
		npc.setSneaking(sneak);
		this.getMyNpc()
				.getFileManager()
				.getNpcRepo()
				.updateNpc(npc);
		player.sendMessage(Texts.PREFIX + Texts.NPC_SNEAK_SET.replace("%s", (sneak ? "§aAn" : "§cAus")));
		return Result.SUCCESS;
	}

	public Result setFollowHead(Player player, Npc npc, boolean follow) {

		// was deleted while he was in gui?
		if (!this.npcs.contains(npc)) {
			player.sendMessage(Texts.PREFIX + Texts.NPC_NOT_FOUND);
			return Result.FAIL;
		}

		if (!canEditNpc(player, npc)) {
			player.sendMessage(Texts.PREFIX + Texts.NOT_YOUR_NPC);
			return Result.FAIL;
		}

		// set npc skin
		npc.setFollowHead(follow);
		this.getMyNpc()
				.getFileManager()
				.getNpcRepo()
				.updateNpc(npc);
		player.sendMessage(Texts.PREFIX + Texts.NPC_FOLLOW_HEAD_SET.replace("%s", (follow ? "§aAn" : "§cAus")));
		return Result.SUCCESS;
	}

	/**
	 * Sets the emote id the npc should perform when a player is nearby. Set emoteId
	 * 0 to disable.
	 * 
	 * @param player the player invoking this change
	 * @param npc    the targeted {@link Npc}
	 * @param emote  {@link LabymodEmote}
	 * @return {@link Result}
	 */
	public Result setEmote(Player player, Npc npc, LabymodEmote emote) {

		// was deleted while he was in gui?
		if (!this.npcs.contains(npc)) {
			player.sendMessage(Texts.PREFIX + Texts.NPC_NOT_FOUND);
			return Result.FAIL;
		}

		if (!canEditNpc(player, npc)) {
			player.sendMessage(Texts.PREFIX + Texts.NOT_YOUR_NPC);
			return Result.FAIL;
		}

		if (!player.hasPermission(emote.getPermission()) && emote != LabymodEmote.NONE) {
			player.sendMessage(Texts.PREFIX + Texts.EMOTE_NOT_OWNED);
			return Result.FAIL;
		}

		// set npc emoteId
		npc.setEmoteId(emote.getId());
		this.getMyNpc()
				.getFileManager()
				.getNpcRepo()
				.updateNpc(npc);
		if (emote == LabymodEmote.NONE) {
			player.sendMessage(Texts.PREFIX + Texts.NPC_EMOTE_RESET);
			npc.stopEmote();
		} else {
			player.sendMessage(Texts.PREFIX + String.format(Texts.NPC_EMOTE_SET, emote.getDisplayName()));
		}
		return Result.SUCCESS;
	}

	public Result setClickActionText(Player player, Npc npc, String text) {

		// was deleted while he was in gui?
		if (!this.npcs.contains(npc)) {
			player.sendMessage(Texts.PREFIX + Texts.NPC_NOT_FOUND);
			return Result.FAIL;
		}

		if (!canEditNpc(player, npc)) {
			player.sendMessage(Texts.PREFIX + Texts.NOT_YOUR_NPC);
			return Result.FAIL;
		}

		if (text.startsWith("./")) {
			text = text.substring(1);
		}

		String[] components = text.split("§§");

		for (String action : components) {
			action = action.trim();

			if (action.startsWith("/")) {
				if (!player.hasPermission(Texts.PERMISSION_ADMIN)) {
					player.sendMessage(Texts.PREFIX + Texts.NO_PERMISSION);
					return Result.FAIL;
				}
			}
		}

		if (text.length() < 2) {
			text = "";
		}

		// set npc skin
		npc.setClickActionText(text);
		this.getMyNpc()
				.getFileManager()
				.getNpcRepo()
				.updateNpc(npc);
		player.sendMessage(Texts.PREFIX + (text.length() > 0 ? Texts.NPC_TEXT_SET : Texts.NPC_TEXT_DISABLED));
		return Result.SUCCESS;
	}

	public Result teleportPlayer(Player player, String name) {
		Npc w = this.getNpcById(name);

		if (w == null) {
			player.sendMessage(Texts.PREFIX + Texts.NPC_NOT_FOUND);
			return Result.FAIL;
		}

		return this.teleportPlayer(player, w);
	}

	public Result teleportPlayer(Player player, Npc npc) {

		// was deleted while he was in gui?
		if (!this.npcs.contains(npc)) {
			player.sendMessage(Texts.PREFIX + Texts.NPC_NOT_FOUND);
			return Result.FAIL;
		}

		Location loc = npc.getLocation()
				.clone();

		// safety checks...
		while (loc.getY() < 255 && (loc.getBlock()
				.getType()
				.isSolid()
				|| loc.getBlock()
						.isLiquid())) {
			loc.add(0, 1, 0);
		}
		while (loc.getY() > 0 && !loc.getBlock()
				.getType()
				.isSolid() && !loc.getBlock()
						.isLiquid()) {
			loc.add(0, -1, 0);
		}

		player.teleport(loc.add(0, 1, 0));
		player.sendMessage(Texts.PREFIX + Texts.TELEPORTED);
		player.playSound(player.getLocation(), MultiVersion.SOUND_ENDERMAN_TELEPORT, 0.6f, 1.2f);
		return Result.SUCCESS;
	}
}
