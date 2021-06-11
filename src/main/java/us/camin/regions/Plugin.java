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
import org.dynmap.markers.MarkerAPI;

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
    RegionPostManager m_regionPosts;

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

        boolean useHolograms = getServer().getPluginManager().isPluginEnabled("HolographicDisplays");
        if (!useHolograms) {
            log.info("HolographicDisplays not enabled. Region posts will not have holograms.");
        }
        m_regionPosts = new RegionPostManager(m_regions, this, useHolograms);
        // TODO: Make holograms configurable. Disabled by default for now.
        //getServer().getPluginManager().registerEvents(m_regionPosts, this);

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
        m_regionPosts.release();
        saveRegions();
        log.info("Plugin disabled");
    }
}
