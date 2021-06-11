package us.camin.regions.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import us.camin.regions.Plugin;
import us.camin.regions.Region;

public class RegionTabComplete implements TabCompleter {
	private Plugin m_plugin;

	public RegionTabComplete(Plugin plugin) {
		m_plugin = plugin;
	}

	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (!(sender instanceof Player)) {
		    return new ArrayList<String>();
		}
		Player p = (Player)sender;
		String fullName = String.join(" ", args);

		ArrayList<String> ret = new ArrayList<String>();
		Collection<Region> regions = m_plugin.regionManager().regionsForWorld(p.getLocation().getWorld());
		for (Region r : regions) {
			if (r.name().startsWith(fullName)) {
			    ret.add(r.name());
			}
		}
		return ret;
	}
}
