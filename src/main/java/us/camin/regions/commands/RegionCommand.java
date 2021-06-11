package us.camin.regions.commands;

/**
 * This file is part of Regions
 *
 * Regions is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Regions is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Regions.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.ArrayList;

import us.camin.regions.Plugin;
import us.camin.regions.Region;
import us.camin.regions.ui.RegionPostBuilder;

public class RegionCommand implements CommandExecutor, TabCompleter {

    Plugin m_plugin;

    public RegionCommand(Plugin p) {
        m_plugin = p;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> ret = new ArrayList<String>();
        if (args.length <= 1) {
            ret.add("create");
            ret.add("remove");
            ret.add("regen");
            ret.add("regenall");
        }
        return ret;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Region command is only available to players.");
            return true;
        }
        Player p = (Player)sender;
        if (split.length == 0) {
            Region r = m_plugin.regionManager().nearestRegion(p.getLocation());
            if (r == null) {
                p.sendMessage("There are no regions in this world.");
                return true;
            }
            p.sendMessage("Current region: "+r.coloredName());
            p.sendMessage("Players in region:");
            for(Player neighbor : m_plugin.playerWatcher().playersInRegion(r)) {
              p.sendMessage(neighbor.getName());
            }
            return true;
        }
        String subCommand = split[0];
        if (subCommand.equals("create") && p.hasPermission("regions.create")) {
            if (split.length <= 1) {
              p.sendMessage("Must specify a region name");
              return true;
            }
            StringBuilder regionName = new StringBuilder();
            for(int i = 1;i<split.length-1;i++) {
                regionName.append(split[i]);
                regionName.append(" ");
            }
            regionName.append(split[split.length-1]);
            Region r = new Region(regionName.toString(), p.getLocation());
            p.teleport(r.teleportLocation());
            m_plugin.regionManager().addRegion(r);
            m_plugin.saveRegions();
        } else if (subCommand.equals("remove") && p.hasPermission("regions.remove")) {
            Region r = m_plugin.regionManager().nearestRegion(p.getLocation());
            if (r == null) {
                p.sendMessage("There are no regions in this world.");
                return true;
            }
            m_plugin.regionManager().removeRegion(r);
            p.sendMessage("Deleted region " + r.coloredName());
            m_plugin.saveRegions();
        } else if (subCommand.equals("regen") && p.hasPermission("regions.regen")) {
            Region r = m_plugin.regionManager().nearestRegion(p.getLocation());
            if (r == null) {
                p.sendMessage("There are no regions in this world.");
            } else {
                p.sendMessage("Regenerating region post...");
                m_plugin.getServer().getScheduler().runTask(m_plugin, () -> {
                  RegionPostBuilder builder = new RegionPostBuilder(r, m_plugin);
                  builder.build();
                  p.sendMessage("Region post regenerated.");
                });
            }
        } else if (subCommand.equals("regenall") && p.hasPermission("regions.regen.all")) {
            for(Region r : m_plugin.regionManager().regionsForWorld(p.getLocation().getWorld())) {
                m_plugin.getServer().getScheduler().runTask(m_plugin, () -> {
                  RegionPostBuilder builder = new RegionPostBuilder(r, m_plugin);
                  builder.build();
                });
            }
            p.sendMessage("Region posts will be regenerated");
        } else {
            p.sendMessage("Unknown operation. Options are: create, remove, regen.");
        }
        return true;
    }
}
