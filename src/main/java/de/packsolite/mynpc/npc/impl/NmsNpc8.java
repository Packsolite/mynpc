package de.packsolite.mynpc.npc.impl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import de.packsolite.mynpc.MyNpc;
import de.packsolite.mynpc.npc.NmsNpc;
import de.packsolite.mynpc.npc.Npc;
import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.MobEffect;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutAnimation;
import net.minecraft.server.v1_8_R3.PacketPlayOutBed;
import net.minecraft.server.v1_8_R3.PacketPlayOutBlockChange;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntity.PacketPlayOutEntityLook;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEffect;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.PacketPlayOutRemoveEntityEffect;
import net.minecraft.server.v1_8_R3.WorldSettings.EnumGamemode;

public class NmsNpc8 implements NmsNpc {

	// VER 1.2

	// <----- static stuff ----->
	public static ArrayList<NmsNpc8> players = new ArrayList<NmsNpc8>();

	public static NmsNpc8 getByName(String name) {
		for (NmsNpc8 fp : players) {
			if (fp.getName()
					.equalsIgnoreCase(name))
				return fp;
		}
		return null;
	}

	public static String getUUIDFromPlayerName(String playername) {
		return FakePlayerRaw.getUUID(playername);
	}

	public static void removeAll() {
		ArrayList<NmsNpc8> fps = (ArrayList<NmsNpc8>) players.clone();
		for (NmsNpc8 fp : fps) {
			fp.remove();
		}
	}

	// <----- object ----->
	FakePlayerRaw fp;

	private boolean canSeenByAll;
	private boolean isInTablist = true;
	private boolean isSneaking = false;
	private boolean isBurning = false;
	private boolean isInvisible = false;
	private int ping = 1;

	private Npc npc;

	public NmsNpc8(Npc parent, Player see, String nameTag, UUID gameProfile, Location spawn, String skinUUID, boolean loadSkin) {
		this.npc = parent;
		players.add(this);
		canSeenByAll = false;

		if (gameProfile == null)
			gameProfile = new UUID(new Random().nextLong(), 0);

		HashSet<Player> cansee = new HashSet<Player>();
		cansee.add(see);
		this.fp = new FakePlayerRaw(cansee, nameTag, gameProfile, spawn);

		if (loadSkin && skinUUID != null)
			fp.setSkin(skinUUID.toString());

		this.fp.spawn(null, 40);
	}

	public NmsNpc8(Npc parent, HashSet<Player> canSee, String nameTag, UUID gameProfile, Location spawn, String skinUUID, boolean loadSkin) {
		this.npc = parent;
		players.add(this);
		canSeenByAll = false;

		if (gameProfile == null)
			gameProfile = new UUID(new Random().nextLong(), 0);

		this.fp = new FakePlayerRaw(canSee, nameTag, gameProfile, spawn);

		if (loadSkin && skinUUID != null)
			fp.setSkin(skinUUID.toString());

		fp.spawn(null, 40);
	}

	public NmsNpc8(Npc parent, String nameTag, UUID gameProfile, Location spawn, String skinUUID, boolean loadSkin) {
		this.npc = parent;
		players.add(this);

		canSeenByAll = true;

		if (gameProfile == null)
			gameProfile = new UUID(new Random().nextLong(), 0);

		this.fp = new FakePlayerRaw(null, nameTag, gameProfile, spawn);

		if (loadSkin && skinUUID != null)
			fp.setSkin(skinUUID.toString());

		fp.spawn(null, 40);
	}

	public boolean isSpawned() {
		if (this.fp == null)
			return false;
		return true;
	}

	public int getEntityID() {
		return this.fp.entityID;
	}

	public Location getLocation() {
		return this.fp.location;
	}

	public String getName() {
		return this.fp.name;
	}

	public HashSet<Player> getPlayersWhoCanSee() {
		return this.fp.cansee;
	}

	public void setLocation(Location l) {
		if (this.fp != null)
			this.fp.teleport(l);
	}

	public boolean canSeenByAll() {
		return this.canSeenByAll;
	}

