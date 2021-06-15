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
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.World;

import org.dynmap.markers.MarkerAPI;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;

import us.camin.regions.commands.RegionCommand;
import us.camin.regions.commands.RegionOpCommand;
import us.camin.regions.commands.RegionsCommand;
import us.camin.regions.config.RegionConfiguration;
import us.camin.regions.config.WorldConfiguration;
import us.camin.regions.ui.PlayerInventoryTeleporter;

import org.dynmap.DynmapCommonAPI;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.login.ConfigurationSpi;

public class Plugin extends JavaPlugin {
    Logger log = Logger.getLogger("Regions");
    RegionManager m_regions;
    PlayerWatcher m_playerWatcher;

    public RegionManager regionManager() {
        return m_regions;
    }

    public PlayerWatcher playerWatcher() {
        return m_playerWatcher;
    }

    public void onEnable() {
        log.info("Enabling Regions");
        ConfigurationSerialization.registerClass(RegionConfiguration.class);
        m_regions = new RegionManager(this, getServer());
        m_playerWatcher = new PlayerWatcher(this, m_regions);
        
        getCommand("region").setExecutor(new RegionCommand(this));
        getCommand("regions").setExecutor(new RegionsCommand(this));
        getCommand("regionop").setExecutor(new RegionOpCommand(this));

        loadRegions();
        m_playerWatcher.recalculatePlayerRegions(true);
        org.bukkit.plugin.Plugin mapPlugin = getServer().getPluginManager().getPlugin("dynmap");
        if (mapPlugin instanceof DynmapCommonAPI && mapPlugin != null) {
            DynmapCommonAPI mapAPI = (DynmapCommonAPI)mapPlugin;
            MarkerAPI markerAPI = mapAPI.getMarkerAPI();
            if (markerAPI != null) {
                DynmapEventRelay regionHandler = new DynmapEventRelay (this, markerAPI);
                getServer().getPluginManager().registerEvents(regionHandler, this);
            } else {
                log.info("Dynmap marker API not found. Disabling map support.");
            }
        } else {
            log.info("Dynmap not found. Disabling map support.");
        }

        // Install the event handler after things are loaded so players aren't spammed with text
        getServer().getPluginManager().registerEvents(m_playerWatcher, this);
        getServer().getPluginManager().registerEvents(new PlayerNotifier(this, m_regions), this);
        getServer().getPluginManager().registerEvents(new PlayerInventoryTeleporter(this, m_regions), this);
        getServer().getPluginManager().registerEvents(new RegionPostItemWatcher(this, m_regions), this);
        getServer().getPluginManager().registerEvents(new RegionPostInteractionWatcher(this, m_regions), this);

        // PluginID is from bstats.org for CaminusRegions
        Metrics metrics = new Metrics(this, 11705);
        metrics.addCustomChart(new SingleLineChart("regions", () -> {
            int allRegions = 0;
            for(World w : getServer().getWorlds()) {
              allRegions += m_regions.regionsForWorld(w).size();
            }
            return allRegions;
          }
        ));
    }

    public void loadRegions() {
        reloadConfig();
        m_regions.clear();
        this.getDataFolder().mkdir();
        File regionConfigFile = new File(this.getDataFolder(), "regions.yml");
        Configuration regionConf = YamlConfiguration.loadConfiguration(regionConfigFile);
        m_regions.loadRegions(regionConf);
    }

    public void saveRegions() {
    	this.getDataFolder().mkdir();
        File regionConfigFile = new File(this.getDataFolder(), "regions.yml");
        YamlConfiguration regionConf = YamlConfiguration.loadConfiguration(regionConfigFile);
        m_regions.saveRegions(regionConf);
        try {
			regionConf.save(regionConfigFile);
		} catch (IOException e) {
			log.log(Level.SEVERE, "Failed to write out regions.yml!!! Your data has not been saved!", e);
		}
        saveConfig();
    }

    public void onDisable() {
        saveRegions();
        log.info("Plugin disabled");
    }
}
