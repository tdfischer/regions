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
import org.bukkit.entity.Player;
import us.camin.regions.Plugin;
import us.camin.regions.Region;

public class MoveinCommand implements CommandExecutor {

    Plugin m_plugin;

    public MoveinCommand(Plugin p) {
        m_plugin = p;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Region command is only available to players.");
            return true;
        }
        Player p = (Player)sender;
        Region nearest = m_plugin.regionManager().nearestRegion(p.getLocation());
        if (nearest != null) {
            m_plugin.regionManager().setHomeRegion(p, nearest);
            sender.sendMessage("Your home region has been set to "+nearest.name());
        } else {
            sender.sendMessage("There are no regions in this world.");
        }
        return true;
    }
}