	public void rotateHeadOnly(float yaw, float pitch) {
		if (this.fp != null)
			this.fp.headRotation(null, yaw, pitch);
	}

	public void rotate(float yaw, float pitch) {
		if (this.fp != null)
			this.fp.Swing(yaw, pitch);
		this.fp.location.setYaw(yaw);
		this.fp.location.setPitch(pitch);
	}

	public void addToTablist() {
		if (this.fp != null) {
			this.fp.addToTablist(null);
			this.isInTablist = true;
		}
	}

	public void removeFromTablist() {
		try {
			this.fp.rmvFromTablist(null);
			this.isInvisible = false;
		} catch (Exception ex) {
		}
	}

	public boolean isInTablist() {
		return this.isInTablist;
	}

	public void setItemInHand(ItemStack item) {
		this.fp.setEquipment(item, 0);
	}

	public ItemStack getItemInHand() {
		return this.fp.getEquipment(0);
	}

	public void setBoots(ItemStack item) {
		this.fp.setEquipment(item, 1);
	}

	public ItemStack getBoots() {
		return this.fp.getEquipment(1);
	}

	public void setLeggings(ItemStack item) {
		this.fp.setEquipment(item, 2);
	}

	public ItemStack getLeggings() {
		return this.fp.getEquipment(2);
	}

	public void setChestplate(ItemStack item) {
		this.fp.setEquipment(item, 3);
	}

	public ItemStack getChestplate() {
		return this.fp.getEquipment(3);
	}

	public void setHelmet(ItemStack item) {
		this.fp.setEquipment(item, 4);
	}

	public ItemStack getHelmet() {
		return this.fp.getEquipment(4);
	}

	public void playSwingArmAnimation() {
		this.fp.playAnimation(0);
	}

	public void playDamageAnimation() {
		this.fp.playAnimation(1);
	}

	public void addPotionEffect(PotionEffect pe) {
		this.fp.addPotionEffect(pe);
	}

	public void removePotionEffect(PotionEffectType type) {
		this.fp.removePotionEffect(type);
	}

	public boolean isSneaking() {
		return this.isSneaking;
	}

	public boolean isBurning() {
		return this.isBurning;
	}

	public boolean isSInvisible() {
		return this.isInvisible;
	}

	public int getPing() {
		return this.ping;
	}

	public void setBed() {
		this.fp.setBed();
	}

	public void setSneaking(boolean b) {
		if (b) {
			this.isBurning = false;
			this.isInvisible = false;
			this.fp.setStatus(0, (byte) 0x02, true);
		} else {
			this.fp.setStatus(0, (byte) 0x00, true);
		}
		this.isSneaking = b;
	}

	public void setSneaking(Player p, boolean b) {
		if (b) {
			this.fp.setStatus(p, 0, (byte) 0x02, true);
		} else {
			this.fp.setStatus(p, 0, (byte) 0x00, true);
		}
	}

	public void setBurning(boolean b) {
		if (b) {
			this.isSneaking = false;
			this.isInvisible = false;
			this.fp.setStatus(0, (byte) 0x01, true);
		} else {
			this.fp.setStatus(0, (byte) 0x00, true);
		}
		this.isBurning = b;
	}

	public void setInvisible(boolean b) {
		if (b) {
			this.isBurning = false;
			this.isSneaking = false;
			this.fp.setStatus(0, (byte) 0x20, true);
		} else {
			this.fp.setStatus(0, (byte) 0x00, true);
		}
		this.isInvisible = b;
	}

	public void setPing(int ping) {
		if (this.isSpawned()) {
			this.ping = ping;
			this.fp.updatePing(this.ping);
		}
	}

	public void remove() {
		try {
			this.removeFromTablist();
			this.fp.destroy();
			this.fp = null;
		} catch (Exception ex) {
		}
		players.remove(this);
	}

	public HashSet<Player> getRecipients() {
		return this.fp.cansee;
	}

	public void setEquipment(int slot, ItemStack item) {
		this.fp.setEquipment(item, slot - 1);
	}

