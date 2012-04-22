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

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Location;
import org.bukkit.plugin.PluginManager;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.logging.Logger;

import java.util.Random;

public class Plugin extends JavaPlugin {
    Logger log = Logger.getLogger("Regions");
    RegionManager m_regions;
    PlayerWatcher m_playerWatcher;

    public RegionManager regionManager() {
        return m_regions;
    }

    public void onEnable() {
        log.info("[Regions] Enabling Regions");
        m_regions = new RegionManager();
        loadRegions();

        m_playerWatcher = new PlayerWatcher(this);
        
        getServer().getScheduler().scheduleAsyncRepeatingTask(this, m_playerWatcher, 0, 5*20);

        CommandExecutor regionCommand = new RegionCommand(this);
        getCommand("region").setExecutor(regionCommand);
    }

    private void loadTestRegions() {
        log.info("[Regions] Loading test regions for development");
        String[] regionNames = {"Redstone", "Lapis", "Dwarf City"};
        Random rand = new Random();
        for(World w : getServer().getWorlds()) {
            for(String name : regionNames) {
                Location loc = new Location(w, rand.nextInt(30), 64, rand.nextInt(30));
                Region r = new Region(name, loc);
                m_regions.addRegion(r);
            }
        }
    }

    public void loadRegions() {
        reloadConfig();
        ConfigurationSection section = getConfig().getConfigurationSection("regions");
        if (section != null)
            m_regions.loadRegions(section, getServer());
    }

    public void saveRegions() {
        m_regions.saveRegions(getConfig().createSection("regions"));
        saveConfig();
    }

    public void onDisable() {
        saveRegions();
        log.info("[Regions] Plugin disabled");
    }
}
