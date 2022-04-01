package de.packsolite.mynpc.npc.impl;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import de.liquiddev.util.bukkit.MultiVersion;
import de.liquiddev.util.bukkit.Reflections;
import de.liquiddev.util.bukkit.Reflections.ConstructorInvoker;
import de.liquiddev.util.bukkit.Reflections.MethodInvoker;
import de.packsolite.mynpc.npc.NmsNpc;

public class NmsNpc13 implements NmsNpc {

	static final Class<?> packetClass = Reflections.getMinecraftClass("Packet");
	static final Class<?> dataWatcherClass = Reflections.getMinecraftClass("DataWatcher");
	static final Class<?> dataWatcherObjectClass = Reflections.getMinecraftClass("DataWatcherObject");
	static final Class<?> dataWatcherRegistryClass = Reflections.getMinecraftClass("DataWatcherRegistry");
	static final Class<?> dataWatcherSerializerClass = Reflections.getMinecraftClass("DataWatcherSerializer");
	static final Class<?> packetEntityMetadataClass = Reflections.getMinecraftClass("PacketPlayOutEntityMetadata");
	static final Class<?> entityClass = Reflections.getMinecraftClass("Entity");
	static final Class<?> blockPositionClass = Reflections.getMinecraftClass("BlockPosition");
	static final Class<?> enumGamemodeClass = Reflections.getMinecraftClass("EnumGamemode");
	static final Class<?> packetPlayerInfoClass = Reflections.getMinecraftClass("PacketPlayOutPlayerInfo");
	static final Class<?> packetPlayerInfoDataClass = Reflections.getMinecraftClass("PacketPlayOutPlayerInfo$PlayerInfoData");
	static final Class<?> iChatBaseComponentClass = Reflections.getMinecraftClass("IChatBaseComponent");
	static final Class<?> enumPlayerInfoActionClass = Reflections.getMinecraftClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
	static final Class<?> craftChatMessageClass = Reflections.getCraftBukkitClass("util.CraftChatMessage");
	static final Class<?> enumItemSlotClass = Reflections.getMinecraftClass("EnumItemSlot");
	static final Class<?> packetEntityEquipmentClass = Reflections.getMinecraftClass("PacketPlayOutEntityEquipment");
	static final Class<?> packetEntityEffectClass = Reflections.getMinecraftClass("PacketPlayOutEntityEffect");
	static final Class<?> mobEffectClass = Reflections.getMinecraftClass("MobEffect");
	static final Class<?> packetAnimationClass = Reflections.getMinecraftClass("PacketPlayOutAnimation");
	static final Class<?> packetBedClass = MultiVersion.isVersionHigherThan(1, 13) ? null : Reflections.getMinecraftClass("PacketPlayOutBed");
	static final Class<?> packetEntityDestroyClass = Reflections.getMinecraftClass("PacketPlayOutEntityDestroy");
	static final Class<?> packetEntityLookClass = Reflections.getMinecraftClass("PacketPlayOutEntity$PacketPlayOutEntityLook");
	static final Class<?> packetEntityHeadRotation = Reflections.getMinecraftClass("PacketPlayOutEntityHeadRotation");
	static final Class<?> packetEntityStatus = Reflections.getMinecraftClass("PacketPlayOutEntityStatus");
	static final Class<?> packetEntityTeleport = Reflections.getMinecraftClass("PacketPlayOutEntityTeleport");
	static final Class<?> packetNamedEntitySpawn = Reflections.getMinecraftClass("PacketPlayOutNamedEntitySpawn");
	static final Class<?> craftItemStackClass = Reflections.getCraftBukkitClass("inventory.CraftItemStack");

	static final MethodInvoker enumItemSlotMethod = Reflections.getMethod(enumItemSlotClass, "values");
	static final MethodInvoker sendBlockChangeMethod = Reflections.getMethod(Player.class, "sendBlockChange", Location.class, Material.class, byte.class);
	static final MethodInvoker craftItemStackNmsCopyMethod = Reflections.getMethod(craftItemStackClass, "asNMSCopy", ItemStack.class);
	static final MethodInvoker iChatFromStringMethod = Reflections.getMethod(craftChatMessageClass, "fromString", String.class);

