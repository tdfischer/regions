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

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import java.util.logging.Logger;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Set;

import us.camin.regions.config.RegionConfiguration;
import us.camin.regions.config.WorldConfiguration;
import us.camin.regions.events.RegionCreateEvent;
import us.camin.regions.events.RegionRemoveEvent;
import us.camin.regions.geometry.RegionSet;
import java.util.logging.Level;

public class RegionManager {

    Logger log = Logger.getLogger("Regions.RegionManager");
    private Map<String, RegionSet> m_regions;
    private Server m_server;
    public RegionManager(Plugin plugin, Server server) {
        m_server = server;
        log.setLevel(Level.ALL);
        clear();
    }

    public synchronized void clear() {
        m_regions = new HashMap<>();
    }

    public synchronized void renameWorld(String oldName, String newName) {
        log.fine("Renaming "+oldName+" to "+newName);
        m_regions.put(newName, m_regions.remove(oldName));
    }

    public synchronized boolean addRegion(Region r) {
        String worldName = r.location().getWorld().getName();
        log.fine("Adding new region "+r.name()+" at "+r.location());
        if (!m_regions.containsKey(worldName))
            m_regions.put(worldName, new RegionSet());
        if (m_regions.get(worldName).add(r)) {
            m_server.getPluginManager().callEvent(new RegionCreateEvent(r));
        }
        return false;
    }

    public synchronized boolean removeRegion(Region r) {
        String worldName = r.location().getWorld().getName();
        log.fine("Removing region "+r.name()+" from "+r.location());
        if (m_regions.containsKey(worldName)) {
            if (m_regions.get(worldName).remove(r)) {
                m_server.getPluginManager().callEvent(new RegionRemoveEvent(r));
            }
            return true;
        }
        return false;
    }

    public RegionSet regionsForWorld(World world) {
        return regionsForWorld(world.getName());
    }

    public Collection<Region> neighborsForRegion(Region region) {
        return regionsForWorld(region.location().getWorld()).borders().neighbors(region);
    }

    public Collection<Region> worldHubs(World world) {
        ArrayList<Region> regions = new ArrayList<Region>();
        for(Region r : regionsForWorld(world.getName())) {
          if (r.isHub()) {
            regions.add(r);
          }
        }
        return regions;
    }

    public synchronized RegionSet regionsForWorld(String worldName) {
        if (m_regions.containsKey(worldName)) {
            return m_regions.get(worldName);
        } else {
            return new RegionSet();
        }
    }

    public Region nearestRegion(Location loc) {
        return regionsForWorld(loc.getWorld()).nearestRegion(loc);
    }

    public synchronized void saveRegions(ConfigurationSection section) {
        for(String worldName : m_regions.keySet()) {
            ConfigurationSection worldRegionSection = section.createSection(worldName);
            for(Region r : regionsForWorld(worldName)) {
                RegionConfiguration conf = new RegionConfiguration(r);
                worldRegionSection.createSection(r.name(), conf.serialize());
            }
        }
    }

    public synchronized Region homeRegion(String playerName) {
        return null;
        //return m_homeRegions.get(playerName);
    }

    public synchronized void setHomeRegion(Player player, Region r) {
        /*Region old = m_homeRegions.get(player.getName());
        m_homeRegions.put(player.getName(), r);
  	    log.info("Player "+player.getName()+" moved in to "+r.name());
  	    PlayerMoveInEvent evt = new PlayerMoveInEvent(player, r, old);
  	    m_plugin.getServer().getPluginManager().callEvent(evt);*/
    }

    public synchronized void loadRegions(ConfigurationSection section) {
    	for(World world : m_server.getWorlds()) {
    		ConfigurationSection worldConfig = section.getConfigurationSection(world.getName());

    		if (worldConfig == null) {
    			log.info("No regions configured for world " + world.getName());
    			continue;
    		}

            for(String regionName : worldConfig.getKeys(false)) {
                RegionConfiguration conf = new RegionConfiguration(worldConfig.getConfigurationSection(regionName).getValues(false));
                addRegion(new Region(regionName, world, conf));
            }
    	}
    }
}
