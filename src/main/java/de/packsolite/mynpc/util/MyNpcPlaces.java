package de.packsolite.mynpc.util;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import de.packsolite.mynpc.MyNpc;

public class MyNpcPlaces {

	public static boolean canPlace(Player player, Location location) {

		if (!MyNpc.getInstance()
				.getFileManager()
				.getCheckPlaceholder())
			return true;

		try {
			String owner = "%plotsquared_currentplot_owner%";
			owner = (String) Class.forName("me.clip.placeholderapi.PlaceholderAPI")
					.getMethod("setPlaceholders", Player.class, String.class)
					.invoke(null, player, owner);
			if (player.getName()
					.equalsIgnoreCase(owner))
				return true;
		} catch (Exception ex) {
		}

		try {
			String owner = "%acidisland_visited_island_owner%";
			owner = (String) Class.forName("me.clip.placeholderapi.PlaceholderAPI")
					.getMethod("setPlaceholders", Player.class, String.class)
					.invoke(null, player, owner);
			if (player.getName()
					.equalsIgnoreCase(owner))
				return true;
		} catch (Exception ex) {
		}

		try {
			if (PlotSquaredWrapper.ownsPlot(player, location)) {
				return true;
			}
		} catch (Throwable t) {
		}

		try {
			if (LandsWrapper.ownsLand(player, location)) {
				return true;
			}
		} catch (Throwable t) {
		}
		return false;
	}
}