	static final ConstructorInvoker packetPlayerInfoDataCronstructor = Reflections.getConstructor(packetPlayerInfoDataClass, packetPlayerInfoClass, GameProfile.class, int.class, enumGamemodeClass,
			iChatBaseComponentClass);
	static final ConstructorInvoker blockPositionConstructor = Reflections.getConstructor(blockPositionClass, double.class, double.class, double.class);
	static final ConstructorInvoker packetEntityEffectConstructor = Reflections.getConstructor(packetEntityEffectClass, int.class, mobEffectClass);
	static final ConstructorInvoker packetEntityDestroyConstructor = Reflections.getConstructor(packetEntityDestroyClass, int[].class);
	static final ConstructorInvoker packetEntityLookConstructor = Reflections.getConstructor(packetEntityLookClass, int.class, byte.class, byte.class, boolean.class);

	static final Object enumGamemodeNotSet = Reflections.getField(enumGamemodeClass, "NOT_SET", enumGamemodeClass)
			.get(null);
	static final Object emumPlayerInfoActionRemovePlayer = Reflections.getField(enumPlayerInfoActionClass, "REMOVE_PLAYER", enumPlayerInfoActionClass)
			.get(null);
	static final Object emumPlayerInfoActionUpdateDisplayName = Reflections.getField(enumPlayerInfoActionClass, "UPDATE_DISPLAY_NAME", enumPlayerInfoActionClass)
			.get(null);
	static final Object emumPlayerInfoActionAddPlayer = Reflections.getField(enumPlayerInfoActionClass, "ADD_PLAYER", enumPlayerInfoActionClass)
			.get(null);
	private static Object[] poses;

	private int entityId;
	private GameProfile gameprofile;
	private Location location;
	private boolean sneaking;
	Object[] equipment = new Object[6];

	private Collection<Player> recipients;
	private HashMap<Player, BukkitRunnable> tablistRemoveRunnable = new HashMap<>();

	private String display_name;
	private String tablist_name;
	private Object dataWatcher;
	private Object object_entity_state;
	private Object object_entity_pose;

	private Plugin plugin;

