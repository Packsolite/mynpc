package de.packsolite.mynpc.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import de.packsolite.mynpc.MyNpc;

public class LandsWrapper {

	private static Object landsAddon;
	private static Class<?> landsIntegrationClass;

	static {
		try {
			landsIntegrationClass = Class.forName("me.angeschossen.lands.api.integration.LandsIntegration");
			Constructor<?> landsIntegrationConstructor = landsIntegrationClass.getConstructor(org.bukkit.plugin.Plugin.class);
			landsAddon = landsIntegrationConstructor.newInstance(MyNpc.getInstance());
		} catch (Exception e) {
		}
	}

	public static void disable() {
		try {
			landsIntegrationClass.getMethod("disable")
					.invoke(landsAddon);
		} catch (Throwable e) {
		}
	}

	public static boolean ownsLand(Player player, Location location) throws ClassNotFoundException, NoClassDefFoundError, ReflectiveOperationException {

		if (landsAddon == null) {
			throw new ClassNotFoundException("LandsIntegration not found");
		}

		Class<?> landAreaClass = Class.forName("me.angeschossen.lands.api.land.LandArea");
		Method getAreaMethod = landsIntegrationClass.getMethod("getArea", location.getClass());
		Object area = getAreaMethod.invoke(landsAddon, location);

		if (area != null) {
			Class<?> landClass = Class.forName("me.angeschossen.lands.api.land.Land");
			Object land = landAreaClass.getMethod("getLand")
					.invoke(area);

			if (land != null) {
				Object owner = landClass.getMethod("getOwnerUID")
						.invoke(land);

				if (owner != null) {
					String ownerUuid = owner.toString();
					String playerUuid = player.getUniqueId()
							.toString();

					if (ownerUuid.equalsIgnoreCase(playerUuid)) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
