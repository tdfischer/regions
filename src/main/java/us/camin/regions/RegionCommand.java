package us.camin.regions;

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
import org.bukkit.entity.Player;

public class RegionCommand implements CommandExecutor {

    Plugin m_plugin;

    public RegionCommand(Plugin p) {
        m_plugin = p;
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
            p.sendMessage("Current region: "+r.name());
            return true;
        }
        String subCommand = split[0];
        if (subCommand.equals("create") && p.hasPermission("regions.create")) {
            StringBuilder regionName = new StringBuilder();
            for(int i = 1;i<split.length-1;i++) {
                regionName.append(split[i]);
                regionName.append(" ");
            }
            regionName.append(split[split.length-1]);
            Region r = new Region(regionName.toString(), p.getLocation());
            m_plugin.regionManager().addRegion(r);
            m_plugin.regenRegionPost(r);
            p.teleport(r.teleportLocation());
        } else if (subCommand.equals("remove") && p.hasPermission("regions.remove")) {
            Region r = m_plugin.regionManager().nearestRegion(p.getLocation());
            if (r == null) {
                p.sendMessage("There are no regions in this world.");
                return true;
            }
            m_plugin.regionManager().removeRegion(r);
        } else if (subCommand.equals("city") && p.hasPermission("regions.setCity")) {
            Region r = m_plugin.regionManager().nearestRegion(p.getLocation());
            if (r == null) {
                p.sendMessage("There are no regions in this world.");
                return true;
            }
            m_plugin.regionManager().setCityRegion(p.getLocation().getWorld().getName(), r);
            p.sendMessage("City region set to "+r.name());
        } else if (subCommand.equals("regen") && p.hasPermission("regions.create")) {
            Region r = m_plugin.regionManager().nearestRegion(p.getLocation());
            m_plugin.regenRegionPost(r);
            p.sendMessage("Region post regenerated.");
        } else {
            p.sendMessage("Unknown operation. Options are create, remove, city.");
        }
        return true;
    }
}
