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
import org.dynmap.markers.MarkerSet;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.Marker;
import org.bukkit.event.EventHandler;
import org.bukkit.Location;

public class RegionEventHandler implements Listener {
    private MarkerSet m_set;
    private MarkerAPI m_api;

    public RegionEventHandler(MarkerAPI markerAPI) {
        m_api = markerAPI;
        m_set = m_api.createMarkerSet("Regions", "Regions", null, false);
    }

    @EventHandler
    public void onRegionEvent(RegionRemoveEvent event) {
        Marker marker = m_set.findMarkerByLabel(event.region.name());
        marker.deleteMarker();
    }

    @EventHandler
    public void onRegionEvent(RegionCreateEvent event) {
        Location loc = event.region.location();
        MarkerIcon icon = m_api.getMarkerIcon("default");
        m_set.createMarker(null, event.region.name(), loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), icon, false);
    }
}
