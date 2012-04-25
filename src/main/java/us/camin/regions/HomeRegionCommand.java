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
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class HomeRegionCommand implements CommandExecutor {

    Plugin m_plugin;

    public HomeRegionCommand(Plugin p) {
        m_plugin = p;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Region command is only available to players.");
            return true;
        }
        Player p = (Player)sender;
        Region home = m_plugin.regionManager().homeRegion(p.getName());
        Region nearest = m_plugin.regionManager().nearestRegion(p.getLocation());

        if (home != null) {
            if (p.getLocation().distance(nearest.teleportLocation()) <= 5) {
                p.teleport(home.teleportLocation(), TeleportCause.COMMAND);
            } else {
                sender.sendMessage("You must be within 5 blocks of a region center.");
            }
        } else {
            sender.sendMessage("You have no home region.");
        }
        return true;
    }
}
