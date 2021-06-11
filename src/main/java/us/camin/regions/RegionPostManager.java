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
import org.bukkit.Location;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import us.camin.regions.events.PlayerNearRegionPostEvent;
import us.camin.regions.events.RegionCreateEvent;
import us.camin.regions.events.RegionRemoveEvent;
import us.camin.regions.ui.RegionPostBuilder;

import com.gmail.filoghost.holographicdisplays.api.Hologram;

public class RegionPostManager implements Listener {
    Logger log = Logger.getLogger("Regions.RegionPostManager");

    Plugin m_plugin;
    RegionManager m_regions;
    boolean m_useHolograms;
    HashMap<Region, Hologram> m_regionHolograms;
    //HashMap<Region, ArmorStand> m_armorStands;

    public RegionPostManager(RegionManager regions, Plugin plugin, boolean useHolograms) {
        //m_armorStands = new HashMap<Region, ArmorStand>();
        m_regions = regions;
        m_plugin = plugin;
        m_useHolograms = useHolograms;
        m_regionHolograms = new HashMap<>();
    }

    @EventHandler
    public void onRegionCreate(RegionCreateEvent event) {
        Location loc = event.region.location();
        if (loc.getWorld().isChunkLoaded((int)loc.getX(), (int)loc.getY())) {
            createHologram(event.region);
            queueRebuild(event.region);
        }
    }

    @EventHandler
    public void onPlayerNearRegionPost(PlayerNearRegionPostEvent event) {
        createHologram(event.region);
    }

    private void queueRebuild(Region region) {
        if (region != null) {
          log.info("Rebuilding region post for " + region.name());
          m_plugin.getServer().getScheduler().runTask(m_plugin, () -> {
            RegionPostBuilder builder = new RegionPostBuilder(region, m_plugin);
            builder.build();
          });
        }
    }

    @EventHandler
    public void onRegionDestroy(RegionRemoveEvent event) {
        destroyHologram(event.region);
    }

    public void release() {
      for(Region r : new ArrayList<>(m_regionHolograms.keySet())) {
          destroyHologram(r);
      }
      /*for(Region r : new ArrayList<>(m_armorStands.keySet())) {
          destroyHologram(r);
      }*/
    }

    private void createHologram(Region r) {
        if (!m_useHolograms) {
          return;
        }
        Hologram hologram = m_regionHolograms.get(r);
        if (hologram == null) {
            hologram = HologramsAPI.createHologram(m_plugin, r.teleportLocation().clone().add(0.5, 0, 0.5));
            hologram.appendTextLine(r.coloredName());
            m_regionHolograms.put(r, hologram);
        }
        /*boolean needsRegen = false;
        if (m_armorStands.containsKey(r)) {
            // If we have an armor stand, but it isn't valid, regen
            needsRegen = !m_armorStands.get(r).isValid();
        } else {
            // If we don't have an armor stand at all, regen
            needsRegen = true;
        }
        if (needsRegen) {
          log.info("Creating hologram for " + r.name());
          Location markerLocation = r.teleportLocation().clone().add(0.5, 0, 0.5);
          ArmorStand stand = (ArmorStand) r.location().getWorld().spawnEntity(markerLocation, EntityType.ARMOR_STAND);
          stand.setVisible(false);
          stand.setCustomName(r.coloredName());
          stand.setMarker(true);
          stand.setSmall(true);
          stand.setRemoveWhenFarAway(true);
          stand.setCustomNameVisible(true);
          stand.setInvulnerable(true);
          stand.setSilent(true);
          m_armorStands.put(r, stand);
        }*/
    }

    private void destroyHologram(Region r) {
        if (!m_useHolograms) {
          return;
        }
        Hologram hologram = m_regionHolograms.get(r);
        if (hologram != null) {
          hologram.delete();
        }
        /*log.info("Destroying hologram for " + r.name());
        ArmorStand stand = m_armorStands.get(r);
        if (stand != null && stand.isValid()) {
            stand.remove();
        }
        m_armorStands.remove(r);*/
    }
}
