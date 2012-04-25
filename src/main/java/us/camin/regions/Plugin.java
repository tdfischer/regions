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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.DynmapCommonAPI;

import java.util.logging.Logger;

import java.util.Random;

public class Plugin extends JavaPlugin {
    Logger log = Logger.getLogger("Regions");
    RegionManager m_regions;
    PlayerWatcher m_playerWatcher;

    public RegionManager regionManager() {
        return m_regions;
    }

    public void recalculatePlayerRegions() {
        m_playerWatcher.run();
    }

    public void onEnable() {
        log.info("[Regions] Enabling Regions");
        m_regions = new RegionManager(getServer().getPluginManager());

        m_playerWatcher = new PlayerWatcher(this);
        
        getServer().getScheduler().scheduleAsyncRepeatingTask(this, m_playerWatcher, 0, 5*20);

        CommandExecutor regionCommand = new RegionCommand(this);
        getCommand("region").setExecutor(regionCommand);
        getCommand("cityregion").setExecutor(new CityRegionCommand(this));
        getCommand("homeregion").setExecutor(new HomeRegionCommand(this));
        getCommand("movein").setExecutor(new MoveinRegionCommand(this));

        getServer().getPluginManager().registerEvents(new BukkitEventHandler(this), this);

        org.bukkit.plugin.Plugin mapPlugin = getServer().getPluginManager().getPlugin("dynmap");
        if (mapPlugin instanceof DynmapCommonAPI) {
            DynmapCommonAPI mapAPI = (DynmapCommonAPI)mapPlugin;
            MarkerAPI markerAPI = mapAPI.getMarkerAPI();
            if (markerAPI != null) {
                RegionEventHandler regionHandler = new RegionEventHandler(markerAPI);
                getServer().getPluginManager().registerEvents(regionHandler, this);
            } else {
                log.info("[Regions] Dynmap marker API not found. Disabling map support.");
            }
        } else {
            log.info("[Regions] Dynmap not found. Disabling map support.");
        }

        loadRegions();
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
        ConfigurationSection section = getConfig().getConfigurationSection("worlds");
        if (section != null)
            m_regions.loadRegions(section, getServer());
    }

    public void saveRegions() {
        m_regions.saveRegions(getConfig().createSection("worlds"));
        saveConfig();
    }

    public void onDisable() {
        saveRegions();
        log.info("[Regions] Plugin disabled");
    }

    public void regenRegionPost(Region r) {
        World world = r.location().getWorld();
        Location center = world.getHighestBlockAt(r.location()).getLocation();
        for(int x = center.getBlockX()-1;x <= center.getBlockX()+1;x++) {
            for(int z = center.getBlockZ()-1;z <= center.getBlockZ()+1;z++) {
                Block b = world.getBlockAt(x, center.getBlockY()-1, z);
                b.setType(Material.COBBLESTONE);
            }
        }

        for(int y = center.getBlockY()-2;y < center.getBlockY()+2;y++) {
            Block b = world.getBlockAt(center.getBlockX(), y, center.getBlockZ());
            b.setType(Material.GLOWSTONE);
        }
    }
}
