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

public class BukkitEventHandler implements Listener {
    Plugin m_plugin;
    public BukkitEventHandler(Plugin p) {
        m_plugin = p;
    }
    
    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        m_plugin.recalculatePlayerRegions();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        m_plugin.recalculatePlayerRegions();
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        m_plugin.recalculatePlayerRegions();
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        m_plugin.recalculatePlayerRegions();
    }
}
