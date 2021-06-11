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
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;

import us.camin.regions.Plugin;
import us.camin.regions.RegionPostItemWatcher;

public class RegionOpCommand implements CommandExecutor, TabCompleter {
    Plugin m_plugin;

    public RegionOpCommand(Plugin p) {
        m_plugin = p;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> ret = new ArrayList<String>();
        ret.add("save");
        ret.add("load");
        ret.add("item");
        ret.add("compass");
        ret.add("chargeitem");
        return ret;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
        String subCommand = split[0];
        if (subCommand.equals("save") && sender.hasPermission("regions.create")) {
            m_plugin.saveRegions();
            sender.sendMessage("Regions saved.");
        } else if (subCommand.equals("load") && sender.hasPermission("regions.create")) {
            sender.sendMessage("Reloading regions...");
            m_plugin.getServer().getScheduler().runTask(m_plugin, () -> {
                m_plugin.loadRegions();
                sender.sendMessage("Regions loaded.");
            });
        } else if (subCommand.equals("compass") && sender.hasPermission("regions.give-items.compass")) {
            Player player = (Player)sender;
            ItemStack compassItem = RegionPostItemWatcher.createCompass(m_plugin.regionManager().nearestRegion(player.getLocation()));
            if (split.length > 1) {
              compassItem.setAmount(Integer.parseInt(split[1]));
            }
            HashMap<Integer, ItemStack> rejected = player.getInventory().addItem(compassItem);
            for(ItemStack item : rejected.values()) {
              player.getLocation().getWorld().dropItem(player.getLocation(), item);
            }
        } else if (subCommand.equals("item") && sender.hasPermission("regions.give-items.creator")) {
            Player player = (Player)sender;
            ItemStack createItem = RegionPostItemWatcher.createCreateItem();
            if (split.length > 1) {
              createItem.setAmount(Integer.parseInt(split[1]));
            }
            HashMap<Integer, ItemStack> rejected = player.getInventory().addItem(createItem);
            for(ItemStack item : rejected.values()) {
              player.getLocation().getWorld().dropItem(player.getLocation(), item);
            }
        } else if (subCommand.equals("chargeitem") && sender.hasPermission("regions.give-items.charge")) {
            Player player = (Player)sender;
            ItemStack chargeItem = RegionPostItemWatcher.createChargeItem();
            if (split.length > 1) {
              chargeItem.setAmount(Integer.parseInt(split[1]));
            }
            HashMap<Integer, ItemStack> rejected = player.getInventory().addItem(chargeItem);
            for(ItemStack item : rejected.values()) {
              player.getLocation().getWorld().dropItem(player.getLocation(), item);
            }
        } else {
            sender.sendMessage("Unknown operation. Options are save, load, item, compass.");
        }
        return true;
    }
}

