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

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.entity.Player;

public class BukkitEventHandler implements Listener {
    RegionManager m_manager;
    public BukkitEventHandler(RegionManager manager) {
        m_manager = manager;
    }
    
    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        m_manager.recalculatePlayerRegions();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        m_manager.recalculatePlayerRegions();
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        m_manager.recalculatePlayerRegions();
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        m_manager.recalculatePlayerRegions();
    }

    @EventHandler
    public void onPlayerRegionChanged(PlayerRegionChangeEvent event) {
        if (event.oldRegion != null) {
            for (Player p : m_manager.playersInRegion(event.oldRegion)) {
                p.sendMessage(event.player.getName()+" has left the region.");
            }
        }
        for (Player p : m_manager.playersInRegion(event.newRegion)) {
            if (p != event.player) {
                p.sendMessage(event.player.getName()+" has entered the region.");
            }
        }
        event.player.sendMessage("Now entering region: "+event.newRegion.name());
    }
}