	/*
	 * initialize all the npc default settings
	 */
	public NmsNpc13(String name, Location location, Plugin plugin) {
		this.plugin = plugin;
		this.display_name = name;
		this.tablist_name = name;
		this.recipients = new HashSet<Player>(); // HashSet for faster contains()
		this.entityId = (int) Math.ceil(Math.random() * Integer.MAX_VALUE);
		UUID uuid = new UUID(new Random().nextLong(), 0);
		this.gameprofile = new GameProfile(uuid, display_name);
		this.location = location;

		try {
			// create data watcher
			this.dataWatcher = dataWatcherClass.getConstructor(entityClass)
					.newInstance(new Object[] { null });

			// prepare methods
			Method registerDataWatcherMethod = dataWatcherClass.getDeclaredMethod("register", dataWatcherObjectClass, Object.class);
			Constructor<?> dataWatcherObjectConstructor = dataWatcherObjectClass.getConstructor(int.class, dataWatcherSerializerClass);

			// Entity state (fire, crouching etc)
			Object a_registry = dataWatcherRegistryClass.getField("a")
					.get(null);
			object_entity_state = dataWatcherObjectConstructor.newInstance(0, a_registry);
			registerDataWatcherMethod.invoke(dataWatcher, object_entity_state, Byte.valueOf((byte) 0));

			if (!MultiVersion.isVersionHigherThan(1, 14)) {
				this.setRegisterDataWatcher(registerDataWatcherMethod, dataWatcherObjectConstructor, 1, "b", 300);
				this.setRegisterDataWatcher(registerDataWatcherMethod, dataWatcherObjectConstructor, 6, "a", Byte.valueOf((byte) 0));
				this.setRegisterDataWatcher(registerDataWatcherMethod, dataWatcherObjectConstructor, 7, "c", Float.valueOf(20.0f));
				this.setRegisterDataWatcher(registerDataWatcherMethod, dataWatcherObjectConstructor, 8, "b", 0);
				this.setRegisterDataWatcher(registerDataWatcherMethod, dataWatcherObjectConstructor, 10, "b", 0);
				this.setRegisterDataWatcher(registerDataWatcherMethod, dataWatcherObjectConstructor, 11, "c", Float.valueOf(0.0F));
				this.setRegisterDataWatcher(registerDataWatcherMethod, dataWatcherObjectConstructor, 12, "b", 20);
				this.setRegisterDataWatcher(registerDataWatcherMethod, dataWatcherObjectConstructor, 13, "a", Byte.valueOf((byte) 127));
				this.setRegisterDataWatcher(registerDataWatcherMethod, dataWatcherObjectConstructor, 14, "a", Byte.valueOf((byte) 1));
			} else {
				// Entity Pose
				Object s_registry = dataWatcherRegistryClass.getField("s")
						.get(null);
				object_entity_pose = dataWatcherObjectConstructor.newInstance(6, s_registry);
				if (poses == null) {
					poses = (Object[]) Reflections.getMinecraftClass("EntityPose")
							.getMethod("values")
							.invoke(null);
				}
				registerDataWatcherMethod.invoke(dataWatcher, object_entity_pose, poses[0]);

				this.setRegisterDataWatcher(registerDataWatcherMethod, dataWatcherObjectConstructor, 16, "a", Byte.valueOf((byte) 127));
				this.setRegisterDataWatcher(registerDataWatcherMethod, dataWatcherObjectConstructor, 17, "a", Byte.valueOf((byte) 1));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void setRegisterDataWatcher(Method registerDataWatcherMethod, Constructor<?> dataWatcherObjectConstructor, int id, String registry, Object val)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, NoSuchFieldException, SecurityException {
		Object the_registry = dataWatcherRegistryClass.getField(registry)
				.get(null);
		registerDataWatcherMethod.invoke(dataWatcher, dataWatcherObjectConstructor.newInstance(id, the_registry), val);
	}

	/*
	 * view all the packet receivers
	 */
	public Collection<Player> getRecipients() {
		return this.recipients;
	}

	/*
	 * get the signature and skin from the gameprofile
	 */
	public Property getSkin() {
		if (this.gameprofile.getProperties()
				.isEmpty())
			return null;
		return (Property) this.gameprofile.getProperties()
				.get("textures")
				.toArray()[0];
	}

	/*
	 * get npc id
	 */
	public int getEntityId() {
		return entityId;
	}

	/*
	 * get npc location
	 */
	public Location getLocation() {
		return location;
	}

	/*
	 * get npc gameprofile
	 */
	public GameProfile getGameprofile() {
		return gameprofile;
	}

	/*
	 * get npc displayname above head
	 */
	public String getDisplay_name() {
		return display_name;
	}

	/*
	 * get npc displayname in tablist
	 */
	public String getTablist_name() {
		return tablist_name;
	}

	/*
	 * get plugin
	 */
	public Plugin getPlugin() {
		return plugin;
	}

	/*
	 * add Player so it receives the update packets
	 */
	public void addRecipient(Player p) {
		this.recipients.add(p);
	}

	/*
	 * Remove Player that receives the update packets
	 */
	public void removeRecipient(Player p) {
		this.recipients.remove(p);
	}

	/*
	 * spawn the npc, this should be the last function after init.
	 */
	@Override
	public void spawn(boolean tablist, boolean fix_head, Player player, int tabRemoveDelay) {
		try {
			Object packet = packetNamedEntitySpawn.newInstance();
			this.setField(packet, "a", this.entityId);
			this.setField(packet, "b", this.gameprofile.getId());
			this.setField(packet, "c", location.getX()); // maybe change this to players coodinates to fix skin loading problems
			this.setField(packet, "d", location.getY());
			this.setField(packet, "e", location.getZ());
			this.setField(packet, "f", fix_head ? (byte) ((int) location.getYaw() * 256.0F / 360.0F) : 0);
			this.setField(packet, "g", fix_head ? (byte) ((int) location.getPitch() * 256.0F / 360.0F) : 0);

			if (!MultiVersion.isVersionHigherThan(1, 14)) {
				this.setField(packet, "h", this.dataWatcher);
			}

			this.addToTabList(player);
			this.sendPacket(packet, player);
			this.sendEquipment(player);
			if (!MultiVersion.isVersionHigherThan(1, 14)) {
				this.setAction(this.sneaking ? (byte) 0x2 : (byte) 0x0);
			}
			this.updatePosition(player, true);

			// if (!this.recipients.contains(player)) <-- unnesesarry for hashset
			this.recipients.add(player);

			if (!tablist) {
				// Delay is required to commit the tablist changes
				BukkitRunnable br = new BukkitRunnable() {
					@Override
					public void run() {
						removeFromTabList(player);
					}
				};

				br.runTaskLater(this.plugin, tabRemoveDelay);
				tablistRemoveRunnable.put(player, br);
			}
		} catch (Exception ex) {
		}
	}

	/*
	 * set the name above the player
	 */
	public void setDisplayNameAboveHead(String name) throws IOException {
		if (name.length() > 16)
			throw new IOException("Name cannot be longer than 16 characters.");
		this.display_name = name;
		this.reloadNpc(0);
	}

	/*
	 * set the name above the player and tablist.
	 */
	public void setDisplayName(String name) throws IOException {
		this.setDisplayNameAboveHead(name);
		this.setTablistName(name);
	}

	/*
	 * set custom name in tablist
	 */
	public void setTablistName(String name) {
		this.tablist_name = name;
		this.updateToTabList();
	}

	/*
	 * respawn the npc and refresh all comitted changes
	 */
	public void reloadNpc(int tabDelay) {
		this.updateProfile();

		Object packet = packetEntityDestroyConstructor.invoke(new int[] { this.entityId });

		for (Player all : this.recipients) {
			this.sendPacket(packet, all);
			this.spawn(false, true, all, tabDelay);
		}
	}

	/*
	 * Update/Refresh the gameprofile that contains UUID, Name, Skin.
	 */
	private void updateProfile() {
		Property skin = this.getSkin();
		this.gameprofile = new GameProfile(this.gameprofile.getId(), this.display_name);
		if (skin != null)
			this.setSkin(skin.getValue(), skin.getSignature());
	}

	/*
	 * set the texture and signature in the gameprofile, to submit it you must
	 * reload player.
	 */
	public void setSkin(String texture, String signature) {
		this.gameprofile.getProperties()
				.put("textures", new Property("textures", texture, signature));
	}

	/*
	 * remove npc from the recipient's tablist
	 */

	public void removeFromTabList(Player player) {
		try {
			Object packet = packetPlayerInfoClass.newInstance();
			Object[] craftMessageList = (Object[]) iChatFromStringMethod.invoke(null, tablist_name);
			Object data = packetPlayerInfoDataCronstructor.invoke(packet, this.gameprofile, 0, enumGamemodeNotSet, craftMessageList[0]);
			List<Object> players = (List<Object>) getField(packet, "b");
			players.add(data);
			this.setField(packet, "a", emumPlayerInfoActionRemovePlayer);
			this.setField(packet, "b", players);
			this.sendPacket(packet, player);
			BukkitRunnable br = tablistRemoveRunnable.remove(player);
			if (br != null) {
				br.cancel();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/*
	 * update npc to the recipient's tablist
	 */
	public void updateToTabList() {
		try {
			Object packet = packetPlayerInfoClass.newInstance();
			Object[] craftMessageList = (Object[]) iChatFromStringMethod.invoke(null, tablist_name);
			Object data = packetPlayerInfoDataCronstructor.invoke(packet, this.gameprofile, 0, enumGamemodeNotSet, craftMessageList[0]);

			List<Object> players = (List<Object>) getField(packet, "b");
			players.add(data);
			this.setField(packet, "a", emumPlayerInfoActionUpdateDisplayName);
			this.setField(packet, "b", players);
			for (Player all : this.recipients)
				this.sendPacket(packet, all);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/*
	 * add npc from the recipient's tablist
	 */

	public void addToTabList(Player player) {
		try {
			Object packet = packetPlayerInfoClass.newInstance();

			Object[] craftMessageList = (Object[]) iChatFromStringMethod.invoke(null, tablist_name);

			Object data = packetPlayerInfoDataCronstructor.invoke(packet, this.gameprofile, 0, enumGamemodeNotSet, craftMessageList[0]);

			List<Object> players = (List<Object>) getField(packet, "b");
			players.add(data);
			this.setField(packet, "a", emumPlayerInfoActionAddPlayer);
			this.setField(packet, "b", players);
			this.sendPacket(packet, player);
			BukkitRunnable br = tablistRemoveRunnable.remove(player);
			if (br != null) {
				br.cancel();
			}

			if (MultiVersion.isVersionHigherThan(1, 14)) {
				Bukkit.getScheduler()
						.runTaskLater(plugin, () -> {
							try {
								Constructor<?> packetEntityMetadataConstructor = packetEntityMetadataClass.getConstructor(int.class, dataWatcherClass, boolean.class);
								Object metadataPacket = packetEntityMetadataConstructor.newInstance(this.entityId, this.dataWatcher, true);
								sendPacket(metadataPacket, player);
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}, 1);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/*
	 * put items in inventory
	 */
	@SuppressWarnings("rawtypes")
	public void setNmsEquipment(int slot, Object item) {

		try {
			this.equipment[slot] = item;
			Object slotEnum = ((Object[]) enumItemSlotMethod.invoke(null))[slot];
			Object packet = packetEntityEquipmentClass.newInstance();
			this.setField(packet, "a", this.entityId);

			if (MultiVersion.VERSION_HIGHER_THAN_1_15) {
				Class<?> pairClass = Reflections.getClass("com.mojang.datafixers.util.Pair");
				MethodInvoker pairOfMethod = Reflections.getMethod(pairClass, "of", Object.class, Object.class);
				Object pair = pairOfMethod.invoke(null, slotEnum, item);
				List list = (List) getField(packet, "b");
				list.add(pair);
			} else {
				this.setField(packet, "b", slotEnum);
				this.setField(packet, "c", item);
			}

			for (Player all : this.recipients)
				this.sendPacket(packet, all);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void setEquipment(int slot, ItemStack item) {
		this.setNmsEquipment(slot, craftItemStackNmsCopyMethod.invoke(null, item));
	}

	/*
	 * set player action, such as shift, onfire, ect recommending to use 'public
	 * void setAction(Action action)'.
	 */
	public void setAction(byte action) {
		this.setState(action);
	}

	/*
	 * set player action, such as shift, onfire, ect.
	 */
	public void setAction(Action action) {
		this.setState(action.build());
	}

	/*
	 * Does not support 1.14. Make sure the npc is near a bed or on it.
	 */
	public void setSleep(boolean state) {
		if (packetBedClass == null) {
			System.out.println("Bed packet not compatible with 1.14!");
			return;
		}

		try {
			if (state) {
				Location bed = new Location(this.location.getWorld(), 0, 0, 0);

				Object packet = packetBedClass.newInstance();
				this.setField(packet, "a", this.entityId);
				this.setField(packet, "b", blockPositionConstructor.invoke(bed.getX(), bed.getY(), bed.getZ()));

				for (Player p : this.recipients) {
					sendBlockChange(p, bed, MultiVersion.MATERIAL_BED, (byte) 0);
					sendBlockChange(p, bed.add(0, 0, 1), MultiVersion.MATERIAL_BED, (byte) 2);
				}

				for (Player all : this.recipients)
					this.sendPacket(packet, all);

				this.teleport(location.clone()
						.add(0, 0.3, 0), false);

			} else {
				this.playAnimation(NPCAnimation.LEAVE_BED);
				this.teleport(location.clone()
						.subtract(0, 0.3, 0), true);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void sendBlockChange(Player player, Location loc, Material mat, byte b) {
		try {
			sendBlockChangeMethod.invoke(player, loc, mat, b);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/*
	 * delete the npc from the server.
	 */
	public void destroy(Player player) {
		sendDestroyPackets(player);
		recipients.remove(player);
	}

	@Override
	public void destroyAll() {
		this.recipients.stream()
				.forEach(this::sendDestroyPackets);
	}

	private void sendDestroyPackets(Player player) {
		Object packet = packetEntityDestroyConstructor.invoke(new int[] { this.entityId });
		this.removeFromTabList(player);
		this.sendPacket(packet, player);
	}

	/*
	 * set npc status such as die or hurt. I recommond to use method 'public void
	 * setStatus(NPCStatus status)'
	 */
	public void setStatus(byte status) {
		try {
			Object packet = packetEntityStatus.newInstance();
			this.setField(packet, "a", this.entityId);
			this.setField(packet, "b", status);

			for (Player all : this.recipients)
				this.sendPacket(packet, all);
		} catch (Exception ex) {
		}
	}

	/*
	 * set npc status such as die or hurt.
	 */
	public void setStatus(NPCStatus status) {
		this.setStatus((byte) status.getId());
	}

	/*
	 * set npc effect such as Night Vision or something else.
	 */
	public void setEffect(Object mobEffect) {
		Object packet = packetEntityEffectConstructor.invoke(this.entityId, mobEffect);

		for (Player all : this.recipients)
			this.sendPacket(packet, all);
	}

	/*
	 * play npc animation such as Swing arm ect. I recommend using method 'public
	 * void setAnimation(NPCAnimation animation)'
	 */
	public void playAnimation(byte animation) {
		try {
			Object packet = packetAnimationClass.newInstance();
			this.setField(packet, "a", this.entityId);
			this.setField(packet, "b", animation);

			for (Player all : this.recipients)
				this.sendPacket(packet, all);
		} catch (Exception ex) {
		}
	}

	/*
	 * set npc animation such as Swing arm ect.
	 */
	public void playAnimation(NPCAnimation animation) {
		this.playAnimation((byte) animation.getId());
	}

	/*
	 * teleport npc to different location
	 */
	public void teleport(Location location, boolean onGround) {
		try {
			Object packet = packetEntityTeleport.newInstance();

			setField(packet, "a", Integer.valueOf(this.entityId));
			setField(packet, "b", Double.valueOf(location.getX()));
			setField(packet, "c", Double.valueOf(location.getY()));
			setField(packet, "d", Double.valueOf(location.getZ()));
			setField(packet, "e", Byte.valueOf((byte) (int) location.getYaw()));
			setField(packet, "f", Byte.valueOf((byte) (int) location.getPitch()));
			setField(packet, "g", Boolean.valueOf(onGround));

			for (Player all : this.recipients) {
				sendPacket(packet, all);
			}

			rotateHead(location.getPitch(), location.getYaw());

			this.location = location;
		} catch (Exception exception) {
		}
	}

	/*
	 * update the position for a player
	 */
	public void updatePosition(Player player, Boolean onground) {
		try {
			Object packet = packetEntityTeleport.newInstance();

			this.setField(packet, "a", this.entityId);
			this.setField(packet, "b", location.getX());
			this.setField(packet, "c", location.getY());
			this.setField(packet, "d", location.getZ());
			this.setField(packet, "e", this.getFixRotation((location.getYaw())));
			this.setField(packet, "f", (byte) location.getPitch());
			this.setField(packet, "g", onground);

			this.sendPacket(packet, player);
			this.updateHead(player);
		} catch (Exception ex) {
		}
	}

	/*
	 * rotate npc head to pitch and yaw.
	 */

	public void rotateHead(float pitch, float yaw) {
		try {
			byte fixedYaw = getFixRotation(yaw);
			Object packet = packetEntityLookConstructor.invoke(this.entityId, fixedYaw, (byte) pitch, true);
			Object packet_1 = packetEntityHeadRotation.newInstance();
			this.setField(packet_1, "a", this.entityId);
			this.setField(packet_1, "b", fixedYaw);

			for (Player all : this.recipients) {
				this.sendPacket(packet, all);
				this.sendPacket(packet_1, all);
			}
		} catch (Exception ex) {
		}
	}

	/*
	 * update the head rotation for a player
	 */
	public void updateHead(Player player) {
		try {
			Object packet = packetEntityLookConstructor.invoke(this.entityId, getFixRotation(this.getLocation()
					.getYaw()),
					(byte) this.getLocation()
							.getPitch(),
					true);
			Object packet_1 = packetEntityHeadRotation.newInstance();
			this.setField(packet_1, "a", this.entityId);
			this.setField(packet_1, "b", getFixRotation(this.getLocation()
					.getYaw()));

			this.sendPacket(packet, player);
			this.sendPacket(packet_1, player);
		} catch (Exception ex) {
		}
	}

	/*
	 * set the sneaking state
	 */
	public void setSneaking(boolean b) {
		this.sneaking = b;

		if (MultiVersion.isVersionHigherThan(1, 14)) {
			this.setDataWatcherObject(this.object_entity_pose, b ? poses[5] : poses[0]);
		}

		this.setAction(b ? (byte) 0x2 : (byte) 0x0);
	}

	/*
	 * get the sneaking state
	 */
	public boolean isSneaking() {
		return this.sneaking;
	}

	/*
	 * These methods below are not usefull.
	 */

	private <T> void setDataWatcherObject(Object datawatcherobject, Object t0) {
		try {
			Method m = this.dataWatcher.getClass()
					.getDeclaredMethod("registerObject", dataWatcherObjectClass, Object.class);
			m.setAccessible(true);
			m.invoke(this.dataWatcher, datawatcherobject, t0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setState(byte data) {
		try {
			this.setDataWatcherObject(this.object_entity_state, data);
			Constructor<?> packetEntityMetadataConstructor = packetEntityMetadataClass.getConstructor(int.class, dataWatcherClass, boolean.class);
			Object packet = packetEntityMetadataConstructor.newInstance(this.entityId, this.dataWatcher, true);
			for (Player all : this.recipients) {
				sendPacket(packet, all);
			}
		} catch (Exception ex) {
		}
	}

	private byte getFixRotation(float yawpitch) {
		return (byte) ((int) (this.location.getYaw() * 256.0F / 360.0F));
	}

	@SuppressWarnings("rawtypes")
	private void sendEquipment(Player player) {
		for (int i = 0; i < this.equipment.length; i++) {
			Object item = this.equipment[i];

			if (item == null)
				continue;

			try {
				Object slotEnum = ((Object[]) enumItemSlotMethod.invoke(null))[i];
				Object packet = packetEntityEquipmentClass.newInstance();
				this.setField(packet, "a", this.entityId);

				if (MultiVersion.VERSION_HIGHER_THAN_1_15) {
					Class<?> pairClass = Reflections.getClass("com.mojang.datafixers.util.Pair");
					MethodInvoker pairOfMethod = Reflections.getMethod(pairClass, "of", Object.class, Object.class);
					Object pair = pairOfMethod.invoke(null, slotEnum, item);
					List list = (List) getField(packet, "b");
					list.add(pair);
				} else {
					this.setField(packet, "b", slotEnum);
					this.setField(packet, "c", item);
				}

				this.sendPacket(packet, player);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private Object getField(Object obj, String field_name) {
		try {
			Field field = obj.getClass()
					.getDeclaredField(field_name);
			field.setAccessible(true);
			return field.get(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void setField(Object obj, String field_name, Object value) {
		try {
			Field field = obj.getClass()
					.getDeclaredField(field_name);
			field.setAccessible(true);
			field.set(obj, value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendPacket(Object packet, Player player) {
		try {
			Object handle = player.getClass()
					.getMethod("getHandle")
					.invoke(player);
			Object playerConnection = handle.getClass()
					.getField("playerConnection")
					.get(handle);
			playerConnection.getClass()
					.getMethod("sendPacket", packetClass)
					.invoke(playerConnection, packet);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	enum NPCAnimation {

		SWING_MAIN_HAND(0), TAKE_DAMAGE(1), LEAVE_BED(2), SWING_OFFHAND(3), CRITICAL_EFFECT(4), MAGIC_CRITICAL_EFFECT(5);

		private int id;

		private NPCAnimation(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}

	}

	enum NPCStatus {

		HURT(2), DIE(3);

		private int id;

		private NPCStatus(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}
	}

	public static class Action {

		private boolean on_fire, crouched, sprinting, invisible, glowing, flying_elytra;
		private byte result = 0;

		public Action(boolean on_fire, boolean crouched, boolean sprinting, boolean invisible, boolean glowing, boolean flying_elytra) {
			this.on_fire = on_fire;
			this.crouched = crouched;
			this.sprinting = sprinting;
			this.invisible = invisible;
			this.glowing = glowing;
			this.flying_elytra = flying_elytra;
		}

		public Action() {
		}

		public boolean isOn_fire() {
			return on_fire;
		}

		public Action setOn_fire(boolean on_fire) {
			this.on_fire = on_fire;
			return this;
		}

		public boolean isCrouched() {
			return crouched;
		}

		public Action setCrouched(boolean crouched) {
			this.crouched = crouched;
			return this;
		}

		public boolean isSprinting() {
			return sprinting;
		}

		public Action setSprinting(boolean sprinting) {
			this.sprinting = sprinting;
			return this;
		}

		public boolean isInvisible() {
			return invisible;
		}

		public Action setInvisible(boolean invisible) {
			this.invisible = invisible;
			return this;
		}

		public boolean isGlowing() {
			return glowing;
		}

		public Action setGlowing(boolean glowing) {
			this.glowing = glowing;
			return this;
		}

		public boolean isFlying_elytra() {
			return flying_elytra;
		}

		public Action setFlying_elytra(boolean flying_elytra) {
			this.flying_elytra = flying_elytra;
			return this;
		}

		public byte build() {
			result = 0;
			result = add(this.on_fire, (byte) 0x01);
			result = add(this.crouched, (byte) 0x02);
			result = add(this.sprinting, (byte) 0x08);
			result = add(this.invisible, (byte) 0x20);
			result = add(this.glowing, (byte) 0x40);
			result = add(this.flying_elytra, (byte) 0x80);
			return result;
		}

		private byte add(boolean condition, byte amount) {
			return (byte) (result += (condition ? amount : 0x00));
		}
	}

	@Override
	public UUID getNpcUuid() {
		return this.gameprofile.getId();
	}
}