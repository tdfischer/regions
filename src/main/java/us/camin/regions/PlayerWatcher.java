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

import java.lang.Runnable;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class PlayerWatcher implements Runnable {
    Logger log = Logger.getLogger("Regions.PlayerWatcher");
    private Plugin m_plugin;
    private Map<Player, Region> m_lastKnownRegions;

    public PlayerWatcher(Plugin p) {
        m_plugin = p;
        m_lastKnownRegions = new HashMap<Player, Region>();
    }

    public void run() {
        Player[] allPlayers = m_plugin.getServer().getOnlinePlayers();
        RegionManager manager = m_plugin.regionManager();
        Map<Region, List<Player>> newPlayers = new HashMap<Region, List<Player>>();
        for (Player p : allPlayers) {
            Location loc = p.getLocation();
            Region nearest = manager.nearestRegion(loc);
            if (nearest != null) {
                log.finest("Current region for "+p.getName()+": "+nearest.name());
                if (nearest != m_lastKnownRegions.get(p)) {
                    p.sendMessage("Now entering region: "+nearest.name());
                    log.fine("Player "+p.getName()+" entered region "+nearest.name());
                    m_lastKnownRegions.put(p, nearest);
                    if (!newPlayers.containsKey(nearest))
                        newPlayers.put(nearest, new ArrayList<Player>());
                    newPlayers.get(nearest).add(p);
                }
            }
        }
        for (Region r : newPlayers.keySet()) {
            for (Player newPlayer : newPlayers.get(r)) {
                for (Player oldPlayer : manager.filterPlayersInRegion(r, allPlayers)) {
                    if (oldPlayer != newPlayer) {
                        oldPlayer.sendMessage(newPlayer.getName()+" has entered the region");
                    }
                }
            }
        }
    }
}