	/**
	 * API pars
	 */

	@Override
	public void teleport(Location npcLoc, boolean b) {
		this.setLocation(npcLoc);
	}

	@Override
	public void setSkin(String skinValue, String skinSignature) {
		this.fp.setSkin(skinValue, skinSignature);
	}

	@Override
	public void destroy(Player player) {
		this.fp.destroy(player);
		this.fp.cansee.remove(player);
	}

	@Override
	public void destroyAll() {
		this.fp.cansee.stream()
				.forEach(fp::destroy);
		this.fp.cansee.clear();
	}

	@Override
	public void spawn(boolean b, boolean c, Player player, int tabRemoveDelay) {
		this.fp.spawn(player, tabRemoveDelay);
		this.fp.cansee.add(player);

		if (this.isSneaking) {
			this.setSneaking(player, true);
		}

		// equipment
		ItemStack[] equipmentStack = this.npc.getEquipmentStack();

		if (equipmentStack != null) {
			for (int i = 0; i < equipmentStack.length; i++) {
				if (equipmentStack[i] != null && equipmentStack[i].getType() != Material.AIR) {
					this.setEquipment(i, equipmentStack[i]);
				}
			}
		}
	}

	@Override
	public void setDisplayName(String name) throws IOException {
		this.fp.destroy();
		this.fp.cansee.clear();
		this.fp.setName(name);
		this.fp.setSkin(this.npc.getSkinValue(), this.npc.getSkinSignature());
	}

	@Override
	public void rotateHead(float pitch, float yaw) {
		this.fp.headRotation(null, yaw, pitch);
	}

	@Override
	public int getEntityId() {
		return this.getEntityID();
	}

	@Override
	public void setTablistName(String name) {
		this.fp.tablistName = name;
	}

	@Override
	public UUID getNpcUuid() {
		return fp.gameprofile.getId();
	}
}

class FakePlayerRaw {

	public int entityID;
	public Location location;
	public GameProfile gameprofile;
	public String name;
	public String tablistName;
	public DataWatcher dw;

	public HashSet<Player> cansee = new HashSet<Player>();

	public FakePlayerRaw(HashSet<Player> canSee, String name, UUID gameProfile, Location location) {

		if (canSee != null)
			this.cansee = canSee;

		this.name = name;
		this.tablistName = name;
		entityID = (int) Math.ceil(Math.random() * Integer.MAX_VALUE);
		gameprofile = new GameProfile(gameProfile, name);
		this.location = location.clone();
	}

	public void setName(String name) {
		this.name = name;
		this.tablistName = name;
		gameprofile = new GameProfile(gameprofile.getId(), name);
		this.destroy();
		this.spawn(null, 40);
	}

