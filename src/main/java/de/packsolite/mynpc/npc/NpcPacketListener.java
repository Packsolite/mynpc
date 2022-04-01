package de.packsolite.mynpc.npc;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import de.liquiddev.util.bukkit.HastebinReporter;
import de.liquiddev.util.bukkit.Reflections;
import de.liquiddev.util.bukkit.Reflections.FieldAccessor;
import de.liquiddev.util.bukkit.Reflections.MethodInvoker;
import de.packsolite.mynpc.MyNpc;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

public class NpcPacketListener implements Listener {
	static final String INJECTOR_NAME = "PacketInjectorMyNPC";

	Plugin pl;

	public static ArrayList<PacketInjector> injectors = new ArrayList<PacketInjector>();

	private static PacketInjector getInjectorByPlayer(Player p) {
		for (PacketInjector inj : injectors) {
			if (inj.player.equals(p))
				return inj;
		}
		return null;
	}

	public static void uninject(Player p) {
		PacketInjector inj = getInjectorByPlayer(p);

		if (inj != null) {
			inj.uninject();
			injectors.remove(inj);
		}
	}

	public static void inject(Player p, NpcPacketListener listener) {
		PacketInjector inj = new PacketInjector(p, listener);
		inj.inject();
		injectors.add(inj);
	}

	public static void uninjectAll() {
		ArrayList<PacketInjector> injects = (ArrayList<PacketInjector>) injectors.clone();

		for (PacketInjector inj : injects) {
			inj.uninject();
			injectors.remove(inj);
		}
	}

	public NpcPacketListener(Plugin plugin, boolean registerInjectorsAutomaticly) {
		this.pl = plugin;

		if (registerInjectorsAutomaticly) {
			Bukkit.getPluginManager()
					.registerEvents(this, this.pl);

			for (Player all : Bukkit.getOnlinePlayers()) {
				inject(all, this);
			}
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();

		if (!p.isOnline()) {
			return;
		}

		PacketInjector inj = new PacketInjector(p, this);
		inj.inject();
		injectors.add(inj);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		uninject(e.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onQuit1(PlayerKickEvent e) {
		if (e.isCancelled()) {
			return;
		}
		uninject(e.getPlayer());
	}

}

class PacketInjector {
	static Class<?> craftPlayerClass = Reflections.getCraftBukkitClass("entity.CraftPlayer");
	static MethodInvoker getHandleMethod = Reflections.getMethod(craftPlayerClass, "getHandle");
	static Class<?> playerConnectionClass = Reflections.getMinecraftClass("PlayerConnection");
	static Class<?> entityPlayerClass = Reflections.getMinecraftClass("EntityPlayer");
	static FieldAccessor<?> playerConnectionField = Reflections.getField(entityPlayerClass, "playerConnection", playerConnectionClass);
	static Class<?> networkManagerClass = Reflections.getMinecraftClass("NetworkManager");
	static Class<?> enumEntityUseActionClass = Reflections.getMinecraftClass("PacketPlayInUseEntity$EnumEntityUseAction");
	static Class<?> packetPlayInUseEntityClass = Reflections.getMinecraftClass("PacketPlayInUseEntity");
	static FieldAccessor<?> networkField = Reflections.getField(playerConnectionClass, "networkManager", networkManagerClass);
	static FieldAccessor<Channel> channelField = Reflections.getField(networkManagerClass, "channel", Channel.class);
	static MethodInvoker enumEntityUseActionMethod = Reflections.getMethod(packetPlayInUseEntityClass, "b");
	static Object enumEntityUseActionAttack = Reflections.getField(enumEntityUseActionClass, "ATTACK", enumEntityUseActionClass)
			.get(null);
	static Object enumEntityUseActionInteract = Reflections.getField(enumEntityUseActionClass, "INTERACT", enumEntityUseActionClass)
			.get(null);
	static FieldAccessor<?> useEntityActionTypeField = Reflections.getField(packetPlayInUseEntityClass, "action", enumEntityUseActionClass);
	static FieldAccessor<Integer> useEntityEntityIdField = Reflections.getField(packetPlayInUseEntityClass, "a", int.class);

	NpcPacketListener fpl;
	Player player;
	Channel channel;

	public PacketInjector(Player player, NpcPacketListener packetListener) {
		this.fpl = packetListener;
		this.player = player;
	}

	public void inject() {
		try {
			Object handle = getHandleMethod.invoke(player);
			Object playerConnection = playerConnectionField.get(handle);
			Object networkManager = networkField.get(playerConnection);
			channel = channelField.get(networkManager);

			channel.pipeline()
					.addAfter("decoder", NpcPacketListener.INJECTOR_NAME, new MessageToMessageDecoder<Object>() {
						@Override
						protected void decode(ChannelHandlerContext arg0, Object packet, List<Object> arg2) throws Exception {
							if (readPacket(packet))
								arg2.add(packet);

						}
					});
		} catch (NoSuchElementException ex) {
			Bukkit.getConsoleSender()
					.sendMessage("§cCould not inject " + NpcPacketListener.INJECTOR_NAME + " to " + player.getName() + ": " + ex.getClass()
							.getSimpleName() + " " + ex.getMessage());
		}
	}

	public void uninject() {
		try {
			if (channel.pipeline()
					.get(NpcPacketListener.INJECTOR_NAME) != null) {
				channel.eventLoop()
						.submit(() -> {
							channel.pipeline()
									.remove(NpcPacketListener.INJECTOR_NAME);
							return null;
						});
			}
		} catch (Exception ex) {
		}
	}

	public boolean readPacket(Object packet) {
		try {
			if (packetPlayInUseEntityClass.isInstance(packet)) {
				int entityId = useEntityEntityIdField.get(packet);
				Object type = useEntityActionTypeField.get(packet);

				boolean wasNpc = false;

				for (Npc npc : MyNpc.getInstance()
						.getNpcmanager()
						.getNpcs()) {
					if (entityId == npc.getNmsnpc()
							.getEntityId()) {
						if (this.player.getWorld()
								.equals(npc.getLocation()
										.getWorld())) {
							double distanceSqrt = this.player.getLocation()
									.distanceSquared(npc.getLocation());
							if (distanceSqrt < 25) {
								wasNpc = true;
								Bukkit.getScheduler()
										.runTask(this.fpl.pl, () -> {
											if (type.equals(enumEntityUseActionAttack)) {
												npc.onLeftClick(this.player);
											} else if (type.equals(enumEntityUseActionInteract)) {
												npc.onRightClick(this.player);
											}
										});
							}
						}
					}
				}
				return !wasNpc;
			}
		} catch (Exception ex) {
			HastebinReporter.getDefaultReporter()
					.reportError(this.getClass(), ex, "error handling packet " + packet.getClass()
							.getSimpleName());
			ex.printStackTrace();
		}
		return true;
	}
}