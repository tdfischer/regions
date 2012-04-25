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
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import java.util.logging.Logger;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Set;

public class RegionManager {
    Logger log = Logger.getLogger("Regions.RegionManager");
    private Map<String, Collection<Region>> m_regions;
    private Map<String, Region> m_cityRegions;
    private Map<String, Region> m_homeRegions;
    private PluginManager m_pm;

    public RegionManager(PluginManager pm) {
        m_pm = pm;
        clear();
    }

    public void clear() {
        m_regions = new HashMap<String, Collection<Region>>();
        m_cityRegions = new HashMap<String, Region>();
        m_homeRegions = new HashMap<String, Region>();
    }

    public void renameWorld(String oldName, String newName) {
        log.fine("Renaming "+oldName+" to "+newName);
        m_regions.put(newName, m_regions.remove(oldName));
        m_cityRegions.put(newName, m_cityRegions.remove(oldName));
    }

    public Region cityRegion(String worldName) {
        return m_cityRegions.get(worldName);
    }

    public void setCityRegion(String worldName, Region region) {
        m_cityRegions.put(worldName, region);
    }

    public boolean addRegion(Region r) {
        String worldName = r.location().getWorld().getName();
        log.fine("Adding new region "+r.name()+" at "+r.location());
        if (!m_regions.containsKey(worldName))
            m_regions.put(worldName, new ArrayList<Region>());
        if (m_regions.get(worldName).add(r)) {
            m_pm.callEvent(new RegionCreateEvent(r));
        }
        return false;
    }

    public boolean removeRegion(Region r) {
        String worldName = r.location().getWorld().getName();
        log.fine("Removing region "+r.name()+" from "+r.location());
        if (m_regions.containsKey(worldName)) {
            if (m_regions.get(worldName).remove(r)) {
                m_pm.callEvent(new RegionRemoveEvent(r));
            }
            return true;
        }
        return false;
    }

    public Collection<Region> regionsForWorld(World world) {
        return regionsForWorld(world.getName());
    }

    public Collection<Region> regionsForWorld(String worldName) {
        if (m_regions.containsKey(worldName))
            return Collections.unmodifiableCollection(m_regions.get(worldName));
        else
            return Collections.unmodifiableCollection(new ArrayList<Region>());
    }

    public List<Player> filterPlayersInRegion(Region r, Player[] players) {
        ArrayList<Player> ret = new ArrayList<Player>();
        for (Player p : players) {
            Region nearest = nearestRegion(p.getLocation());
            if (nearest == r)
                ret.add(p);
        }
        return ret;
    }

    public Region nearestRegion(Location loc) {
        Collection<Region> regions = regionsForWorld(loc.getWorld());
        Region nearest = null;
        int minDistance = -1;
        for(Region r : regions) {
            int check = distance(loc, r.location());
            if (minDistance == -1 || check < minDistance) {
                nearest = r;
                minDistance = check;
            }
        }
        return nearest;
    }

    public void saveRegions(ConfigurationSection section) {
        for(String worldName : m_regions.keySet()) {
            ConfigurationSection worldSection = section.createSection(worldName);
            Region cityRegion = cityRegion(worldName);
            if (cityRegion != null)
                worldSection.set("city", cityRegion.name());
            ConfigurationSection worldRegionSection = worldSection.createSection("regions");
            for(Region r : regionsForWorld(worldName)) {
                ConfigurationSection regionSection = worldRegionSection.createSection(r.name());
                regionSection.set("x", r.location().getBlockX());
                regionSection.set("z", r.location().getBlockZ());
                ArrayList<String> homePlayers = new ArrayList<String>();
                for(String player : m_homeRegions.keySet()) {
                    if (m_homeRegions.get(player) == r) {
                        homePlayers.add(player);
                    }
                }
                regionSection.set("players", homePlayers);
            }
        }
    }

    public Region homeRegion(String playerName) {
        return m_homeRegions.get(playerName);
    }

    public void setHomeRegion(String player, Region r) {
        m_homeRegions.put(player, r);
    }

    public void loadRegions(ConfigurationSection section, Server server) {
        Set<String> worldNames = section.getKeys(false);
        for(String worldName : worldNames) {
            ConfigurationSection worldSection = section.getConfigurationSection(worldName);
            String cityName = worldSection.getString("city");
            ConfigurationSection worldRegionSection = worldSection.getConfigurationSection("regions");
            Set<String> regionNames = worldRegionSection.getKeys(false);
            World world = server.getWorld(worldName);
            if (world == null) {
                log.warning("Could not find world: "+worldName);
                continue;
            }
            for(String regionName : regionNames) {
                ConfigurationSection regionSection = worldRegionSection.getConfigurationSection(regionName);
                int x = regionSection.getInt("x");
                int z = regionSection.getInt("z");
                Location loc = new Location(world, x, 64, z);
                Region r = new Region(regionName, loc);
                addRegion(r);

                if (regionName.equals(cityName)) {
                    m_cityRegions.put(worldName, r);
                }

                List<String> regionPlayers = regionSection.getStringList("players");
                for(String player : regionPlayers) {
                    m_homeRegions.put(player, r);
                }
            }
        }
    }

    /**
     * An alternative to Location.distance() which doesn't use floating point math.
     */
    private static int distance(Location l1, Location l2) {
        return Math.abs((l1.getBlockX()-l2.getBlockX())+Math.abs(l1.getBlockZ()-l2.getBlockZ()));
    }
}
