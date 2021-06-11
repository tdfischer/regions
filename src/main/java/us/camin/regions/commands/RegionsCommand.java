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
import org.bukkit.command.TabCompleter;
import org.bukkit.World;

import java.util.List;
import java.util.ArrayList;

import us.camin.regions.Plugin;
import us.camin.regions.Region;

public class RegionsCommand implements CommandExecutor, TabCompleter {
    Plugin m_plugin;

    public RegionsCommand(Plugin p) {
        m_plugin = p;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> ret = new ArrayList<String>();
        for(World w : m_plugin.getServer().getWorlds()) {
            ret.add(w.getName());
        }
        return ret;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
        World world = null;
        if (sender instanceof Player) {
            Player p = (Player)sender;
            world = p.getLocation().getWorld();
        }
        if (split.length >= 0) {
            world = m_plugin.getServer().getWorld(String.join(" ", split));
        }

        if (world == null) {
            sender.sendMessage("Please specify a world.");
            return true;
        }

        sender.sendMessage("Regions in this world:");
        for (Region r : m_plugin.regionManager().regionsForWorld(world)) {
            sender.sendMessage(r.coloredName());
        }
        return true;
    }
}