	public void spawn(Player p, int tabRemoveDelay) {
		PacketPlayOutNamedEntitySpawn packet = new PacketPlayOutNamedEntitySpawn();

		setValue(packet, "a", entityID);
		setValue(packet, "b", gameprofile.getId());
		setValue(packet, "c", getFixLocation(location.getX()));
		setValue(packet, "d", getFixLocation(location.getY()));
		setValue(packet, "e", getFixLocation(location.getZ()));
		setValue(packet, "f", getFixRotation(location.getYaw()));
		setValue(packet, "g", getFixRotation(location.getPitch()));
		setValue(packet, "h", 0);
		this.dw = new DataWatcher(null);
		dw.a(6, (float) 20);
		dw.a(10, (byte) 127);
		setValue(packet, "i", dw);
		addToTablist(p);

		if (p == null) {
			sendPacket(packet);
		} else {
			((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
		}

		headRotation(p, location.getYaw(), location.getPitch());

		Bukkit.getScheduler()
				.runTaskLater(MyNpc.getInstance(), () -> {
					rmvFromTablist(p);
				}, tabRemoveDelay);
	}

	@SuppressWarnings("deprecation")
	public void addPotionEffect(PotionEffect pe) {
		PacketPlayOutEntityEffect packet = new PacketPlayOutEntityEffect(this.entityID, new MobEffect(pe.getType()
				.getId(), pe.getDuration()));

		this.sendPacket(packet);
	}

	@SuppressWarnings("deprecation")
	public void removePotionEffect(PotionEffectType type) {
		PacketPlayOutRemoveEntityEffect packet = new PacketPlayOutRemoveEntityEffect();
		setValue(packet, "a", this.entityID);
		setValue(packet, "b", (byte) type.getId());

		this.sendPacket(packet);
	}

	public void setStatus(Player player, int status, byte event, boolean b) {
		DataWatcher dw = new DataWatcher(null);
		dw.a(status, event);
		// dw.a(0, (byte) 0x01);
		PacketPlayOutEntityMetadata packet12 = new PacketPlayOutEntityMetadata(this.entityID, dw, b);

		sendPacket(packet12, player);
	}

	public void setStatus(int status, byte event, boolean b) {
		DataWatcher dw = new DataWatcher(null);
		dw.a(status, event);
		// dw.a(0, (byte) 0x01);
		PacketPlayOutEntityMetadata packet12 = new PacketPlayOutEntityMetadata(this.entityID, dw, b);

		sendPacket(packet12);
	}

	public static HashMap<String, String> skinFetched = new HashMap<String, String>();

	public static String getUUID(String playername) {
		try {
			Gson gson = new Gson();
			String url = "https://api.mojang.com/users/profiles/minecraft/" + playername;
			String json = getStringFromURL(url);
			String uuid = gson.fromJson(json, JsonObject.class)
					.get("id")
					.getAsString();

			return uuid;
		} catch (Exception ex) {
			return null;
		}
	}

	public void setSkin(final String uuid) {

		if (skinFetched.isEmpty()) {
			skinFetched.put("ef30e4fccf0e4702b95172a31532ce45",
					"{\"id\":\"ef30e4fccf0e4702b95172a31532ce45\",\"name\":\"CokeJokeYT\",\"properties\":[{\"name\":\"textures\",\"value\":\"eyJ0aW1lc3RhbXAiOjE1MjIxNTM1NzU0NzgsInByb2ZpbGVJZCI6ImVmMzBlNGZjY2YwZTQ3MDJiOTUxNzJhMzE1MzJjZTQ1IiwicHJvZmlsZU5hbWUiOiJDb2tlSm9rZVlUIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9mYTQ0NTI5OTI4YTJjYzIyMjI0Y2I5ZDI4M2ZmZmRiMzI1YTE2Mjg4YzMxNGU5NGExNTk4MjUyZWZmNzE4MSIsIm1ldGFkYXRhIjp7Im1vZGVsIjoic2xpbSJ9fX19\",\"signature\":\"UsoR/6DPahqroIGzR4z+/s2Ctc5zuAzAfspBYXgX6YPmxW8F/3RLaWeTA8MBWSUT5dMq+3otnbCSNqQ3AMzipAa0oo8mktHxy0i4hjPzWsqTaql5l/LfsXDNFQE4eQD+ZfCNMFFHU5OKG+rG6Yrpx0wpeeditu+pR5/YqDB914h2KD1M17ZwYYlYChDbLwRWcjCwKiYSDMj9fND5UvZb9HgERxuN5Tl6yal6r8VxV4ncqwCU5Rufn3/hNieOeH4MkUQVTqmharqewXCDAI8MV9700sIj8fm2I8D+JnAxwX0WZc3ZonJ8modzBDu4Dh9EmgLAxy6PIWgrZlzquPBY9nLB0ddZK4BhATQpGABUipO5AIFWxZewdEMdq7dHrmhvH3NjR4kVwa6WDTz+2cD/fOnyzl72VT+VbClueUsb2cnqJIEDnwX2smc1fD77ilUToJIwO8+byXu6YtGsl2e/VruK527uEfgYmUZ4w8/sJdVLlMhBCreVUsK5kMHF9bvXslNiSIPRL0privs4AiXcfn3IgwkAQzRHnknDyUElmAj6l9XOXiZj2nkaDCAOVy0y8zArQcjiwz6ajhiia8DtChT1ApijGLekjOrA0xzzqhaRtxIdODZf0gERCs5ZcSxXFsPYPrSzwNvkBgtZIerCsvxuQ6AHhpZ/yMR2Z8hR/ng=\"}]}");
		}

		try {
			Gson gson = new Gson();
			String json = "";

			if (!skinFetched.containsKey(uuid)) {
				String url = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false";
				json = getStringFromURL(url);
				skinFetched.put(uuid, json);
			} else {
				json = skinFetched.get(uuid);
			}

			JsonObject mainObject = gson.fromJson(json, JsonObject.class);
			JsonObject jObject = mainObject.get("properties")
					.getAsJsonArray()
					.get(0)
					.getAsJsonObject();
			String value = jObject.get("value")
					.getAsString();
			String signatur = jObject.get("signature")
					.getAsString();

			gameprofile.getProperties()
					.put("textures", new Property("textures", value, signatur));

		} catch (Exception ex) {
			System.out.println("Error by getting skin : " + uuid);
		}
	}

	private static String getStringFromURL(String url) {
		String text = "";
		try {
			Scanner scanner = new Scanner(new URL(url).openStream());
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				while (line.startsWith(" ")) {
					line = line.substring(1);
				}
				text = text + line;
			}
			scanner.close();
		} catch (IOException e) {
		}
		return text;
	}

	public void setBed() {
		BlockPosition bedLoc = new BlockPosition((int) this.location.getX(), 0, (int) this.location.getZ());

		PacketPlayOutBlockChange change = new PacketPlayOutBlockChange(((CraftWorld) location.getWorld()).getHandle(), bedLoc);
		change.block = Block.getByCombinedId(26);
		sendPacket(change);

		PacketPlayOutBed bed = new PacketPlayOutBed();
		setValue(bed, "a", this.entityID);
		setValue(bed, "b", bedLoc);

		sendPacket(bed);
		this.teleport(this.location);
	}

	public void setSkin(String value, String signatur) {
		gameprofile.getProperties()
				.put("textures", new Property("textures", value, signatur));
	}

	public void teleport(Location location) {
		PacketPlayOutEntityTeleport packet = new PacketPlayOutEntityTeleport();
		setValue(packet, "a", entityID);
		setValue(packet, "b", getFixLocation(location.getX()));
		setValue(packet, "c", getFixLocation(location.getY()));
		setValue(packet, "d", getFixLocation(location.getZ()));
		setValue(packet, "e", getFixRotation(location.getYaw()));
		setValue(packet, "f", getFixRotation(location.getPitch()));
		setValue(packet, "g", Math.random() < 0.5 ? true : false);

		sendPacket(packet);
		headRotation(null, location.getYaw(), location.getPitch());
		this.location = location.clone();
	}

	public void headRotation(Player p, float yaw, float pitch) {
		PacketPlayOutEntityLook packet = new PacketPlayOutEntityLook(entityID, getFixRotation(yaw), getFixRotation(pitch), true);
		PacketPlayOutEntityHeadRotation packetHead = new PacketPlayOutEntityHeadRotation();
		setValue(packetHead, "a", entityID);
		setValue(packetHead, "b", getFixRotation(yaw));

		if (p == null)
			sendPacket(packet);
		else
			((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);

		if (p == null)
			sendPacket(packetHead);
		else
			((CraftPlayer) p).getHandle().playerConnection.sendPacket(packetHead);
	}

	public void Swing(float yaw, float pitch) {
		PacketPlayOutAnimation packet = new PacketPlayOutAnimation();
		PacketPlayOutAnimation packetHead = new PacketPlayOutAnimation();
		setValue(packetHead, "a", entityID);

		sendPacket(packet);
		sendPacket(packetHead);
	}

	public void playAnimation(int id) {
		PacketPlayOutAnimation packet = new PacketPlayOutAnimation();
		setValue(packet, "a", entityID);
		setValue(packet, "b", id);

		sendPacket(packet);
	}

	public void destroy() {
		PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(new int[] { entityID });
		sendPacket(packet);
	}

	public void destroy(Player player) {
		PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(new int[] { entityID });
		sendPacket(packet, player);
	}

	public void addToTablist(Player p) {
		PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo();
		PacketPlayOutPlayerInfo.PlayerInfoData data = packet.new PlayerInfoData(gameprofile, 1, EnumGamemode.SURVIVAL, CraftChatMessage.fromString(tablistName)[0]);
		List<PacketPlayOutPlayerInfo.PlayerInfoData> players = (List<PacketPlayOutPlayerInfo.PlayerInfoData>) getValue(packet, "b");
		players.add(data);

		setValue(packet, "a", PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER);
		setValue(packet, "b", players);

		if (p == null)
			sendPacket(packet);
		else
			((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
	}

	HashMap<Integer, ItemStack> equip = new HashMap<Integer, ItemStack>();

	public ItemStack getEquipment(int slot) {
		if (equip.containsKey(slot))
			return equip.get(slot);
		else
			return new ItemStack(Material.AIR);
	}

	public void setEquipment(ItemStack is, int slot) {
		net.minecraft.server.v1_8_R3.ItemStack mcStack = CraftItemStack.asNMSCopy(is);
		PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(this.entityID, slot, mcStack);
		equip.put(slot, is.clone());

		this.sendPacket(packet);
	}

	public void updatePing(int ping) {
		PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo();
		PacketPlayOutPlayerInfo.PlayerInfoData data = packet.new PlayerInfoData(gameprofile, ping, EnumGamemode.SURVIVAL, CraftChatMessage.fromString(gameprofile.getName())[0]);
		@SuppressWarnings("unchecked")
		List<PacketPlayOutPlayerInfo.PlayerInfoData> players = (List<PacketPlayOutPlayerInfo.PlayerInfoData>) getValue(packet, "b");
		players.add(data);

		setValue(packet, "a", PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_LATENCY);
		setValue(packet, "b", players);

		sendPacket(packet);
	}

	public void rmvFromTablist(Player p) {
		PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo();
		PacketPlayOutPlayerInfo.PlayerInfoData data = packet.new PlayerInfoData(gameprofile, 1, EnumGamemode.SURVIVAL, CraftChatMessage.fromString(tablistName)[0]);
		@SuppressWarnings("unchecked")
		List<PacketPlayOutPlayerInfo.PlayerInfoData> players = (List<PacketPlayOutPlayerInfo.PlayerInfoData>) getValue(packet, "b");
		players.add(data);

		setValue(packet, "a", PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER);
		setValue(packet, "b", players);

		if (p == null) {
			sendPacket(packet);
		} else {
			sendPacket(packet, p);
		}
	}

	public int getFixLocation(double pos) {
		return (int) MathHelper.floor(pos * 32.0D);
	}

	public byte getFixRotation(float yawpitch) {
		return (byte) ((int) (yawpitch * 256.0F / 360.0F));
	}

	public void setValue(Object obj, String name, Object value) {
		try {
			Field field = obj.getClass()
					.getDeclaredField(name);
			field.setAccessible(true);
			field.set(obj, value);
		} catch (Exception e) {
		}
	}

	public Object getValue(Object obj, String name) {
		try {
			Field field = obj.getClass()
					.getDeclaredField(name);
			field.setAccessible(true);
			return field.get(obj);
		} catch (Exception e) {
		}
		return null;
	}

	private static final int maxDistanceSquared = 998001;

	public void sendPacket(Packet<?> packet) {
		if (this.cansee == null) {
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (player.getWorld()
						.equals(this.location.getWorld())
						&& player.getLocation()
								.distanceSquared(this.location) < maxDistanceSquared)
					((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
			}
			return;
		}

		for (Player player : this.cansee) {
			if (player.getWorld()
					.equals(this.location.getWorld())
					&& player.getLocation()
							.distanceSquared(this.location) < maxDistanceSquared)
				((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
		}
	}

	public void sendPacket(Packet<?> packet, Player player) {
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
	}
}
