package de.packsolite.mynpc.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlotSquaredWrapper {

	public static boolean ownsPlot(Player player, Location location) throws ClassNotFoundException, NoClassDefFoundError, ReflectiveOperationException {
		try {
			Class<?> locationClass = Class.forName("com.github.intellectualsites.plotsquared.plot.object.Location");
			Class<?> areaClass = Class.forName("com.github.intellectualsites.plotsquared.plot.object.PlotArea");
			Class<?> plotClass = Class.forName("com.github.intellectualsites.plotsquared.plot.object.Plot");
			Class<?> psClass = Class.forName("com.github.intellectualsites.plotsquared.plot.PlotSquared");

			Constructor<?> locationConst = locationClass.getConstructor(String.class, int.class, int.class, int.class);
			Method getPSMethod = psClass.getMethod("get");
			Method getAreaMethod = psClass.getMethod("getApplicablePlotArea", locationClass);
			Method getPlotMethod = areaClass.getMethod("getPlot", locationClass);
			Method getOwnersMethod = plotClass.getMethod("getOwners");

			Object plotLoc = locationConst.newInstance(location.getWorld()
					.getName(), (int) location.getX(), (int) location.getY(), (int) location.getZ());
			Object plotArea = getAreaMethod.invoke(getPSMethod.invoke(null), plotLoc);
			Object plot = getPlotMethod.invoke(plotArea, plotLoc);
			Set<UUID> owners = (Set<UUID>) getOwnersMethod.invoke(plot);

			String playerUuid = player.getUniqueId()
					.toString();
			return owners.stream()
					.anyMatch(uuid -> uuid.toString()
							.equalsIgnoreCase(playerUuid));
		} catch (Exception ex) {
			return ownsPlotOld(player, location);
		}
	}

	public static boolean ownsPlotOld(Player player, Location location) throws ClassNotFoundException, NoClassDefFoundError, ReflectiveOperationException {
		Class<?> locationClass = Class.forName("com.intellectualcrafters.plot.object.Location");
		Class<?> areaClass = Class.forName("com.intellectualcrafters.plot.object.PlotArea");
		Class<?> plotClass = Class.forName("com.intellectualcrafters.plot.object.Plot");
		Class<?> psClass = Class.forName("com.intellectualcrafters.plot.PS");

		Constructor<?> locationConst = locationClass.getConstructor(java.lang.String.class, int.class, int.class, int.class);
		Method getPSMethod = psClass.getMethod("get");
		Method getAreaMethod = psClass.getMethod("getApplicablePlotArea", locationClass);
		Method getPlotMethod = areaClass.getMethod("getPlot", locationClass);
		Method getOwnersMethod = plotClass.getMethod("getOwners");

		Object plotLoc = locationConst.newInstance(location.getWorld()
				.getName(), (int) location.getX(), (int) location.getY(), (int) location.getZ());
		Object plotArea = getAreaMethod.invoke(getPSMethod.invoke(null), plotLoc);
		Object plot = getPlotMethod.invoke(plotArea, plotLoc);
		Set<UUID> owners = (Set<UUID>) getOwnersMethod.invoke(plot);

		String playerUuid = player.getUniqueId()
				.toString();
		return owners.stream()
				.anyMatch(uuid -> uuid.toString()
						.equalsIgnoreCase(playerUuid));
	}
}
