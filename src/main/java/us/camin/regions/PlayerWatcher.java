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
import org.bukkit.event.Event;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitTask;

import us.camin.regions.events.PlayerNearRegionPostEvent;
import us.camin.regions.events.PlayerPostInteractEvent;
import us.camin.regions.events.PlayerRegionChangeEvent;
import us.camin.regions.events.PlayerAddRegionChargeEvent;
import us.camin.regions.events.RegionCreateEvent;
import us.camin.regions.events.RegionRemoveEvent;
import us.camin.regions.ui.RegionPostBuilder;

import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.Collections;

public class PlayerWatcher implements Listener {
    Logger log = Logger.getLogger("Regions.PlayerWatcher");
    private RegionManager m_manager;
    private Plugin m_plugin;
    private BukkitTask m_recalculateTask = null;
    private PlayerTracker m_tracker;

    private static int WATCH_INTERVAL = 3 * 20; // Every 3 seconds

    public PlayerWatcher(Plugin plugin, RegionManager manager) {
        m_manager = manager;
        m_plugin = plugin;
        m_tracker = new PlayerTracker();
    }

    private void recalculateAndReschedule() {
        if (m_recalculateTask != null) {
            m_recalculateTask.cancel();
        }
        m_tracker.recalculatePlayerRegions();
        m_recalculateTask = m_plugin.getServer().getScheduler().runTaskLater(m_plugin, () -> recalculateAndReschedule(), WATCH_INTERVAL);
    }

    @EventHandler
    public void onReload(ServerLoadEvent event) {
        recalculateAndReschedule();
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        recalculateAndReschedule();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        recalculateAndReschedule();
        Region homeRegion = m_manager.homeRegion(event.getPlayer().getName());
        if (homeRegion != null) {
            //event.getPlayer().setSubtitle(TextComponent.fromLegacyText(homeRegion.coloredName()));
            //event.getPlayer().setCompassTarget(homeRegion.location());
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        recalculateAndReschedule();
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        recalculateAndReschedule();
    }

    @EventHandler
    public void onRegionCreate(RegionCreateEvent event) {
        recalculateAndReschedule();
    }

    @EventHandler
    public void onRegionCreate(RegionRemoveEvent event) {
        m_tracker.forgetRegion(event.region);
        recalculateAndReschedule();
    }

    public Collection<Player> playersInRegion(Region r) {
        return m_tracker.playersInRegion(r);
    }

    public void recalculatePlayerRegions(boolean b) {
        m_tracker.recalculatePlayerRegions(b);
    }

    public void clear() {
        m_tracker.clear();
    }

    private class PlayerTracker {
	    private Map<Region, Collection<Player>> m_regionPlayerLists;
	    private Map<Player, Region> m_lastKnownRegions;

	    public PlayerTracker() {
        m_lastKnownRegions = new HashMap<Player, Region>();
        m_regionPlayerLists = new HashMap<Region, Collection<Player>>();
	    }

	    public synchronized Collection<Player> playersInRegion(Region r) {
        if (m_regionPlayerLists.get(r) == null) {
            return Collections.unmodifiableCollection(new ArrayList<Player>());
        }
        return Collections.unmodifiableCollection(m_regionPlayerLists.get(r));
	    }

	    public synchronized void clear() {
        m_lastKnownRegions = new HashMap<Player, Region>();
        m_regionPlayerLists = new HashMap<Region, Collection<Player>>();
	    }

	    public synchronized void forgetRegion(Region r) {
        m_regionPlayerLists.remove(r);
	    }

	    public synchronized void recalculatePlayerRegions() {
		    recalculatePlayerRegions(false);
	    }

      private HashMap<Player, Boolean> m_playerIsNearby = new HashMap<>();

      public boolean playerIsNearby(Player p) {
          if (!m_playerIsNearby.containsKey(p)) {
              m_playerIsNearby.put(p, false);
          }
          return m_playerIsNearby.get(p);
      }

	    public synchronized void recalculatePlayerRegions(boolean quiet) {
        ArrayList<Event> updateEvents = new ArrayList<Event>();
        Collection<? extends Player> allPlayers = m_plugin.getServer().getOnlinePlayers();
        for (Player p : allPlayers) {
            Location loc = p.getLocation();
            Region nearest = m_manager.nearestRegion(loc);
            if (nearest != null) {
              log.finest("Current region for "+p.getName()+": "+nearest.name());
              Region last = m_lastKnownRegions.get(p);
              if (nearest != last) {
                  log.fine("Player "+p.getName()+" entered region "+nearest.name());
                  if (m_regionPlayerLists.get(nearest) == null) {
                    m_regionPlayerLists.put(nearest, new ArrayList<Player>());
                  }
                  m_regionPlayerLists.get(nearest).add(p);
                  if (m_regionPlayerLists.get(last) != null) {
                    m_regionPlayerLists.get(last).remove(p);
                  }
                  m_lastKnownRegions.put(p, nearest);
                  m_playerIsNearby.put(p, false);
                  if (!quiet) {
                    updateEvents.add(new PlayerRegionChangeEvent(p, last, nearest));
                  }
              }
              boolean isNearby = loc.distance(nearest.location()) <= 10;
              if (!m_playerIsNearby.containsKey(p)) {
                  m_playerIsNearby.put(p, isNearby);
              }
              if (isNearby != m_playerIsNearby.get(p)) {
                  m_playerIsNearby.put(p, isNearby);
                  if (isNearby && !quiet) {
                      updateEvents.add(new PlayerNearRegionPostEvent(p, nearest));
                  }
              }
            }
        }
        for (Event e : updateEvents) {
            m_plugin.getServer().getScheduler().runTask(m_plugin, () -> m_plugin.getServer().getPluginManager().callEvent(e));
        }
	    }
    }


}
